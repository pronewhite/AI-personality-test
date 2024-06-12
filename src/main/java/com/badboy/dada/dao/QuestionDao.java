package com.badboy.dada.dao;

import com.badboy.dada.mapper.QuestionMapper;
import com.badboy.dada.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/5/30 22:06
 */
@Component
public class QuestionDao extends ServiceImpl<QuestionMapper, Question>{
}
