package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: itxiaohao
 * @date: 2023-05-27 21:44
 * @Description: Shiro身份验证过滤器
 */
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Resource
    private ThreadLocalToken threadLocalToken;
    // redis中token缓存的expire，通常要大于发送给客户端的token有效期，当token过期时，如果redis中还没过期，则无需重新登录，刷新
    // token即可。通过这种方式，解决了无论用户有没有登录访问系统，token都会按时过期的问题。
    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = getRequestToken(req);
        if (StrUtil.isBlank(token)){
            return null;
        }
        return new OAuth2Token(token); //封装token给Shiro处理
    }
    // 是否通过认证
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        // 前端发请求时会先发一个options请求，询问服务器是否支持该content-type类型的数据，支持就发送。
        // 所以options无需认证，而其它的请求都不能直接通过认证，交给onAccessDenied处理。
        return req.getMethod().equals(RequestMethod.OPTIONS.name());
    }
    // 未通过isAccessAllowed()过滤器时触发的过滤器
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        // 解决跨域
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        threadLocalToken.clear();

        String token = getRequestToken(req);
        if (StrUtil.isBlank(token)){
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的token");
            return false;
        }
        try {
            // 校验token签名信息
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) { //客户端令牌过期
            // 假过期（客户端过期而redis缓存中没过期），刷新token
            if (BooleanUtil.isTrue(stringRedisTemplate.hasKey(token))){
                // 删除缓存中的token，并生成新的token
                stringRedisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                String newToken = jwtUtil.createToken(userId);
                stringRedisTemplate.opsForValue().set(newToken, StrUtil.toString(userId), cacheExpire, TimeUnit.DAYS) ;
                threadLocalToken.setToken(newToken);
            }else { // redis中的token缓存也过期了，需要重新登录
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("token已过期");
                return false;
            }
        } catch (Exception e){ // 令牌签名信息不对
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的token");
            return false;
        }
        // 调用 executeLogin 方法进行身份验证的具体逻辑
        // todo 在这个类中，我们实现了token的刷新，但是后面执行了executeLogin方法，根据源码查看，
        //  这个方法用于认证，会执行createToken()获取前端传来的token。但是如果我们刷新了token，executeLogin获取到的token还是老的token。
        return executeLogin(request, response);
    }
    // 认证失败时执行（executeLogin()为false）
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        // 跨域
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            resp.getWriter().print(e.getMessage());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    // 获取前端传来的token
    private String getRequestToken(HttpServletRequest request){
        String token = request.getHeader("token");
        // 可能放在了请求体内
        if (StrUtil.isBlank(token)){
            token = request.getParameter("token");
        }
        return token;
    }
}
