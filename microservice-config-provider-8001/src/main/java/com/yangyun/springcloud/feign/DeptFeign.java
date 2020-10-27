package com.yangyun.springcloud.feign;

import com.yangyun.springcloud.entities.Dept;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author yangyun
 * @Description:
 * @date 2020/10/20 13:57
 */
@Component
@FeignClient(name = "microservicecloud-config-provider-cilent-dept",
        path = "/dept",
        url = "")
public interface DeptFeign {

    @PostMapping("/list")
    List<Dept> list();
}
