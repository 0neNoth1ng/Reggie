package com.hcb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcb.entity.AddressBook;
import com.hcb.mapper.AddressBookMapper;
import com.hcb.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
