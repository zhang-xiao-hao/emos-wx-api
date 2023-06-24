package com.example.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @Author: itxiaohao
 * @date: 2023-05-27 16:49
 * @Description: 封装令牌token为AuthenticationToken
 */
public class OAuth2Token implements AuthenticationToken {
    private final String token;

    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
