package com.badboy.dada.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 用户角色枚举
 *
 
 */
public enum UserRoleEnum{

    USER("用户", "user"),
    ADMIN("管理员", "admin"),
    VIP("VIP用户","VIP"),
    BAN("被封号", "ban");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    static Map<String, UserRoleEnum> cache ;

    public String getValue(){
        return this.value;
    }


    static {
        cache = Arrays.stream(values()).collect(Collectors.toMap(UserRoleEnum::getValue, Function.identity()));
    }

    public static UserRoleEnum of(String value){
        return cache.get(value);
    }
}
