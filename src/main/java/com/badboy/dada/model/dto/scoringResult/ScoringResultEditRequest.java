package com.badboy.dada.model.dto.scoringResult;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑得分结果接口请求
 *
 * 
 */
@Data
public class ScoringResultEditRequest implements Serializable {

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
    private List<String> resultProp;

    private static final long serialVersionUID = 1L;
}