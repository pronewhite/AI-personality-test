package com.badboy.dada.exception;

import com.badboy.dada.model.enums.ErrorEnum;
import lombok.Data;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/1/27 20:59
 */
@Data
public class BusinessException extends RuntimeException{

    public Integer code;
    public String msg;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }



    public BusinessException(String msg){
        super(msg);
        this.code = ExceptionResponseEnum.BUISSNESS_EXCEPTION.getErrorCode();
        this.msg = msg;
    }

    public BusinessException(ErrorEnum errorEnum){
        super(errorEnum.getErrorMessage());
        this.code = errorEnum.getErrorCode();
        this.msg = errorEnum.getErrorMessage();
    }

    public BusinessException(ErrorEnum errorEnum, String msg){
        super(errorEnum.getErrorMessage());
        this.code = ExceptionResponseEnum.BUISSNESS_EXCEPTION.getErrorCode();
        this.msg = msg;
    }
}
