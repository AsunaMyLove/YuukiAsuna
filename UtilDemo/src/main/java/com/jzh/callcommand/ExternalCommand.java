package com.jzh.callcommand;

import java.io.IOException;

import com.jzh.exception.MyException;
import org.apache.log4j.Logger;

/**
 * 	用于处理本地命令
 *
 */
public class ExternalCommand {
    private static Logger logger = Logger.getLogger(ExternalCommand.class);
    private Process process = null;

    public void execute(String command) throws MyException {
        Runtime run = Runtime.getRuntime();//返回与当前Java 应用程序相关的运行时对象

        try {
            logger.debug("execute command:"+command);
            //启动另一个进程来执行命令
            process = run.exec(command.toString());
            StringBuffer builder = new StringBuffer();
            RuntimeExecuteStream errStream = new RuntimeExecuteStream(process.getErrorStream(), true);
            errStream.setReturnInfo(builder);
            errStream.start();
            RuntimeExecuteStream outStream = new RuntimeExecuteStream(process.getErrorStream(), false);
            outStream.setReturnInfo(builder);
            outStream.start();

            //检查命令是否执行失败
            String errorInfo = null;
            if(process.waitFor()!=0){
                process.getErrorStream().close();
                process.getInputStream().close();

                // 0表示正常结束，1非正常结束
                int exitValue = process.exitValue();
                errorInfo = builder.toString();
                if(exitValue!=0){
                    throw new MyException(
                            "ExternalCommand error: command="+command+"\n"
                                    +errorInfo);
                }

            }
        } catch (IOException e) {
            throw new MyException(
                    "ExternalCommand error: command="+command+"\n"
                            +e.getMessage());
        } catch (InterruptedException e) {
            throw new MyException(
                    "ExternalCommand error: command="+command+"\n"
                            +e.getMessage());
        }finally{
            try {
                if(process!=null)
                    process.destroy();
            } catch (Exception e) {
                throw new MyException(
                        "ExternalCommand error: command="+command+"\n"
                                +e.getMessage());
            }
        }
    }

    public void interrupt(){
        try {
            if(process!=null)
                process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

