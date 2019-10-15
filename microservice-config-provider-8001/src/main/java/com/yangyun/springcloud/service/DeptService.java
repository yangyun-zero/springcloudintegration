package com.yangyun.springcloud.service;

import com.yangyun.springcloud.entities.Dept;

import java.util.List;

/**
 * @author yangyun
 * @create 2019-09-03-10:17
 */
public interface DeptService {
    public boolean add(Dept dept);
    public Dept get(Long id);
    public List<Dept> list();
}