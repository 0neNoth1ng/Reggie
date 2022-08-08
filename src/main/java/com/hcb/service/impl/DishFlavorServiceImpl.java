package com.hcb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.entity.DishFlavor;
import com.hcb.mapper.DishFlavorMapper;
import com.hcb.service.DishFlavorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
