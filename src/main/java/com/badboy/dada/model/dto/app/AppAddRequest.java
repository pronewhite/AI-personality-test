package com.badboy.dada.model.dto.app;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建应用接口请求
 *
 * 
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名
     */
    @NonNull
    @Max(20)
    private String appName;

    /**
     * 应用描述
     */
    @NonNull
    @Max(100)
    private String appDesc;

    /**
     * 应用图标
     */
    @NonNull
    private String appIcon;

    /**
     * 应用类型（0-得分类，1-测评类）
     */
    @NonNull
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    @NonNull
    private Integer scoringStrategy;

    private static final long serialVersionUID = 1L;
}