package com.badboy.dada.manager;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/8 13:26
 */
@Component
@Slf4j
public class AIManager {

    @Autowired
    private ClientV4 clientV4;

    private static final Float STABLE_AI_RESULT = 0.05F;
    private static final Float UNSTABLE_AI_RESULT = 0.95F;

    /**
     *  同步稳定调用
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncStableRequest(String systemMessage,String userMessage){
        return doRequest(systemMessage, userMessage, Boolean.TRUE, STABLE_AI_RESULT);
    }

    /**
     * 同步非稳定调用
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncUnstableRequest(String systemMessage,String userMessage){
        return doRequest(systemMessage, userMessage, Boolean.FALSE, UNSTABLE_AI_RESULT);
    }

    /**
     * 异步调用
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public String doAsyncRequest(String systemMessage,String userMessage, Float temperature){
        return doRequest(systemMessage, userMessage, Boolean.TRUE, temperature);
    }

    /**
     * 同步调用
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public String doSyncRequest(String systemMessage,String userMessage, Float temperature){
        return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
    }

    /**
     *  对通用方法的第一层封装
     * @param systemMessage
     * @param userMessage
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(String systemMessage,String userMessage, boolean stream, Float temperature){
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(systemChatMessage);
        messages.add(userChatMessage);
        // 调用通用方法调用底层大模型
        return doRequest(messages, stream, temperature);
    }

    /**
     * 通用方法（底层调用）
     * @param messages 消息
     * @param stream 是否流式返回
     * @return 结果
     */
    public String doRequest(List<ChatMessage> messages, boolean stream, Float temperature){
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().toString();
    }

    /**
     * 对通用方法（流式输出）的第一层封装
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 结果
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage,String userMessage){
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(systemChatMessage);
        messages.add(userChatMessage);
        return doStreamRequest(messages);
    }

    /**
     * 通用方法（流式输出）
     * @param messages
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages) {
        // 参数校验
        if (messages == null || messages.isEmpty()) {
            log.error((Marker) Level.WARNING, "messages is null or empty. Returning an empty Flowable.");
            return Flowable.empty(); // 返回一个空的Flowable以避免空指针异常
        }

        ChatCompletionRequest chatCompletionRequest;
        try {
            // 构建请求，考虑到可能的异常
            chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(Constants.ModelChatGLM4)
                    .stream(Boolean.TRUE)
                    .temperature(UNSTABLE_AI_RESULT)
                    .invokeMethod(Constants.invokeMethod)
                    .messages(messages)
                    .build();
        } catch (Exception e) {
            log.error((Marker) Level.SEVERE, "Failed to build chat completion request", e);
            return Flowable.error(e); // 发射一个错误的Flowable
        }

        // 异常处理和API响应验证
        ModelApiResponse invokeModelApiResp;
        try {
            invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            if (invokeModelApiResp == null) {
                log.info((Marker) Level.WARNING, "API response is null. Returning an empty Flowable.");
                return Flowable.empty();
            }
        } catch (Exception e) {
           log.error((Marker) Level.SEVERE, "API invocation failed", e);
            return Flowable.error(e);
        }

        // 返回Flowable前进行最后的检查
        return invokeModelApiResp.getFlowable()
                .doOnError(error -> log.error((Marker) Level.SEVERE, "Error in processing API response", error))
                .onErrorReturn(throwable -> {
                    log.error((Marker) Level.WARNING, "Returning empty Flowable due to error", throwable);
                    return new ModelData(); // 根据实际情况返回合适的空值或错误处理
                });
    }
}

