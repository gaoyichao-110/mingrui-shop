package com.baidu.rabbit.topic;

import com.baidu.rabbit.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @ClassName SendMessage
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/10
 * @Version V1.0
 **/
public class SendMessage {
    //交换机的名称
    private final static String EXCHANGE_NAME = "topic_exchange_test";

    //主函数
    public static void main(String[] arg) throws Exception {
        //获取到链接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        //创建一个通道
        Channel channel = connection.createChannel();//Channel(却闹) 通道 create(/kriˈeɪt/) 创建

        channel.exchangeDeclare(EXCHANGE_NAME,"topic");//exchange(恩克斯觉隐去 ɪksˈtʃeɪndʒ) 交换 Declare(dɪˈkler) 宣告

        String message = "商品删除成功 id:153";
        /*
        * 交换机名称
        * routingkey
        * 一些配置信息
        * 发送的消息*/
        //发送消息了  basic(beɪsɪk) 基本,publish(ˈpʌblɪʃ) 出版
        channel.basicPublish(EXCHANGE_NAME,"goods.delete",null,message.getBytes());
        //channel.basicPublish(EXCHANGE_NAME,"goods.save",null,message.getBytes());
        //channel.basicPublish(EXCHANGE_NAME,"goods.update",null,message.getBytes());

        System.out.println("服务发送消息routingkey :delete" + message);

        channel.close();
        connection.close();
    }
}
