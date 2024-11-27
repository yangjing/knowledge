package me.yangbajing.share2016.data.domain;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public class ErrResponse {
    private int errcode;
    private String errmsg;

    public ErrResponse() {
    }

    public ErrResponse(int errcode, String errmsg) {
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
