package com.hcb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.entity.ShoppingCart;
import com.hcb.mapper.ShoppingCartMapper;
import com.hcb.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
