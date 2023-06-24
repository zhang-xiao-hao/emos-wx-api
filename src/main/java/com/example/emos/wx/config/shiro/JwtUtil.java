package com.example.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: itxiaohao
 * @date: 2023-05-27 15:44
 * @Description: jwt工具类。
 */
@Component
@Slf4j
public class JwtUtil {
    @Value("${emos.jwt.secret}")
    private String secret; // 密钥

    @Value("${emos.jwt.expire}")
    private int expire; // token过期时间
    public String createToken(int userId){
        // token = Header . payload . signature（使用”.“拼接）
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, expire);
        // 消息认证码算法（对称加密）    Header（token类型和加密算法）
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // payload（存在token中传输的数据）
        JWTCreator.Builder builder = JWT.create();
        // signature (算法对Header 和 payload 计算出来的签名值，用于验证)
        return builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
    }

    public int getUserId(String token){
        // 解密 JWT Token 中的 payload 部分，不需要指定加密算法和密钥。
        // 因为Header和payload都是经过 Base64 编码后的明文数据，而signature签名信息是密文
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userId").asInt();
    }

    public void verifierToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        // 解析token并验证签名信息，失败会抛出runtime异常
        verifier.verify(token);
    }
}
