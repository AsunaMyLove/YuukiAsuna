package com.jzh.callcommand;


import java.io.IOException;

import com.jzh.exception.MyException;
import org.apache.log4j.Logger;


import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

/**
 * 处理远程SSH协议的命令调用
 */
public class RemoteExternalCommand {
    private static Logger logger = Logger.getLogger(RemoteExternalCommand.class);

    private Connection connect;
    private Session session;
    private static final int TIME_OUT = 1000 * 5 * 60; //超过5mins则退出

    /**
     * 通过配置指定的远程服务器ID，读取配置文件中的连接参数进行连接
     *
     * @param remoteServerParam e.g  zhjc/zhjc@168.5.15.146:22
     * @return
     * @throws IOException
     */
    public boolean login(String remoteServerParam) throws IOException {
        int indexPort = remoteServerParam.lastIndexOf(":");
        int indexIP = remoteServerParam.lastIndexOf("@");
        int indexUser = remoteServerParam.lastIndexOf("/");

        String ip = remoteServerParam.substring(indexIP + 1, indexPort);
        String user = remoteServerParam.substring(0, indexUser);
        String psword = remoteServerParam.substring(indexUser + 1, indexIP);
        String port = remoteServerParam.substring(indexPort + 1);

        connect = new Connection(ip, Integer.parseInt(port));
        connect.connect();

        return connect.authenticateWithPassword(user, psword);
    }

    public void execute(String remoteServerParam, String command) throws MyException {
        try {
            logger.debug("execute command:" + command);

            if (login(remoteServerParam)) {
                session = connect.openSession();
                session.execCommand(command);

                StringBuffer builder = new StringBuffer();
                RuntimeExecuteStream errStream = new RuntimeExecuteStream(session.getStderr(), true);
                errStream.setReturnInfo(builder);
                errStream.start();
                RuntimeExecuteStream outStream = new RuntimeExecuteStream(session.getStdout(), false);
                outStream.setReturnInfo(builder);
                outStream.start();

                int conditions = session.waitForCondition(
                        ChannelCondition.EXIT_STATUS, TIME_OUT);
                session.getStderr().close();
                session.getStdout().close();

                if ((conditions & ChannelCondition.TIMEOUT) != 0) { //连接中断
                    throw new IOException("Timeout while waiting for "
                            + TIME_OUT / 1000 + " seconds ");
                }

                //0表示正常 1表示非正常

                int exitValue = session.getExitStatus();
                String errorInfo = builder.toString();
                if (exitValue != 0)
                    throw new MyException(
                            "RemoteExternalCommand error: command=" + command
                                    + "\n" + errorInfo);
            } else {
                throw new MyException("RemoteExternalCommand connect ["
                        + remoteServerParam + "] fail!");
            }
        } catch (IOException e) {
            throw new MyException("RemoteExternalCommand connect ["
                    + remoteServerParam + "] IOException:" + e.getMessage());
        } finally {
            try {
                if (session != null)
                    session.close();
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
                throw new MyException("RemoteExternalCommand connect ["
                        + remoteServerParam + "] Exception:" + e.getMessage());
            }
        }
    }

    public void interrupt() {
        try {
            if (session != null)
                session.close();
            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MyException {
        RemoteExternalCommand rec = new RemoteExternalCommand();
        rec.execute("jzh/qweqwe@192.168.31.250:22", "ls -l");
    }

}
