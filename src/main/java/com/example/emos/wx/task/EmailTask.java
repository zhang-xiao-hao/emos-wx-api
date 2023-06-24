package com.example.emos.wx.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: itxiaohao
 * @date: 2023-06-02 16:28
 * @Description: 邮件异步发送任务类
 */
@Component
@Scope("prototype")
public class EmailTask {
    @Resource
    private JavaMailSender javaMailSender;
    @Value("${emos.email.system}")
    private String mailbox;
    @Async //异步方法
    public void sendAsync(SimpleMailMessage message){
        message.setFrom(mailbox);
        javaMailSender.send(message);
    }
}
