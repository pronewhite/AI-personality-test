package com.badboy.dada.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户答题记录
 * @TableName user_answer
 */
@TableName(value ="user_answer")
@Data
public class UserAnswer implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用 id
     */
    @TableField(value = "appId")
    private Long appId;

    /**
     * 应用类型（0-得分类，1-角色测评类）
     */
    @TableField(value = "appType")
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    @TableField(value = "scoringStrategy")
    private Integer scoringStrategy;

    /**
     * 用户答案（JSON 数组）
     */
    @TableField(value = "choices")
    private String choices;

    /**
     * 评分结果 id
     */
    @TableField(value = "resultId")
    private Long resultId;

    /**
     * 结果名称，如物流师
     */
    @TableField(value = "resultName")
    private String resultName;

    /**
     * 结果描述
     */
    @TableField(value = "resultDesc")
    private String resultDesc;

    /**
     * 结果图标
     */
    @TableField(value = "resultPicture")
    private String resultPicture;

    /**
     * 得分
     */
    @TableField(value = "resultScore")
    private Float resultScore;

    /**
     * 用户 id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 得分类题目的正确答案，有可能多选
     */
    @TableField(exist = false)
    private List<List<String>> correctAnswers;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}