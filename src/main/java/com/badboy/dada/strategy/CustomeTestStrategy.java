package com.badboy.dada.strategy;

import cn.hutool.json.JSONUtil;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.QuestionDao;
import com.badboy.dada.dao.ScoringResultDao;
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
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author badboy
 * @version 1.0
 * 自定义人格测试
 * Create by 2024/6/5 15:18
 */
@ScoreStragetyConfig(appType = 1)
public class CustomeTestStrategy implements JudgeScore {

    @Autowired
    private AppDao appDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ScoringResultDao scoringResultDao;

    @Override
    public UserAnswer doScore(UserAnswer userAnswer, Long appId) {
        AssertUtil.isEmpty(appId, "appId不能为空");
        Map<String, Integer> scoreMap = new HashMap<>();
        // 取出所有的 得分结果，并且根据 scoreRange 降序排序
        List<ScoringResult> scoringResults = scoringResultDao.list(new LambdaQueryWrapper<ScoringResult>()
                .eq(ScoringResult::getAppId, appId));
        // 1. 首先通过appId 拿到 app 信息
        App app = appDao.getById(appId);
        // 2. 然后取出 app 对应的问题
        Question question = questionDao.getOne(new LambdaQueryWrapper<Question>().eq(Question::getAppId, appId));
        AssertUtil.isEmpty(question, "app 对应的问题不存在");
        // 3. 然后根据用户答案统计答案情况
        UserAnswerVO objUserAnswer = UserAnswerVO.objToVo(userAnswer);
        List<List<String>> choices = objUserAnswer.getChoices();
        QuestionVO questionVO = QuestionVO.objToVo(question);
        for (int i = 0; i < questionVO.getQuestionContent().size(); i++) {
            QuestionContentDTO questionContentDTO = questionVO.getQuestionContent().get(i);
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                List<String> strings = choices.get(i);
                if(strings.contains(option.getKey())){
                    // 答案匹配
                    String result = option.getResult();
                    scoreMap.put(result, scoreMap.getOrDefault(result, 0) + 1);
                }
            }
        }
        // 4. 然后根据得分找到最合适的人格结果
        ScoringResult finalResult = scoringResults.get(0);
        int maxScore = 0;
        for (ScoringResult scoringResult : scoringResults) {
            String resultProp = scoringResult.getResultProp();
            List<String> propList = JSONUtil.toList(resultProp, String.class);
            int sum = propList.stream().mapToInt(prop -> scoreMap.getOrDefault(prop, 0))
                    .sum();
            if (sum > maxScore) {
                maxScore = sum;
                finalResult = scoringResult;
            }
        }
        // 5. 然后返回 用户答案
        UserAnswer finalUserAnswer = new UserAnswer();
        finalUserAnswer.setId(userAnswer.getId());
        finalUserAnswer.setAppId(appId);
        finalUserAnswer.setAppType(app.getAppType());
        finalUserAnswer.setScoringStrategy(app.getScoringStrategy());
        finalUserAnswer.setChoices(userAnswer.getChoices());
        finalUserAnswer.setResultId(finalResult.getId());
        finalUserAnswer.setResultName(finalResult.getResultName());
        finalUserAnswer.setResultDesc(finalResult.getResultDesc());
        finalUserAnswer.setResultPicture(finalResult.getResultPicture());
        return finalUserAnswer;
    }
}
