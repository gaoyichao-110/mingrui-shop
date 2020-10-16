package com.baidu.rabbit.fanout;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @ClassName SendMessage
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class SendMessage {
    private final static String EXCHANGE_NAME = "fanout_exchange_test";

    public static void main(String[] arg) throws Exception {
        Connection connection = RabbitmqConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
         /*
        param1: 交换机名称
        param2: 交换机类型
         */
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        //发送的消息内容
        String message = "good good study";

        /*
        param1: 交换机名称
        param2: routingKey
        param3: 一些配置信息
        param4: 发送的消息
         */
        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

        System.out.println(" 消息发送 '" + message + "' 到交换机 success");
        // 关闭通道和连接
        channel.close();
        connection.close();
    }
}
