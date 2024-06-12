package com.badboy.dada.strategy;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.QuestionDao;
import com.badboy.dada.dao.ScoringResultDao;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.manager.AIManager;
import com.badboy.dada.model.dto.question.AIGenerateQuestionDTO;
import com.badboy.dada.model.dto.question.AIJudgeValidResult;
import com.badboy.dada.model.dto.question.QuestionContentDTO;
import com.badboy.dada.model.dto.userAnswer.AIJudgeUserAnswerDTO;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.entity.Question;
import com.badboy.dada.model.entity.ScoringResult;
import com.badboy.dada.model.entity.UserAnswer;
import com.badboy.dada.model.vo.QuestionVO;
import com.badboy.dada.model.vo.UserAnswerVO;
import com.badboy.dada.utils.AssertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.apache.lucene.search.join.JoinUtil;
import org.checkerframework.checker.units.qual.A;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author badboy
 * @version 1.0
 * 智谱 AI 进行人格测试
 * Create by 2024/6/5 15:18
 */
@ScoreStragetyConfig(appType = 1, scoringStragety = 1)
public class AITestStrategy implements JudgeScore {

    @Autowired
    private AppDao appDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private AIManager aiManager;
    @Autowired
    private RedissonClient redissonClient;

    private static final Long expireTime = 1L;

    // 添加一个监听器，监听缓存中的元素
    RemovalListener<String, String> listener = (key, value, cause) ->
            System.out.println("被移除的键：" + key + ", 原因：" + cause);

    /**
     * 缓存题目与用户答案，作用是如果下次遇到了相同题目和相同答案，那么可以直接从缓存中返回结果，而不用再调用 AI 判题
     */
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(expireTime, TimeUnit.DAYS)
            .removalListener(listener)
            .build();


    private static final String REDIS_LOCK_KEY = "AI_LOCK_KEY_";

    private static final String JUDGE_SYSTEM_PROMPT = "你是一个判题专家，现在你要根据以下信息进行判题：\n" +
            "```\n" +
            "【【【用户答案】】】\n" +
            "```\n" +
            "要求：\n" +
            "\t1. 返回的结果必须根据我给你的信息进行判题得到；\n" +
            "\t2. 输入的【【【用户答案】】】是一个 JSON 数组：{\"appName\":\"应用名称\",\"appDesc\":\"应用描述\",\"answers\":[{\"title\":\"题目标题\",\"answer\":\"答案\"},{\"title\":\"题目标题\",\"answer\":\"答案\"}]}，其中 appName 表示的是应用名称，appDesc 表示的是应用描述，answers 表示的是用户的答案集合，title 表示题目标题，answer 表示对应题目的答案\n" +
            "\t3. 返回的结果不能包含序号；\n" +
            "\t4. 返回结果的形式为：\n" +
            "\t```\n" +
            "\t{\n" +
            "\t\"resultName\":\"结果名称\",\n" +
            "\t\"resultDesc\":\"结果描述\"\n" +
            "\t}\n" +
            "\t```\n" +
            "\t其中，resultName必须是 16 种人格中的一种，resultDesc是对resultName 进行的100 到 200 字的描述；\n" +
            "\t5. 不能胡编捏造结果；\n" +
            "\t6. 注意：返回的结果必须是一个 JSON 字符串，不能有除 JSON 字符串以外的文字内容；\n" +
            "在返回结果之前，自己对结果的格式以及内容进行评估分析，保证返回的结果是符合要求的。";

    @Override
    @Nullable
    public UserAnswer doScore(UserAnswer userAnswer, Long appId) {

        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        Question question = questionDao.getOne(new LambdaQueryWrapper<Question>()
                .eq(Question::getAppId, appId));
        App app = appDao.getById(appId);
        // 对于 AI 判题，如果相同题目，相同答案可以查缓存，而不用在调用 AI 去判题，一方面节省 AI tokens，另一方面，可以提高用户体验，缩短响应时间
        // 这里使用 caffeine 来实现本地缓存
        // 构造缓存的 KEY
        String cacheKey = generateCacheKey(appId, userAnswer.getChoices());
        // 尝试从缓存中获取结果
        String aiOldJudgeResult = cache.getIfPresent(cacheKey);
        // 如果缓存中结果不为空，那么可以直接返回结果
        if (aiOldJudgeResult != null) {
            UserAnswer aiJudgeValidResult = JSONUtil.toBean(aiOldJudgeResult, UserAnswer.class);
            userAnswer.setResultName(aiJudgeValidResult.getResultName());
            userAnswer.setResultDesc(aiJudgeValidResult.getResultDesc());
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(userAnswer.getChoices());
            return userAnswer;
        }
        // 如果缓存中没有结果，那么需要调用 AI 来判题，但是这里有一个 缓存击穿的问题，如果同时
        // 有很多请求来访问，那么会频繁调用 AI 进行判题，接口会有崩溃的可能，这里应该使用 分布式锁来保证不会发生击穿
        RLock lock = redissonClient.getLock(REDIS_LOCK_KEY + cacheKey);
        try {
            // 获取锁失败立马返回
            boolean tryLock = lock.tryLock(0, TimeUnit.MILLISECONDS);
            if(tryLock){
                // 1. 根据用户答案调用 AI 来进行人格测试结果返回
                String judgeUserAnswerMessage = generateUserAnswerMessage(userAnswer,appId, question);
                String aiJudgeResult = aiManager.doSyncUnstableRequest(JUDGE_SYSTEM_PROMPT, judgeUserAnswerMessage);
                // 2. 截取 AI 回答中的有效答案
                String finalResult = aiJudgeResult.substring(aiJudgeResult.indexOf("{"), aiJudgeResult.lastIndexOf("}") + 1);
                // 3. 将有效答案转换为 UserAnswer 对象
                UserAnswer validUserAnwser = JSONUtil.toBean(finalResult, UserAnswer.class);
                // 5. 然后返回 用户答案
                UserAnswer finalUserAnswer = new UserAnswer();
                finalUserAnswer.setId(userAnswer.getId());
                finalUserAnswer.setAppId(appId);
                finalUserAnswer.setScoringStrategy(app.getScoringStrategy());
                finalUserAnswer.setAppType(app.getAppType());
                finalUserAnswer.setResultName(validUserAnwser.getResultName());
                finalUserAnswer.setCreateTime(new Date());
                finalUserAnswer.setUpdateTime(new Date());
                finalUserAnswer.setResultDesc(validUserAnwser.getResultDesc());
                finalUserAnswer.setChoices(userAnswer.getChoices());
                // 将结果放入到缓存中
                cache.put(cacheKey, JSONUtil.toJsonStr(finalResult));
                return finalUserAnswer;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
        return null;
    }

    private String generateCacheKey(Long appId, String choices) {
        return DigestUtil.md5Hex(appId + ":" + choices);
    }

    private String generateUserAnswerMessage(UserAnswer userAnswer,Long appId, Question question) {
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        List<List<String>> choices = userAnswerVO.getChoices();
        App app = appDao.getById(appId);
        AIJudgeUserAnswerDTO aiJudgeUserAnswerDTO = new AIJudgeUserAnswerDTO();
        aiJudgeUserAnswerDTO.setAppName(app.getAppName());
        aiJudgeUserAnswerDTO.setAppDesc(app.getAppDesc());
        List<AIJudgeUserAnswerDTO.Answer> answerList = new ArrayList<>();
        for (int i = 0; i < questionContent.size(); i++) {
            List<String> strings = choices.get(i);
            QuestionContentDTO questionContentDTO = questionContent.get(i);
            List<QuestionContentDTO.Option> options = questionContentDTO.getOptions();
            for (QuestionContentDTO.Option option : options) {
                if(strings.contains(option.getKey())){
                    AIJudgeUserAnswerDTO.Answer answer = new AIJudgeUserAnswerDTO.Answer();
                    answer.setTitle(questionContentDTO.getTitle());
                    answer.setAnswer(option.getValue());
                    answerList.add(answer);
                }
            }
        }
        aiJudgeUserAnswerDTO.setAnswerList(answerList);
        return  aiJudgeUserAnswerDTO.toString();
    }
}
