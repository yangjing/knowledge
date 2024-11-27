package me.yangbajing.share2016.common.exception;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public class SNotFoundException extends SBaseException {
    public SNotFoundException(String errmsg) {
        super(errmsg, 404);
    }

    public SNotFoundException(String errmsg, Throwable cause) {
        super(errmsg, 404, cause);
    }
}
