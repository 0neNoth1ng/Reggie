package com.hcb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hcb.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
