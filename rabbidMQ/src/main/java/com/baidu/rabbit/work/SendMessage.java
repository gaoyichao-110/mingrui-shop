package com.baidu.rabbit.work;

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
    private final static String QUEUE_NAME = "test_work_queue";

    public static void main(String[] arg) throws Exception {
        //获取到链接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        //创建通道
        Channel channel = connection.createChannel();
        /*
        队列名称
        是否持久化
        是否排外
        是否自动删除
        其他参数
         */
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //循环发送消息一百条
        for(int i = 0;i < 100; i++){
            //消息参数内容
            String message = "task-goods study - "+i;
            /*
            * 交换机名称
            * routingkey
            * 一些配置信息
            * 发送的消息
            * */
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
            System.out.println("send"+message+"success");
        }
        channel.close();
    }
}
