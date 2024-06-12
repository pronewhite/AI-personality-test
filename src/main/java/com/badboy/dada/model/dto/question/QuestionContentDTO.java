package com.badboy.dada.model.dto.question;

import lombok.*;

import java.util.List;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/4 19:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContentDTO {

    private String title;
    private List<Option> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Option {
        private String result; // 如果是测评类，那么再 result 中记录答案属性
        private int score;// 如果是得分类，那么在score 中记录分数
        private String value;
        private String key;
    }
}
