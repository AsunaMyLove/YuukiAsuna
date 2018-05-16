package com.jzh.dataSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

/**
 *异常定义
 *
 */
public class DbAccessException extends RuntimeException{
    private static final long serialVersionUID = 100L;
    private Throwable cibException=null;
    private String infoMsg = "->IF";

    public DbAccessException(Throwable e) {
        this.cibException = e;
        e.printStackTrace();
    }

    public DbAccessException(String msg) {
        super(msg);
        this.infoMsg = msg;
    }

    public String getThrowableStackMsg(Throwable e){
        try {
            if(e==null){
                return "";
            }
            StringWriter stringWriter = new StringWriter();
            PrintWriter print = new PrintWriter(stringWriter);
            e.printStackTrace(print);
            return new String(stringWriter.getBuffer());
        } catch (Exception e1) {
            return e.toString();
        }
    }

    public String getMessage() {
        if(this.cibException == null)
            return this.infoMsg;
        return getThrowableStackMsg(this.cibException);
    }

    public String getMessageTitle() {
        return this.infoMsg;
    }

    public String getInfoMsg() {
        return infoMsg;
    }

    public void setInfoMsg(String infoMsg) {
        this.infoMsg = infoMsg;
    }

    /**
     * @return若是SQLException则返回错误号
     */
    public int getSQLErrorCode() {
        return this.cibException instanceof SQLException ? ((SQLException)this.cibException).getErrorCode() : 0;
    }
























}

