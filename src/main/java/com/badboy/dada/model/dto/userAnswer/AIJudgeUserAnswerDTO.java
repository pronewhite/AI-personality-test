package com.badboy.dada.model.dto.userAnswer;

import lombok.Data;

import java.util.List;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/9 10:17
 */
@Data
public class AIJudgeUserAnswerDTO {

    private String appName;

    private String appDesc;

    private List<Answer> answerList;

    @Data
    public static class Answer{

        private String title;

        private String answer;
    }
}
