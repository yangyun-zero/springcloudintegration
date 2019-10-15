package com.yangyun.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @ClassName ConfigEurekaServerApp7001
 * @Description:
 * @Author yangyun
 * @Date 2019/9/16 0016 10:07
 * @Version 1.0
 **/
@SpringBootApplication
@EnableEurekaServer  // 作为 eureka server
public class ConfigEurekaServerApp7001 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigEurekaServerApp7001.class, args);
    }
}
