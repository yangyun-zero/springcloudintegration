package com.yangyun.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author yangyun
 * @Description:
 * @date 2020/10/20 9:33
 */
@SpringBootApplication
@EnableEurekaClient // 本服务启动后会自动注册到 eureka 服务中心
@EnableDiscoveryClient // 服务发现
@EnableFeignClients("com.yangyun.*") // feign 远程调用  com.yangyun.springcloud.feign.ConsumerFeign
@EnableCircuitBreaker // 开启 Hystrix 熔断器
@EnableHystrixDashboard
public class ConsumerMain_80 {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerMain_80.class);
    }
}
