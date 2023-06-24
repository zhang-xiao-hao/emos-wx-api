package com.example.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.mapper.TbDeptMapper;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: itxiaohao
 * @date: 2023-05-30 21:19
 * @Description:
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Value("${wx.app-id}")
    private String appId;
    @Value("${wx.app-secret}")
    private String appSecret;
    @Resource
    private TbUserMapper userMapper;
    @Resource
    private MessageTask messageTask;
    @Resource
    private TbDeptMapper deptMapper;
    /**
     * 申请微信openId临时授权凭证
     * @param code 临时授权凭证
     * @return openId
     */
    private String getOpenId(String code){
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> map = new HashMap<>();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0){
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    /**
     * 注册用户，绑定了用户的openId则视为注册
     * @param registerCode 邀请码
     * @param code 临时授权凭证
     * @param nickname 微信昵称
     * @param photo 微信头像url
     * @return 用户id
     */
    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        // 邀请码000000代表超级管理员，只有一个
        if (registerCode.equals("000000")){
            boolean haveRootUser = userMapper.haveRootUser();
            // 没有则注册一个超管
            if (!haveRootUser){
                String openId = getOpenId(code);
                HashMap<String, Object> param = new HashMap<>();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]"); // 超管角色为0，json
                param.put("status", 1); // 1:在职的有效状态
                param.put("createTime", new Date());
                param.put("root", true);
                userMapper.insert(param);
                int id = userMapper.searchIdByOpenId(openId);
                // rabbitmq异步发送注册成功的消息
                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0); //系统发送：0
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎您注册成为超级管理员，请及时更新您的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id+"", entity);
                // 返回用户id
                return id;
            }else {
                // 业务异常
                throw new EmosException("无法绑定超级管理有账号");
            }
        }
        // 普通员工注册
        else {

        }
        return 0;
    }

    /**
     *  查询用户权限
     * @param userId id
     * @return 用户权限信息
     */
    @Override
    public Set<String> searchUserPermissions(int userId) {
        return userMapper.searchUserPermissions(userId);
    }

    /**
     *  登录,根据openId查询用户的id，如果存在那么说明该用户注册过
     * @param code 临时授权码
     * @return id
     */
    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userMapper.searchIdByOpenId(openId);
        if (id == null){
            throw new EmosException("账户不存在");
        }
        return id;
    }

    @Override
    public TbUser searchById(int userId) {
        return userMapper.searchById(userId);
    }

    @Override
    public String searchUserHiredate(int userId) {
        return userMapper.searchUserHiredate(userId);
    }

    @Override
    public HashMap<String, Object> searchUserSummary(int userId) {
        return userMapper.searchUserSummary(userId);
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptMapper.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2 = userMapper.searchUserGroupByDept(keyword);
        for (HashMap map_1 : list_1) {
            long deptId = (long) map_1.get("id");
            ArrayList members = new ArrayList();
            for (HashMap map_2 : list_2) {
                long id = (long) map_2.get("deptId");
                if (deptId == id){
                    members.add(map_2);
                }
            }
            map_1.put("members", members);
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        return userMapper.searchMembers(param);
    }

    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        return userMapper.selectUserPhotoAndName(param);
    }

    @Override
    public String searchMemberEmail(int id) {
        return userMapper.searchMemberEmail(id);
    }
}
