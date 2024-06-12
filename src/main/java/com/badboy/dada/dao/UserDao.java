package com.badboy.dada.dao;

import com.badboy.dada.mapper.UserMapper;
import com.badboy.dada.model.entity.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/5/30 21:54
 */
@Component
public class UserDao extends ServiceImpl<UserMapper, User> {

}
