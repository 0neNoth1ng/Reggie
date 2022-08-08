package com.hcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcb.common.BaseContext;
import com.hcb.common.R;
import com.hcb.entity.ShoppingCart;
import com.hcb.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
    * 添加购物车
    * */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        //设置用户id，指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询当前 菜品 或 套餐 是否已经在购物车中，       如果存在，在数据库中number + 1就行
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        if(dishId != null){
            //添加到购物车的是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //SQL:select * from shopping_cart where user_id = ? and dish_id = ?/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

        if(cartServiceOne != null){
            //存在购物车，在原来的数量基础上 + 1
            cartServiceOne.setNumber(cartServiceOne.getNumber() + 1);
            cartServiceOne.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //不存在购物车里面，则添加到购物车里面，默认数量是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId())
           .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }

    /**
     * 清空购物车
     * */
    @DeleteMapping("/clean")
    public R<String > clean(){
        //SQL: delete * from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);
        return R.success("删除成功");
    }



    @PostMapping("/sub")
    public R<List<ShoppingCart>> sub(@RequestBody ShoppingCart shoppingCart){
        Long userId = BaseContext.getCurrentId();
        //查询当前 菜品 或 套餐 是否已经在购物车中，如果存在，在数据库中number + 1就行
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);

        if(dishId != null){
            //减少的购物车的是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //减少的购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartUpdate = shoppingCartService.getOne(lqw);

        //判断购物车是否只有一个东西了
        int count = shoppingCartUpdate.getNumber();
        if(count == 1){
            //只有一个东西，清空此项，而不是number = 0
            shoppingCartService.remove(lqw);

        }else {
            //将其number - 1
            shoppingCartUpdate.setNumber(shoppingCartUpdate.getNumber() - 1);
            shoppingCartService.updateById(shoppingCartUpdate);
        }
        List<ShoppingCart> list = shoppingCartService.list();

        return R.success(list);
    }
}
