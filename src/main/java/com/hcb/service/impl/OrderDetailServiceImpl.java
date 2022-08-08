package com.hcb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.entity.OrderDetail;
import com.hcb.mapper.OrderDetailMapper;
import com.hcb.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
