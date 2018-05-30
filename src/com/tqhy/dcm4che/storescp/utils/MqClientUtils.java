package com.tqhy.dcm4che.storescp.utils;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author Yiheng
 * @create 2018/5/22
 * @since 1.0.0
 */
public class MqClientUtils {
    Connection connection = null; // Session： 一个发送或接收消息的线程
    Session session; // Destination ：消息的目的地;消息发送给谁.
    private static MqClientUtils mqClient = null;

    //静态工厂方法
    public static MqClientUtils getMqClient() {
        if (mqClient == null) {
            mqClient = new MqClientUtils();
        }
        return mqClient;
    }

    private MqClientUtils() {
        // ConnectionFactory ：连接工厂，JMS 用它创建连接
        ConnectionFactory connectionFactory; // Connection ：JMS 客户端到JMS
        // Provider 的连接

        // 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
        connectionFactory = new ActiveMQConnectionFactory(
                "tqhy",
                "tqhy817@2017", "tcp://192.168.1.244:61686");
        try { // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void sendMessage(String msg, String queueName) {
        try {
            Destination destination; // MessageProducer：消息发送者
            MessageProducer producer; // TextMessage message;
            // 获取session注意参数值，须在在ActiveMq的console配置
            destination = session.createQueue(queueName);
            // 得到消息生成者【发送者】
            producer = session.createProducer(destination);
            // 设置不持久化，此处学习，实际根据项目决定
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            // 构造消息，此处写死，项目就是参数，或者方法获取
            TextMessage message = session.createTextMessage(msg);
            // 发送消息到目的地方
            producer.send(message);
            System.out.println(getClass().getSimpleName() + " sendMsg: " + msg);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (null != connection)
                connection.close();
        } catch (Throwable ignore) {
        }
    }
}
