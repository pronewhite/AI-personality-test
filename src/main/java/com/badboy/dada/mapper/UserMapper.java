package com.badboy.dada.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.badboy.dada.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据库操作
 *
 
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User getUserByOpenId(String openId){
        // 根据openId查询用户信息
        return selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getMpOpenId, openId));
    }

    default User getUserByName(String name){
        return selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, name)
                .eq(User::getIsDelete, Boolean.FALSE));
    }

}




