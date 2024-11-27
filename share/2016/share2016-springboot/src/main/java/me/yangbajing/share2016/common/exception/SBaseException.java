package me.yangbajing.share2016.common.exception;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public abstract class SBaseException extends RuntimeException {
    private final int errcode;

    SBaseException(String errmsg, int errcode) {
        super(errmsg);
        this.errcode = errcode;
    }

    SBaseException(String errmsg, int errcode, Throwable cause) {
        super(errmsg, cause);
        this.errcode = errcode;
    }

    public int getErrcode() {
        return errcode;
    }

    public String getErrmsg() {
        return getMessage();
    }

    @Override
    public String toString() {
        return "SBaseException{" +
                "errcode=" + getErrcode() +
                ", errmsg='" + getErrmsg() + '\'' +
                '}';
    }
}
