package com.spiritdata.dataanal.exceptionC;

/**
 * DataAnal访问日志信息不规范，内部码为1202，基本信息为'访问日志信息不规范'
 * @author wh
 */
public class Dtal1202CException extends DtalCException {
    private static final long serialVersionUID = 2028056374348414941L;

    private static String myBaseMsg = "访问日志信息不规范";
    private static int myCode = 1202;

    /**
     * 构造没有详细消息内容的——'访问日志信息不规范'异常
     */
    public Dtal1202CException() {
        super(myCode, myBaseMsg);
    }

    /**
     * 构造有详细消息内容的——'访问日志信息不规范'异常
     * @param message 详细消息
     */
    public Dtal1202CException(String msg) {
        super(myCode, myBaseMsg, msg);
    }

    /**
     * 根据指定的原因和(cause==null?null:cause.toString())的详细消息构造新——'访问日志信息不规范'异常
     * @param cause 异常原因，以后通过Throwable.getCause()方法获取它。允许使用null值，指出原因不存在或者是未知的异常
     */
    public Dtal1202CException(Throwable cause) {
        super(myCode, myBaseMsg, cause);
    }

    /**
     * 根据指定的原因和(cause==null?null:cause.toString())的详细消息，以及详细消息构造新——'访问日志信息不规范'异常
     * @param message 详细消息
     * @param cause 异常原因，以后通过Throwable.getCause()方法获取它。允许使用null值，指出原因不存在或者是未知的异常
     */
    public Dtal1202CException(String msg, Throwable cause) {
        super(myCode, myBaseMsg, msg, cause);
    }

    public Dtal1202CException(String msg, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(myCode, myBaseMsg, msg, cause, enableSuppression, writableStackTrace);
    }
}