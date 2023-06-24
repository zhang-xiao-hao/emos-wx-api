package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByPageForm;
import com.example.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.example.emos.wx.service.MessageService;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-06-06 22:45
 * @Description:
 */
@RestController
@RequestMapping("/message")
@Api("消息模块接口")
public class MessageController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private MessageService messageService;
    @Resource
    private MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form,
                                 @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form){
        HashMap map = messageService.searchMessageById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateUnreadMessage")
    @ApiOperation("未读消息更新为已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form){
        long count = messageService.updateUnreadMessage(form.getId());
        // 更新了：true
        return R.ok().put("result", count == 1);
    }

    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form){
        long count = messageService.deleteMessageRefById(form.getId());
        // 删除了：true
        return R.ok().put("result", count == 1);
    }

    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户消息")
    public R refreshMessage(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        // 异步接收数据，把队列中的数据全部发给用户
        messageTask.receiveAsync(userId+"");
        // 查询用户接收了多少条消息
        long lastRows = messageService.searchLastCount(userId);
        // 未读消息的数量
         long unreadRows = messageService.searchUnreadCount(userId);
         return R.ok().put("lastRows", lastRows).put("unreadRows", unreadRows);
    }
}
