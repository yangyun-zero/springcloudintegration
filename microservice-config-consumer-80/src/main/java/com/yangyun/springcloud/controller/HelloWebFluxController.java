package com.yangyun.springcloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author yangyun
 * @Description: test  webflux
 * @date 2020/10/27 18:18
 */
@RestController
@RequestMapping("/webFlux")
public class HelloWebFluxController {

    @GetMapping("/hello")
    public String hello() {
        Mono<String> just = Mono.just("");
        System.out.println(just);
        return "Hello, WebFlux !";
    }
}
