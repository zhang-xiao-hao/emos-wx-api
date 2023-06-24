package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.mapper.TbMeetingMapper;
import com.example.emos.wx.db.mapper.TbUserMapper;
import com.example.emos.wx.db.pojo.TbMeeting;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-06-11 21:34
 * @Description:
 */
@Slf4j
@Service
public class MeetingServiceImpl implements MeetingService {
    @Resource
    private TbMeetingMapper meetingMapper;
    @Resource
    private TbUserMapper userMapper;
    @Value("${emos.code}")
    private String code;
    @Value("${workflow.url}")
    private String workflow;
    @Value("${emos.recieveNotify}")
    private String recieveNotify;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void insertMeeting(TbMeeting entity) {
        int row = meetingMapper.insertMeeting(entity);
        if (row != 1){
            throw new EmosException("会议添加失败");
        }
        //开启审批工作流
        startMeetingWorkflow(entity.getUuid(), entity.getCreatorId().intValue(), entity.getDate(), entity.getStart());
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingMapper.searchMyMeetingListByPage(param);
        String date = null; // 上一个会议的日期(list是有序查询的)
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null;
        JSONArray array = null; // 放置会议记录
        // 遍历会议列表来将相同日期的会议组合到一起
        for (HashMap map : list) {
            String temp = map.get("date").toString();
            if (!temp.equals(date)){
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date", date);
                array = new JSONArray(); //放置不同日期的会议记录
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            array.put(map);
        }
        return resultList;
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = meetingMapper.searchMeetingById(id);
        ArrayList<HashMap> list = meetingMapper.searchMeetingMembers(id);
        map.put("members", list);
        return map;
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        String date = param.get("date").toString();
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();
        // 查询修改前的会议记录
        HashMap oldMeeting = meetingMapper.searchMeetingById(id);
        String uuid = oldMeeting.get("uuid").toString();
        Integer creatorId = Integer.parseInt(oldMeeting.get("creatorId").toString());
        int row = meetingMapper.updateMeetingInfo(param); //更新
        if (row != 1) {
            throw new EmosException("会议更新失败");
        }
        // 更新成功，删除以前的工作流实例
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被修改");
        json.set("uuid", uuid);
        json.set("code", code);
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error("删除工作流失败");
            throw new EmosException("删除工作流失败");
        }
        // 创建新的工作流
        startMeetingWorkflow(uuid, creatorId, date, start);
    }

    @Override
    public void deleteMeetingById(int id) {
        // 查询会议信息
        HashMap meeting = meetingMapper.searchMeetingById(id);
        String uuid = meeting.get("uuid").toString();
        String instanceId = meeting.get("instanceId").toString();
        DateTime date = DateUtil.parse(meeting.get("date") + " " + meeting.get("start"));
        DateTime now = DateUtil.date();
        // 会议开始前20分钟，不能删除会议
        if (now.isAfterOrEquals(date.offset(DateField.MINUTE, -20))) {
            throw new EmosException("距离会议开始不足20分钟，不能删除会议");
        }
        int row = meetingMapper.deleteMeetingById(id);
        if (row != 1) {
            throw new EmosException("会议删除失败");
        }
        // 删除会议工作流实例
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被修改");
        json.set("uuid", uuid);
        json.set("code", code);
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error("删除工作流失败");
            throw new EmosException("删除工作流失败");
        }
    }

    @Override
    public long searchRoomIdByUUID(String uuid) {
        Object temp = redisTemplate.opsForValue().get(uuid);
        long roomId = Long.parseLong(temp.toString());
        return roomId;
    }

    @Override
    public List<String> searchUserMeetingInMonth(HashMap param) {
        return meetingMapper.searchUserMeetingInMonth(param);
    }

    private void startMeetingWorkflow(String uuid, int creatorId, String date, String start){
        HashMap info = userMapper.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code", code);
        json.set("date", date);
        json.set("start", start);
        String[] roles = info.get("roles").toString().split(",");
        if (!ArrayUtil.contains(roles, "总经理")) {
            // 部门经理id
            Integer managerId = userMapper.searchDeptManagerId(creatorId);
            json.set("managerId", managerId);
            // 总经理id
            Integer gmId = userMapper.searchGmId();
            json.set("gmId", gmId);
            // 参会人是否同一个部门
            boolean bool = meetingMapper.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }
        // 发送工作流请求（服务器部署的workflow依赖mysql，但mysql我运行在本地的，懒得配了。功能暂时用不了）
        // 而且依赖code，更用不了了，理解代码就是行
        String url = workflow + "/workflow/startMeetingProcess";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() == 200) {
            json = JSONUtil.parseObj(resp.body());
            // 这个工作流审批过后会返回一个instanceId
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);
            int row = meetingMapper.updateMeetingInstanceId(param);
            if (row != 1) {
                throw new EmosException("保存会议工作流实例ID失败");
            }
        }
    }
}
