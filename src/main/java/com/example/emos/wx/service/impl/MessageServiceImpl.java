package com.example.emos.wx.service.impl;

import com.example.emos.wx.db.mapper.MessageMapper;
import com.example.emos.wx.db.mapper.MessageRefMapper;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-06-06 22:39
 * @Description:
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageRefMapper messageRefMapper;
    @Override
    public String insertMessage(MessageEntity entity) {
        return messageMapper.insert(entity);
    }

    @Override
    public String insertRef(MessageRefEntity entity) {
        return messageRefMapper.insert(entity);
    }

    @Override
    public long searchUnreadCount(int userId) {
        return messageRefMapper.searchUnreadCount(userId);
    }

    @Override
    public long searchLastCount(int userId) {
        return messageRefMapper.searchLastCount(userId);
    }

    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        return messageMapper.searchMessageByPage(userId, start, length);
    }

    @Override
    public HashMap searchMessageById(String id) {
        return messageMapper.searchMessageById(id);
    }

    @Override
    public long updateUnreadMessage(String id) {
        return messageRefMapper.updateUnreadMessage(id);
    }

    @Override
    public long deleteMessageRefById(String id) {
        return messageRefMapper.deleteMessageRefById(id);
    }

    @Override
    public long deleteUserMessageRef(int userId) {
        return messageRefMapper.deleteUserMessageRef(userId);
    }
}
