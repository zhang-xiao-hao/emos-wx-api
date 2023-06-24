package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @Author: itxiaohao
 * @date: 2023-06-06 21:57
 * @Description:
 */
@Repository
public class MessageRefMapper {
    @Resource
    private MongoTemplate mongoTemplate;

    public String insert(MessageRefEntity entity){
        entity = mongoTemplate.save(entity);
        return entity.get_id();
    }

    public long searchUnreadCount(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId));
        return mongoTemplate.count(query, MessageRefEntity.class);
    }

    public long searchLastCount(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId));
        Update update = new Update();
        update.set("lastFlag", false);
        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        // 修改lastFlag为false的数量，即最新消息的数量(查过这些消息后，就不是最新的了)
        return result.getModifiedCount();
    }

    public long updateUnreadMessage(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("readFlag", true);
        UpdateResult result = mongoTemplate.updateFirst(query, update, "message_ref");
        return result.getModifiedCount();
    }

    public long deleteMessageRefById(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        return result.getDeletedCount();
    }

    public long deleteUserMessageRef(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        return result.getDeletedCount();
    }
}
