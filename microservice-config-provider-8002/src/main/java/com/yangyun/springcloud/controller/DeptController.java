package com.yangyun.springcloud.controller;

import com.yangyun.springcloud.entities.Dept;
import com.yangyun.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName DeptController
 * @Description:
 * @Author yangyun
 * @Date 2019/9/3 0003 10:19
 * @Version 1.0
 **/
@RestController
public class DeptController {

    @Autowired
    private DeptService service;

    @PostMapping("/dept/add")
    public boolean add(@RequestBody Dept dept)
    {
        return service.add(dept);
    }

    @GetMapping("/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id)
    {
        return service.get(id);
    }

    @GetMapping("/dept/list")
    public List<Dept> list()
    {
        return service.list();
    }


    /*用于发现服务*/
    @Autowired
    private DiscoveryClient client;

    public DiscoveryClient getClient (){
        List<String> services = client.getServices();
        return client;
    }

}
