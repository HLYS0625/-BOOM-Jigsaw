package com.cugb.xiaob.mozaiku;

/**
 * Created by xiaob on 2017/7/16.
 * 邮件发送认证
 */

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
public class MyAuthenticator extends Authenticator {
    String userName = null;
    String password = null;
    public MyAuthenticator() {
    }
    public MyAuthenticator(String username, String password) {
        this.userName = username;
        this.password = password;
    }
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}