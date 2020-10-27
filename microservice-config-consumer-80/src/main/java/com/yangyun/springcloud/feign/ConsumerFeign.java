package com.yangyun.springcloud.feign;

import com.yangyun.springcloud.entity.Dept;
import com.yangyun.springcloud.fallback.ConsumerFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author yangyun
 * @Description:
 * @date 2020/10/20 9:57
 */
@Component
@FeignClient(name = "microservicecloud-config-provider-cilent-dept", path = "/dept", url = "localhost:8002", fallback = ConsumerFallBack.class)
public interface ConsumerFeign {

    @PostMapping("/list")
    List<Dept> select();

    @GetMapping("/get/{id}")
    Dept get(Long id);
}
