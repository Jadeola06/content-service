package com.flexydemy.content.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class Utils {


    public static String getToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");

        if(StringUtils.isNotBlank(token) && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        return token;
    }
}
