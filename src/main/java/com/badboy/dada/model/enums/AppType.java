package com.badboy.dada.model.enums;

import com.badboy.dada.model.entity.App;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum AppType {

    SCORE(0, "得分类"),
    TEST(1, "测评类");

    static Map<Integer, AppType> cache ;

    public int getValue(){
        return this.value;
    }


    static {
        cache = Arrays.stream(values()).collect(Collectors.toMap(AppType::getValue, Function.identity()));
    }

    public AppType of(Integer value){
        return cache.get(value);
    }

    private int value;
    private String text;
}
