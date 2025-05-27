package com.zlp.platform.util;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.util.AesUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Test {

    public static void main(String[] args) {
        JSONObject one=new JSONObject();

        one.put("timestamp","2022-12-20 10:54:30");
        one.put("userCode","developer");
        one.put("userPassword","a");
        // laHPsUR4b3UotbrqiniGN2YhHulEZzWQdYhdQPoEvCjktW6pUGX2JdeOb0BzqMNYMxr/sUNrc5htVWAuRmQ1noVTRyTZKyYudJr9wRvHIwIhTq/vlUAlx0dtMyhsmXdLXi1+gARKFssRfWKe/049jw==
       String key="icm666777333adim";

        String cont=AesUtils.encrypt(one.toString(),key);
        System.out.println(cont);
        System.out.println(AesUtils.decrypt(cont,key));


        String dateStr = "2022-12-20 10:54:30";
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.parse(dateStr, inputFormatter);
        System.out.println("datetime : " + datetime.plusMinutes(5).compareTo(LocalDateTime.now()));

    }



}
