package com.cugb.xiaob.mozaiku;

/**
 * Created by xiaob on 2017/7/16.
 */
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Administrator on 2017/4/10.
 */

public class SendMailUtil {
    //126(NetEase)
    private static final String HOST = "smtp.126.com";
    private static final String PORT = "25";
    private static final String FROM_ADD = "mine_riko_lupin@126.com"; //发送方邮箱
    private static final String FROM_PSW = "linshimima1";//发送方邮箱授权码

    public static void send(String toAdd,String content){
        final MailInfo mailInfo = creatMail(toAdd,content);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendTextMail(mailInfo);
            }
        }).start();
    }

    @NonNull
    private static MailInfo creatMail(String toAdd,String content) {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);
        mailInfo.setMailServerPort(PORT);
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // 你的邮箱地址
        mailInfo.setPassword(FROM_PSW);// 您的邮箱密码
        mailInfo.setFromAddress(FROM_ADD); // 发送的邮箱
        mailInfo.setToAddress(toAdd); // 发到哪个邮件去
        mailInfo.setSubject("感谢您使用找回密码服务"); // 邮件主题
        mailInfo.setContent(content); // 邮件文本
        return mailInfo;
    }
}
