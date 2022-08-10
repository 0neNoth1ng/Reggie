package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcb.common.R;
import com.hcb.entity.Category;
import com.hcb.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* 分类管理
* */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    public CategoryService categoryService;

    @Autowired
    public RedisTemplate redisTemplate;

    /**
    * 新增分类
    * */
    @PostMapping
    public R<String > save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);

        //修改了之后  清理  特定菜品的   缓存数据
        //动态生成key
        String key = "dish_" + category.getId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增分类成功！");
    }

    /**
    * 分页分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //创建分页构造器对象
        Page<Category> pageInfo = new Page(page, pageSize);
        //创建条件构造器(排序用)
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
            //添加排序条件，根据sort进行排序
        lqw.orderByAsc(Category::getSort);

        //进行分页查询
        categoryService.page(pageInfo, lqw);

        return R.success(pageInfo);
    }

    /**
    * 删除分类
    * */
    @DeleteMapping
    public R<String > delete(Long ids){
        log.info("删除分类，id为：{}",ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 根据Id修改分类信息
     * */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据 ==> 查询 菜品 和 套餐(下拉框的显示数据)
     * */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        //添加条件
        lqw.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询
        List<Category> list = categoryService.list(lqw);
        return R.success(list);
    }

}
