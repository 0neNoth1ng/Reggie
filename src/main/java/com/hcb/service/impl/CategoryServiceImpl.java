package com.hcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.common.CustomException;
import com.hcb.common.R;
import com.hcb.entity.Category;
import com.hcb.entity.Dish;
import com.hcb.entity.Setmeal;
import com.hcb.mapper.CategoryMapper;
import com.hcb.service.CategoryService;
import com.hcb.service.DishService;
import com.hcb.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据Id删除分类,删除之前需要进行判断
     * */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了 菜品，如果已经关联，抛出业务异常
        LambdaQueryWrapper<Dish> Dishlqw = new LambdaQueryWrapper<>();
            //添加查询条件，根据 分类 的id（category_id）进行查询
        Dishlqw.eq(Dish::getCategoryId,id);
        Long count1 = dishService.count(Dishlqw);
        if(count1>0){
            //已经关联 菜品，抛出业务异常
            throw new CustomException("当前分类关联了菜品，不能删除");
        }

        //查询当前分类是否关联了 套餐，如果已经关联，抛出业务异常
        LambdaQueryWrapper<Setmeal> SetmealLqw = new LambdaQueryWrapper<>();
            //添加查询条件，根据 分类 的id（category_id）进行查询
        SetmealLqw.eq(Setmeal::getCategoryId,id);
        Long count2 = setmealService.count(SetmealLqw);
        if(count2>0){
            //已经关联 套餐，抛出业务异常
            throw new CustomException("当前分类关联了套餐，不能删除");
        }

        //正常删除
        super.removeById(id);
    }
}
