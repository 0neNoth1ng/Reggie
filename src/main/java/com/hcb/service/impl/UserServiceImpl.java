package com.hcb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.entity.User;
import com.hcb.mapper.UserMapper;
import com.hcb.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
