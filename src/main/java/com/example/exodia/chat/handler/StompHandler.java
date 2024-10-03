package com.example.exodia.chat.handler;

import com.example.exodia.chat.service.ChatRoomManage;
import com.example.exodia.common.auth.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {
    private final ChatRoomManage chatRoomManage;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public StompHandler(ChatRoomManage chatRoomManage, JwtTokenProvider jwtTokenProvider) {
        this.chatRoomManage = chatRoomManage;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // stomphandler 는 공백을 인지하지 못하여 오로지 token만 다룰것 . Bearer 떼야한다.
    // WebSocket을 통해 들어온 요청이 처리 되기 전에 실행
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        String jwtToken = "";
//
//        if(StompCommand.CONNECT == accessor.getCommand()){ // 웹소켓 연결 요청
//            String jwt = accessor.getFirstNativeHeader("Authorization");
//            if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer")){
//                jwtToken = Objects.requireNonNull(accessor.getFirstNativeHeader("token")).substring(7);
//                jwtTokenProvider.validateToken(jwtToken);
//                // chatRoomManage
//            }
//        }else if(StompCommand.SUBSCRIBE == accessor.getCommand()){ // 채팅룸 구독요청
//
//        }else if (StompCommand.DISCONNECT == accessor.getCommand()) {
//            // chatRoomManage
//        }


        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // websocket 연결시 헤더의 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwtToken = "";
            jwtToken = Objects.requireNonNull(accessor.getFirstNativeHeader("Authorization")).substring(7);
            jwtTokenProvider.validateToken(jwtToken);

//            String jwt = accessor.getFirstNativeHeader("Authorization");
//            jwtTokenProvider.validateToken(jwt);
        }
        return message;
    }
}
