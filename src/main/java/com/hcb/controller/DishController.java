package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcb.common.R;
import com.hcb.dto.DishDto;
import com.hcb.entity.Category;
import com.hcb.entity.Dish;
import com.hcb.entity.DishFlavor;
import com.hcb.entity.Employee;
import com.hcb.service.CategoryService;
import com.hcb.service.DishFlavorService;
import com.hcb.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品，具体的代码写在了业务层里面，同时操作两张表，菜品表和口味表
     */
    @PostMapping
    public R<String> saveDish(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品的分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();


        //构造条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper();

        //添加过滤条件 ==> 根据菜名搜索 和 根据更新时间排序
        lqw.like(!StringUtils.isEmpty(name), Dish::getName, name)
                .orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo, lqw);

        //通过对象拷贝,忽略records
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //把普通属性拷贝到dishDto里面
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//每个菜品的 分类Id
            //根据分类id 查询分类对象，主要是为了获取分类的名字
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //目的达成
            dishDto.setCategoryName(categoryName);

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    @Transactional
    public R<DishDto> getOne(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 根据id修改菜品的信息
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);

        //修改了之后  清理  特定菜品的   缓存数据
            //动态生成key
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("菜品信息更新成功");
    }

    /**
     * 起售 和 停售 状态的更新
     */
    @PostMapping("/status/{status}")
    public R<String> statusUpdate(@PathVariable int status, Long ids[]) {

        for (Long id : ids) {

            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);

            //停售起售同时 删除 缓存数据 ，避免前端页面失误
            String key = "dish_" + dish.getCategoryId() + "_1";
            redisTemplate.delete(key);
        }

        return R.success("状态修改成功");
    }

    /**
     * 菜品的删除
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功");
    }

    /**
     * 在新建套餐页面时， 添加相应的 菜品 需要查询数据，从这里查
     */
    @GetMapping("/list")
    public R<List<DishDto>> get(Dish dish) {

        List<DishDto> dishDtoList = null;

        //动态生成key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        //先从Redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //添加条件 ==> 只查询起售状态的菜品 包含排序
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //把普通属性拷贝到dishDto里面
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//每个菜品的 分类Id
            //根据分类id 查询分类对象，主要是为了获取分类的名字
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //目的达成
            dishDto.setCategoryName(categoryName);

            //当前菜品的Id
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> lqwFlavor = new LambdaQueryWrapper<>();
            lqwFlavor.eq(DishFlavor::getDishId, id);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lqwFlavor);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，查询数据库，如果不存在，将查询的数据缓存进Redis,60分钟到期
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }
}
