package com.badboy.dada.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用
 * @TableName app
 */
@TableName(value ="app")
@Data
public class App implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用名
     */
    @TableField(value = "appName")
    private String appName;

    /**
     * 应用描述
     */
    @TableField(value = "appDesc")
    private String appDesc;

    /**
     * 应用图标
     */
    @TableField(value = "appIcon")
    private String appIcon;

    /**
     * 应用类型（0-得分类，1-测评类）
     */
    @TableField(value = "appType")
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    @TableField(value = "scoringStrategy")
    private Integer scoringStrategy;

    /**
     * 审核状态：0-待审核, 1-通过, 2-拒绝
     */
    @TableField(value = "reviewStatus")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @TableField(value = "reviewMessage")
    private String reviewMessage;

    /**
     * 审核人 id
     */
    @TableField(value = "reviewerId")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "reviewTime")
    private Date reviewTime;

    /**
     * 创建用户 id
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
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}