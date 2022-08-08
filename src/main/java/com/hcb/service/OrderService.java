package com.hcb.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hcb.entity.Orders;

public interface OrderService extends IService<Orders> {

    //用户下单
    public void submit(Orders orders);
}
