package com.yangyun.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @ClassName ConfigServerCenterApp1000
 * @Description:
 * @Author yangyun
 * @Date 2019/9/16 0016 9:45
 * @Version 1.0
 **/
@SpringBootApplication
@EnableConfigServer // 开始配置中心
public class ConfigServerCenterApp1000 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerCenterApp1000.class, args);
    }
}
