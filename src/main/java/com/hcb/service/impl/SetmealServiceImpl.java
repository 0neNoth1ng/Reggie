package com.hcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.common.CustomException;
import com.hcb.dto.DishDto;
import com.hcb.dto.SetmealDto;
import com.hcb.entity.Dish;
import com.hcb.entity.DishFlavor;
import com.hcb.entity.Setmeal;
import com.hcb.entity.SetmealDish;
import com.hcb.mapper.SetmealMapper;
import com.hcb.service.SetmealDishService;
import com.hcb.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐，同时保存 套餐 和 菜品的关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

          //给菜品插入字段 --> 套餐的id
        setmealDishes.stream().peek((item) -> item.setSetmealId(setmealDto.getId())).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    //删除套餐，同时删除套餐和菜品的关联数据
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //判断是否停售，只有停售才能删除
            //select count(*) from setmeal where id in ( .. , .. , ..) and status = 1;
            //看有无起售中，有一个都不能删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids)
                .eq(Setmeal::getStatus,1);
        int count = this.count(lqw);

        //不能删除，抛出异常信息
        if(count>0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //可以删除的话，删除套餐数据 --> delete from setmeal where id in ids
        this.removeByIds(ids);

        //删除setmeal_dish表的关联数据 --> delete from setmeal_dish where setmeal_id in (?,?,?)
        LambdaQueryWrapper<SetmealDish> lqwDish = new LambdaQueryWrapper<>();
        lqwDish.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lqwDish);

    }

    //根据id查询 套餐信息 和 套餐 对应的 菜品信息
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //创建DTO
        SetmealDto setmealDto = new SetmealDto();

        //查询菜品基本信息
        Setmeal setmeal = this.getById(id);

        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询 套餐 对应的 菜品 信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> dishes = setmealDishService.list(lqw);

        setmealDto.setSetmealDishes(dishes);
        return setmealDto;
    }

    //更新套餐信息，同时更新 菜品 数据，需要同时操作两张表 ==> seteaml，setmeal_dish
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal套餐数据基本信息
        this.updateById(setmealDto);

        //先清理当前 套餐 的 菜品关联数据 --> setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);

        //添加当前提交过来的 菜品关联数据 --> setmeal_dish表的insert操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();

        Long setmealId = setmealDto.getId();

        //给他们赋上setmeal_id
        dishes = dishes.stream().map((item) ->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        //保存 菜品关联数据 到 套餐菜品 表setmeal_dish  (批量保存)
        setmealDishService.saveBatch(dishes);

    }
}
