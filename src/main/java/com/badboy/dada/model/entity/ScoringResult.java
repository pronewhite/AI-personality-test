package com.badboy.dada.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评分结果
 * @TableName scoring_result
 */
@TableName(value ="scoring_result")
@Data
public class ScoringResult implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

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
     * 结果图片
     */
    @TableField(value = "resultPicture")
    private String resultPicture;

    /**
     * 结果属性集合 JSON，如 [I,S,T,J]
     */
    @TableField(value = "resultProp")
    private String resultProp;

    /**
     * 结果得分范围，如 80，表示 80及以上的分数命中此结果
     */
    @TableField(value = "resultScoreRange")
    private Integer resultScoreRange;

    /**
     * 应用 id
     */
    @TableField(value = "appId")
    private Long appId;

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