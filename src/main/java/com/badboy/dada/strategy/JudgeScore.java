package com.badboy.dada.strategy;

import com.badboy.dada.model.entity.UserAnswer;

public interface JudgeScore {

    /**
     *  进行评分
     * @param userAnswer 用户答案
     * @param appId 应用ID
     * @return
     */
    UserAnswer doScore(UserAnswer userAnswer, Long appId);
}
