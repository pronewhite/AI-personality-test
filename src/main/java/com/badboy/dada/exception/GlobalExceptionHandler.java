package com.badboy.dada.exception;

import com.badboy.dada.common.BaseResponse;
import com.badboy.dada.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/1/27 19:44
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        StringBuilder errorMessage = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorMessage.append(fieldError.getDefaultMessage()).append(","));
        // 将最后一个逗号去掉
        errorMessage.deleteCharAt(errorMessage.length() - 1);
        String  message = errorMessage.toString();
        return ResultUtils.error(ExceptionResponseEnum.PARAM_INVALID.getErrorCode(), message);
    }

    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> buissnessException(BusinessException businessException){
        log.info("业务异常捕获:{}", businessException.getMsg());
        return ResultUtils.error(businessException.getCode(), businessException.getMsg());
    }

    /**
     * 最终的异常捕获，捕获一些数据库内部或者服务内部异常
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public BaseResponse<?> throwableHandler(Throwable throwable){
        log.info("全局异常捕获:{}",throwable.getMessage());
        return ResultUtils.error(ExceptionResponseEnum.SYSTEM_ERROR);
    }
}
