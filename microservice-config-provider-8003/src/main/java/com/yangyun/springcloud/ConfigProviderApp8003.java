package com.yangyun.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName DeptProvider8001_App
 * @Description:
 * @Author yangyun
 * @Date 2019/9/3 0003 10:20
 * @Version 1.0
 **/
@SpringBootApplication
@EnableEurekaClient // 本服务启动后会自动注册到 eureka 服务中心
@EnableDiscoveryClient // 服务发现
public class ConfigProviderApp8003 {

    public static void main(String[] args) {
        SpringApplication.run(ConfigProviderApp8003.class, args);
    }
}
