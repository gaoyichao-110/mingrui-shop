package com.baidu.rabbit.work;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName ReceiveTwo
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class ReceiveTwo {
    private final static String QUEUE_NAME = "teet_work_queue";

    public static void main(String[] arg) throws Exception {
        Connection connection = RabbitmqConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body： 消息中参数信息
                String msg = new String(body);
                System.out.println(" [消费者-2] 收到消息 : " + msg);
                //System.out.println(1/0);

                try {
                    //增加消费者2消费消息的时间
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
