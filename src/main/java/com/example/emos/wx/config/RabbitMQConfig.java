package com.example.emos.wx.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: itxiaohao
 * @date: 2023-06-07 21:53
 * @Description: 同步RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public ConnectionFactory connectionFactory(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("8.130.128.164");
        factory.setPort(5672);
        return factory;
    }
}
