package com.yangyun.springcloud.fallback;

import com.yangyun.springcloud.entity.Dept;
import com.yangyun.springcloud.feign.ConsumerFeign;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yangyun
 * @Description:
 * @date 2020/10/21 10:00
 */
@Component
public class ConsumerFallBack implements ConsumerFeign {
    @Override
    public List<Dept> select() {
        System.out.println("select is failed..");
        return null;
    }

    @Override
    public Dept get(Long id) {
        System.out.println("get is failed..");
        return null;
    }
}
