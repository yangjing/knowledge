package me.yangbajing.share2016.common.exception;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public class SBadRequestException extends SBaseException {
    SBadRequestException(String errmsg) {
        super(errmsg, 400);
    }

    SBadRequestException(String errmsg, int errcode) {
        super(errmsg, errcode);
    }
}
