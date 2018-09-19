package com.tqhy.dcm4che.storescp.utils;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author Yiheng
 * @create 2018/9/11
 * @since 1.0.0
 */
public class RabbitMqClientUtils {
    Connection connection = null;
    Channel channel;
    private static RabbitMqClientUtils mqClient = null;

    //静态工厂方法
    public static RabbitMqClientUtils getMqClient() {
        if (mqClient == null) {
            mqClient = new RabbitMqClientUtils();
        }
        return mqClient;
    }

    private RabbitMqClientUtils() {
        ConnectionFactory connFactory = new ConnectionFactory();
        connFactory.setHost("192.168.1.160");
        connFactory.setPort(5672);
        connFactory.setUsername("tqhy");
        connFactory.setPassword("tqhy817@2017");
        try { // 构造从工厂得到连接对象
            connection = connFactory.newConnection();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void sendMessage(String msg, String queueName) {
        try {
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicPublish("", queueName, null, msg.getBytes(StandardCharsets.UTF_8));
            System.out.println(getClass().getSimpleName() + " sendMsg: " + msg);
           // channel.basicConsume(queueName,true,)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (null != channel) {
                channel.close();
            }

            if (null != connection) {
                connection.close();
            }
        } catch (Throwable ignore) {
        }
    }
}
