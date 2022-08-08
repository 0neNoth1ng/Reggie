package com.hcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hcb.dto.DishDto;
import com.hcb.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入口味数据，需要同时操作两张表 ==> dish，dishFlavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询 菜品信息 和 菜品对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新口味数据，需要同时操作两张表 ==> dish，dishFlavor
    public void updateWithFlavor(DishDto dishDto);

    public void deleteWithFlavor(List<Long> ids);

}
