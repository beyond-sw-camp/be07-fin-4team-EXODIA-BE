package com.example.exodia.sms.service;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CoolSmsService {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.number}")
    private String fromPhoneNumber;

    public String sendSms(String to) throws CoolsmsException {
        String textMessage = "[엑조디아 경조] \n"
                + "인사팀 이명규(팀장)님의 결혼 소식을 알려드립니다.\n"
                + "일시: 2025-02-13\n";
//                + "장소: 대구 동구 예식장\n";


        Message coolsms = new Message(apiKey, apiSecret);

        HashMap<String, String> params = new HashMap<>();
        params.put("to", to);
        params.put("from", fromPhoneNumber);
        params.put("type", "sms");
        params.put("text", textMessage);

        coolsms.send(params);

        return textMessage;
    }

}
