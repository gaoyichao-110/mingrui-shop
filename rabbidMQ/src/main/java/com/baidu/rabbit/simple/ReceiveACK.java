package com.baidu.rabbit.simple;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;


import java.io.IOException;

/**
 * @ClassName ReceiveACK
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class ReceiveACK {

    //创建队列名
    private final static String QUEUE_NAME = "simple_queue";

    public static void main(String arg) throws Exception {
        //获取链接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        //创建通道
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //定义对列 接收端==>消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            //监听队列中的消息,如果有消息,进行处理
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("收到消息,执行中:"+msg+"!");
                /*
                * param1:唯一标识Id
                * param2:是否进行批处理
                * */
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
