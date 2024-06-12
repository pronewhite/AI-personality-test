package com.badboy.dada.model.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum ScoringStragety {

    CUSTOM(0, "自定义评分"),
    AI(1, "AI 评分");

    static Map<Integer, ScoringStragety> cache ;

    public int getValue(){
        return this.value;
    }

    static {
        cache = Arrays.stream(values()).collect(Collectors.toMap(ScoringStragety::getValue, Function.identity()));
    }

    public ScoringStragety of(Integer value){
        return cache.get(value);
    }

    private int value;
    private String text;
}
