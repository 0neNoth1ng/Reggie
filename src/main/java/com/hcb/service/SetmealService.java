package com.hcb.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hcb.dto.SetmealDto;
import com.hcb.entity.Setmeal;
import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时保存 套餐 和 菜品的关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);

    //根据id查询 套餐信息 和 套餐 对应的 菜品信息
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐信息，同时更新 菜品 数据，需要同时操作两张表 ==> seteaml，setmeal_dish
    public void updateWithDish(SetmealDto setmealDto);


}
