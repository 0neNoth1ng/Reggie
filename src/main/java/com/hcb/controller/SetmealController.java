package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcb.common.R;
import com.hcb.dto.DishDto;
import com.hcb.dto.SetmealDto;
import com.hcb.entity.Category;
import com.hcb.entity.Dish;
import com.hcb.entity.Setmeal;
import com.hcb.service.CategoryService;
import com.hcb.service.SetmealDishService;
import com.hcb.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 套餐管理
 * */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 新增套餐
     * */
    @CacheEvict(value = "setmealCache", allEntries = true)
    @PostMapping
    public R<String > save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐 的 分页查询
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<Setmeal>(page,pageSize);

        Page<SetmealDto> setmealDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();

        //添加过滤条件 ==> 根据 套餐名 搜索 和 根据更新时间排序
        lqw.like(!StringUtils.isEmpty(name), Setmeal::getName, name)
                .orderByDesc(Setmeal::getUpdateTime);

        //执行查询
        setmealService.page(pageInfo,lqw);

        //通过对象拷贝,忽略records
        BeanUtils.copyProperties(pageInfo, setmealDtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) ->{
            SetmealDto setmealDto = new SetmealDto();
            //把普通属性拷贝到setmealDto里面
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = item.getCategoryId();//每个套餐的 分类Id
            //根据分类id 查询分类对象，主要是为了获取分类的名字

            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //目的达成
            setmealDto.setCategoryName(categoryName);

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * */
    @DeleteMapping
    public R<String > delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 起售 和 停售 状态的更新
     * */
    @CacheEvict(value = "setmealCache", allEntries = true)
    @PostMapping("/status/{status}")
    public R<String> statusUpdate(@PathVariable int status, Long ids[]){

        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("状态修改成功");
    }

    /**
     * 根据id查询  套餐
     * */
    @GetMapping("/{id}")
    @Transactional
    public R<SetmealDto> getOne(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 套餐的修改
    * */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);

        //修改了之后  清理  特定菜品的   缓存数据
        //动态生成key
        String key = "setmeal_" + setmealDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("修改成功！");
    }

    /**
     * 根据条件查询套餐数据，在移动端展示
     * 这里返回的数据可以给前端页面展示用
     * */
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        List<Setmeal> list = null;
        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId())
                .eq(Setmeal::getStatus,1)//只有起售的菜品才能展示出来
                .orderByDesc(Setmeal::getUpdateTime);
        list = setmealService.list(lqw);

        return R.success(list);
    }
}
