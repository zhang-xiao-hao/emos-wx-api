package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @Author: itxiaohao
 * @date: 2023-05-27 16:54
 * @Description: shiro认证、授权(先执行认证)
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }
    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        TbUser user = (TbUser)collection.getPrimaryPrincipal();
        int userId = user.getId();
        // 查询用户的权限列表
        Set<String> permsSet = userService.searchUserPermissions(userId);
        // 把权限列表添加到info对象中
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }
    // 认证（验证登录时调用）
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 获取字符串token
        String accessToken = (String) token.getPrincipal();
        // 从token中获取userId
        int userId = jwtUtil.getUserId(accessToken);
        TbUser user = userService.searchById(userId);
        if (user == null){
            throw new LockedAccountException("账号已被锁定，请联系管理员");
        }
        // 往info对象中添加用户信息、token字符串，返回给shiro进行认证判断
        return new SimpleAuthenticationInfo(user, accessToken, this.getName());
    }
}
