package com.yangyun.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.yangyun.springcloud.entity.Dept;
import com.yangyun.springcloud.feign.ConsumerFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yangyun
 * @Description:
 * @date 2020/10/20 9:55
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerFeign consumerFeign;

    @HystrixCommand(fallbackMethod = "defaultStores")
    @GetMapping("/list")
    public void select() {
        List<Dept> select = consumerFeign.select();
        System.out.println(select);
    }

    @GetMapping("/get/{id}")
    public Dept get(@PathVariable("id") Long id)
    {
        return consumerFeign.get(id);
    }

    public void defaultStores(){
        System.out.println("======");
    }
}
