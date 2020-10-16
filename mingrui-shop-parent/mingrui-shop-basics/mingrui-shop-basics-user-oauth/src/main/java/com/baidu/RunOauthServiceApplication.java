package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunOauthServiceApplication
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/15
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan(value = "com.baidu.shop.mapper")
public class RunOauthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunOauthServiceApplication.class);
    }
}
