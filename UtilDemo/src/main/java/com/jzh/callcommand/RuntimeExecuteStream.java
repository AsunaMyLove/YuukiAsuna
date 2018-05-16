package com.jzh.callcommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 *	用于执行外部命令是，处理Runtime.getRuntime().exec产生的错误流及输出流
 *
 */
public class RuntimeExecuteStream extends Thread {
    private static Logger logger = Logger.getLogger(RuntimeExecuteStream.class);

    private static final int BufferMaxLength = 3900;

    private InputStream is;
    private boolean isErrorStream;
    private OutputStream os;
    private boolean isReturnInfo = false;
    private StringBuffer returnInfo; 	//StringBuffer为线程安全的

    public RuntimeExecuteStream(InputStream is, boolean isErrorStream) {
        this(is, isErrorStream, null);
    }

    public RuntimeExecuteStream(InputStream is, boolean isErrorStream,
                                OutputStream os) {
        this.is = is;
        this.isErrorStream = isErrorStream;
        this.os = os;
    }

    public void setReturnInfo(StringBuffer returnInfo) {
        this.returnInfo = returnInfo;
        this.isReturnInfo = true;
    }

    public void run(){
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            if(os != null)
                pw = new PrintWriter(os);

            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;
            int len = 0;
            while((line = br.readLine())!=null){
                if(pw!=null)
                    pw.println(line);
                if(isErrorStream)
                    logger.error("exec command>"+line);
                else{
                    logger.info("exec command>"+line);
                }
                if(isReturnInfo){
                    returnInfo.append(line);
                    returnInfo.append("\r\n");

                    //当超出日志所能写入的长度时，截断前面的字符，仅保留后面的日志
                    len = returnInfo.length();
                    if(len>BufferMaxLength)
                        returnInfo.delete(0, len-BufferMaxLength-1000);
                }
            }
        } catch (IOException e) {
            logger.fatal("[1]Runtime.getRuntime().exec fatal:" + e.toString());
        }finally{
            try {
                if(br!=null)
                    br.close();
                if(isr!=null)
                    isr.close();
                if(pw!=null){
                    pw.flush();
                    pw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("[1]Runtime.getRuntime().exec warn:" + e.toString());
            }
        }
    }





















}

