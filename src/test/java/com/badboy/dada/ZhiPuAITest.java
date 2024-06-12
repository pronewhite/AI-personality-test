package com.badboy.dada;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author badboy
 * @version 1.0
 * Create by 2024/6/8 13:01
 */
@SpringBootTest
public class ZhiPuAITest {

    @Value("${zhipuai.api-key}")
    private String apiKey;

    @Test
    public void test(){
        ClientV4 client = new ClientV4.Builder(apiKey).build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "为什么冬暖夏凉");
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "你是一个科学家，解答用户提出的各种科学有关问题");
        messages.add(systemChatMessage);
        messages.add(chatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .temperature(0.1F)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        try {
            System.out.println(invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
