package com.badboy.dada.strategy;

import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.QuestionDao;
import com.badboy.dada.dao.ScoringResultDao;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.model.dto.question.QuestionContentDTO;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.entity.Question;
import com.badboy.dada.model.entity.ScoringResult;
import com.badboy.dada.model.entity.UserAnswer;
import com.badboy.dada.model.vo.QuestionVO;
import com.badboy.dada.model.vo.UserAnswerVO;
import com.badboy.dada.utils.AssertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * @author badboy
 * @version 1.0
 * 非人格测试类题目统计得分
 * Create by 2024/6/5 15:18
 */
@ScoreStragetyConfig
public class CustomeScoreStrategy implements JudgeScore {

    @Autowired
    private AppDao appDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ScoringResultDao scoringResultDao;

    @Override
    public UserAnswer doScore(UserAnswer userAnswer, Long appId) {
        ThrowUtils.throwIf(appId== null, ErrorCode.PARAMS_ERROR);
        // 1. 首先通过appId 拿到 app 信息
        App app = appDao.getById(appId);
        // 2. 然后取出 app 对应的问题
        Question question = questionDao.getOne(new LambdaQueryWrapper<Question>().eq(Question::getAppId, appId));
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // 3. 然后根据用户答案统计得分
        UserAnswerVO objUserAnswer = UserAnswerVO.objToVo(userAnswer);
        List<List<String>> choices = objUserAnswer.getChoices();
        QuestionVO questionVO = QuestionVO.objToVo(question);
        int totalScore = 0;
        int gainedScore = 0;
        List<List<String>> correctAnswers = new ArrayList<>();
        for (int i = 0; i < questionVO.getQuestionContent().size(); i++) {
            QuestionContentDTO questionContentDTO = questionVO.getQuestionContent().get(i);
            List<String> temp = new ArrayList<>();
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                // 题目有可能是多选
                List<String> choicesList = choices.get(i);
                if(choicesList.contains(option.getKey())){
                    int score = Optional.ofNullable(option.getScore()).orElse(0);
                    gainedScore += score;
                }
                // 记录正确答案
                if(option.getScore() != 0){
                    temp.add(option.getKey());
                }
                totalScore += option.getScore();
            }
            correctAnswers.add(temp);
        }
        float score = (float) gainedScore / totalScore;
        // 5. 然后返回 用户答案
        UserAnswer finalUserAnswer = new UserAnswer();
        finalUserAnswer.setId(userAnswer.getId());
        finalUserAnswer.setAppId(appId);
        finalUserAnswer.setAppType(app.getAppType());
        finalUserAnswer.setScoringStrategy(app.getScoringStrategy());
        finalUserAnswer.setChoices(userAnswer.getChoices());
        finalUserAnswer.setCorrectAnswers(correctAnswers);
        finalUserAnswer.setResultScore(score);
        return finalUserAnswer;
    }
}
