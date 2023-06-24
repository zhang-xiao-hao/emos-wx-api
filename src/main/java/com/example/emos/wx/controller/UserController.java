package com.example.emos.wx.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.config.tencent.TLSSigAPIv2;
import com.example.emos.wx.controller.form.*;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @Author: itxiaohao
 * @date: 2023-05-30 22:24
 * @Description:
 */
@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;
    @Value("${trtc.appid}")
    private Integer appid;
    @Value("${trtc.key}")
    private String key;
    @Value("${trtc.expire}")
    private Integer expire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form){
        int id = userService.registerUser(form.getRegisterCode(),
                form.getCode(),
                form.getNickname(),
                form.getPhoto());
        // 根据用户id生成token
        String token = jwtUtil.createToken(id);
        // 查询用户权限
        Set<String> permsSet = userService.searchUserPermissions(id);
        // 保存token到redis缓存
        saveCacheToken(token, id);
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);
    }
    // 保存token缓存
    private void saveCacheToken(String token, int userId){
        stringRedisTemplate.opsForValue().set(token, StrUtil.toString(userId), cacheExpire);
    }

    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form){
        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        return R.ok("登录成功").put("token", token).put("permission", permsSet);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap<String, Object> map = userService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按部门分组")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical =  Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form){
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);  
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"}, logical =  Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form){
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }
    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户姓名和头像")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public R selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameForm form){
        if (!JSONUtil.isJsonArray(form.getIds())){
            throw new EmosException("参数不是JSON数组");
        }
        List<Integer> param = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> list = userService.selectUserPhotoAndName(param);
        return R.ok().put("result", list);
    }

    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token){
        int id = jwtUtil.getUserId(token);
        String email = userService.searchMemberEmail(id);
        // 腾讯提供的生成用户签名的java类
        TLSSigAPIv2 api = new TLSSigAPIv2(appid, key);
        String userSig = api.genUserSig(email, expire);
        return R.ok().put("userSig",userSig).put("email", email);
    }
}
