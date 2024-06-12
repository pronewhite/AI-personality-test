package com.badboy.dada.strategy;

import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.entity.UserAnswer;
import org.springframework.stereotype.Component;
import com.badboy.dada.strategy.ScoreStragetyConfig;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/7 14:38
 */
@Component
public class ScoreStrategyContext {

    @Resource
    List<JudgeScore> judgeScoreList;

    @Nullable
    public UserAnswer judge(UserAnswer userAnswer, App app) {
        for (JudgeScore judgeScore : judgeScoreList) {
            if(judgeScore.getClass().isAnnotationPresent(ScoreStragetyConfig.class)){
                ScoreStragetyConfig scoreStragetyConfig = judgeScore.getClass().getAnnotation(ScoreStragetyConfig.class);
                Integer appType = app.getAppType();
                Integer appScoringStrategy = app.getScoringStrategy();
                if(scoreStragetyConfig.appType() == appType && scoreStragetyConfig.scoringStragety() == appScoringStrategy){
                    return judgeScore.doScore(userAnswer, app.getId());
                }
            }
        }
        return null;
    }
}
