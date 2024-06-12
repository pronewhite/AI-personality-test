package com.badboy.dada.dao;

import com.badboy.dada.mapper.AppMapper;
import com.badboy.dada.model.entity.App;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/5/30 22:02
 */
@Component
public class AppDao extends ServiceImpl<AppMapper, App>{
}
