package com.baidu.rabbit.topic;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @ClassName ReceiveOne
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/10
 * @Version V1.0
 **/
//receive( /rɪˈsiːv/) 收到
public class ReceiveOne {

    //交换机名称
    private final static String EXCHANGE_NAME = "topic_exchange_test";

    private final static String QUEUE_NAME = "topic_exchange_queue";

    public static void main(String[] arg) throws Exception {
        Connection connection = RabbitmqConnectionUtil.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

        //将消息队列绑定到交换机
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,"goods.*");//bind(白的) 绑定



        
    }
}
