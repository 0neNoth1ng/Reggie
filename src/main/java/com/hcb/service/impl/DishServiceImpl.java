package com.hcb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.common.CustomException;
import com.hcb.dto.DishDto;
import com.hcb.entity.Dish;
import com.hcb.entity.DishFlavor;
import com.hcb.entity.Setmeal;
import com.hcb.entity.SetmealDish;
import com.hcb.mapper.DishMapper;
import com.hcb.service.DishFlavorService;
import com.hcb.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     *  新增菜品，同时插入口味数据，需要同时操作两张表 ==> dish，dishFlavor
     * */
    @Override
    //同时操作两张表，要开启事务，同成功，同失败
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //菜品Id
        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //给他们赋上dish_id
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_Flavor  (批量保存)
        dishFlavorService.saveBatch(flavors);
    }

    //根据id查询 菜品信息 和 菜品对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //创建DTO
        DishDto dishDto = new DishDto();

        //查询菜品基本信息
        Dish dish = this.getById(id);

        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);
        return dishDto;
    }

    //更新菜品信息，同时更新口味数据，需要同时操作两张表 ==> dish，dishFlavor
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish基本信息
        this.updateById(dishDto);

        //先清理当前菜品的口味数据 --> dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);

        //添加当前提交过来的口味数据 --> dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        Long dishId = dishDto.getId();

        //给他们赋上dish_id
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_Flavor  (批量保存)
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        //判断是否停售，只有停售才能删除
        //select count(*) from dish where id in ( .. , .. , ..) and status = 1;
        //看有无起售中，有一个都不能删除
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids)
                .eq(Dish::getStatus,1);
        Long count = this.count(lqw);

        //不能删除，抛出异常信息
        if(count>0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }

        //可以删除的话，删除菜品数据 --> delete from dish where id in ids
        this.removeByIds(ids);

        //删除dish_flavor表的关联数据 --> delete from dish_flavor where dish_id in (?,?,?)
        LambdaQueryWrapper<DishFlavor> lqwFlavor = new LambdaQueryWrapper<>();
        lqwFlavor.in(DishFlavor::getDishId,ids);

        dishFlavorService.remove(lqwFlavor);

    }
}
