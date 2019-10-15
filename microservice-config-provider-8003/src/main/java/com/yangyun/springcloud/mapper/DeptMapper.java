package com.yangyun.springcloud.mapper;

import com.yangyun.springcloud.entities.Dept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yangyun
 * @create 2019-09-03-10:14
 */
@Mapper
public interface DeptMapper {
    public boolean addDept(Dept dept);

    public Dept findById(Long id);

    public List<Dept> findAll();
}
