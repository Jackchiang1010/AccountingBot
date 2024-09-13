package com.example.accountbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

@LineMessageHandler
@Slf4j
public class MessageHandler {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private ObjectMapper objectMapper;

//    @EventMapping
//    public Message handlerTextMessageEvent(MessageEvent<TextMessageContent> event) {
//        log.info("event: " + event);
//        final String originalMessageText = event.getMessage().getText();
//        return new TextMessage("打個招呼嘛~我是機器人能為你服務");
//    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("event: " + event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String userId = event.getSource().getUserId();
        String receivedText = event.getMessage().getText();

        // 如果用戶輸入 "flex"，則回應 Flex Message
        if ("flex".equalsIgnoreCase(receivedText)) {
            String flexMessageJson = """
                    {
                              "type": "bubble",
                              "hero": {
                                "type": "image",
                                "url": "https://developers-resource.landpress.line.me/fx/img/01_1_cafe.png",
                                "size": "full",
                                "aspectRatio": "20:13",
                                "aspectMode": "cover",
                                "action": {
                                  "type": "uri",
                                  "uri": "https://line.me/"
                                }
                              },
                              "body": {
                                "type": "box",
                                "layout": "horizontal",
                                "contents": [
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "message",
                                      "label": "action",
                                      "text": "hello1"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "message",
                                      "label": "action",
                                      "text": "hello2"
                                    },
                                    "position": "relative"
                                  },
                                  {
                                    "type": "button",
                                    "action": {
                                      "type": "message",
                                      "label": "action",
                                      "text": "hello3"
                                    },
                                    "position": "relative"
                                  }
                                ]
                              }
                            }
            """;

            try {
                // 將 JSON 字串轉換為 FlexContainer
                FlexContainer flexContainer = objectMapper.readValue(flexMessageJson, FlexContainer.class);

                // 建立 FlexMessage
                FlexMessage flexMessage = new FlexMessage("Flex Message 標題", flexContainer);

                // 發送 Flex Message
                lineMessagingClient.pushMessage(new PushMessage(userId, flexMessage)).get();

            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            // 如果用戶輸入其他內容，回應普通的文字訊息
            TextMessage replyMessage = new TextMessage("請輸入 'flex' 來查看 Flex Message!");
            try {
                lineMessagingClient.pushMessage(new PushMessage(userId, replyMessage)).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}