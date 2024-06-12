package com.badboy.dada.model.vo;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.badboy.dada.model.entity.UserAnswer;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户答案接口视图
 *
 * 
 */
@Data
public class UserAnswerVO implements Serializable {

    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用类型（0-得分类，1-角色测评类）
     */
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    private Integer scoringStrategy;

    /**
     * 用户答案（JSON 数组）,有可能多选
     */
    private List<List<String>> choices;

    /**
     * 得分类题目的正确答案，有可能多选
     */
    private List<List<String>> correctAnswers;

    /**
     * 评分结果 id
     */
    private Long resultId;

    /**
     * 结果名称，如物流师
     */
    private String resultName;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 结果图标
     */
    private String resultPicture;

    /**
     * 得分
     */
    private Float resultScore;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param userAnswerVO
     * @return
     */
    public static UserAnswer voToObj(UserAnswerVO userAnswerVO) {
        if (userAnswerVO == null) {
            return null;
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerVO, userAnswer);
        List<List<String>> choices1 = userAnswerVO.getChoices();
        userAnswer.setChoices(JSONUtil.toJsonStr(choices1));
        return userAnswer;
    }

    /**
     * 对象转封装类
     *
     * @param userAnswer
     * @return
     */
    public static UserAnswerVO objToVo(UserAnswer userAnswer) {
        if (userAnswer == null) {
            return null;
        }
        UserAnswerVO userAnswerVO = new UserAnswerVO();
        BeanUtils.copyProperties(userAnswer, userAnswerVO);
        String choices1 = userAnswer.getChoices();
        // 将 字符串 choices 转换为 列表
        List<List<String>> choices = jsonToList(choices1);
        userAnswerVO.setChoices(choices);
        return userAnswerVO;
    }

    private static List<List<String>> jsonToList(String jsonString) {
        JSONArray jsonArray = new JSONArray(jsonString);
        List<List<String>> resultList = new ArrayList<>();
        for (Object obj : jsonArray) {
            resultList.add(((JSONArray) obj).toList(String.class));
        }
        return resultList;
    }
}
