package com.badboy.dada.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/8 16:04
 */
@Data
public class AIGenerateQuestionDTO implements Serializable {

    /**
     *  应用ID
     */
    private Long appId;

    /**
     * 每一个应用的问题个数
     */
    private Integer questionCount;

    /**
     * 每一个问题的选项个数
     */
    private Integer optionsCount;

    private static final long serialVersionUID = 1L;
}
