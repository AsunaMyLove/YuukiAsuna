package com.jzh.exception;

/**
 * 	自定义一个统一使用的异常类
 *
 */
public class MyException extends Exception {

        private static final long serialVersionUID = -2644804173863488706L;

        public MyException(){
            super();
        }

        public MyException(String message, Throwable cause) {
            super(message, cause);
        }

        public MyException(String message) {
            super(message);
        }

        public MyException(Throwable cause) {
            super(cause.getMessage(),cause);
        }

}
