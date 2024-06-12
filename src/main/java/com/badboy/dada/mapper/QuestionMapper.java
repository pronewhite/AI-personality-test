package com.badboy.dada.mapper;

import com.badboy.dada.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lenovo
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-05-30 21:50:26
* @Entity generator.domain.Question
*/
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}




