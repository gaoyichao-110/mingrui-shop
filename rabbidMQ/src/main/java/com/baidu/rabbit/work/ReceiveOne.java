package com.baidu.rabbit.work;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName ReceiveOne
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class ReceiveOne {
    private final static String QUEUE_NAME = "test_work_queue";

    public static void main(String[] arg) throws Exception{
        //获取链接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        //创建通道
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //定义对列消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                     byte[] body) throws IOException {
              String msg = new String(body);
              System.out.println("消费者1收到消息:"+msg);
              channel.basicAck(envelope.getDeliveryTag(),false);
          }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
        //消费者需要时时监听消息，不用关闭通道与连接
    }
}
