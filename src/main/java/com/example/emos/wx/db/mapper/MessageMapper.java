package com.example.emos.wx.db.mapper;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-06-06 21:33
 * @Description:
 */
@Repository
public class MessageMapper {
    @Resource
    private MongoTemplate mongoTemplate;
    public String insert(MessageEntity entity){
        // 把北京时间转化为格林尼治时间（mongodb为格林尼治时间）
        Date sendTime = entity.getSendTime();
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        entity.setSendTime(sendTime);
        entity = mongoTemplate.save(entity);
        // 插入mongodb后，mongodb回为每条数据创建一个_id字段
        return entity.get_id();
    }
    // 分页查询
    public List<HashMap> searchMessageByPage(int userId, long start, int length){
        JSONObject json = new JSONObject();
        //联合查询
        /*
        db.message.aggregate([
        {
            $set: {
            "id": { $toString: "$_id" }
            }
        },
         {
            $lookup:{
                from:"message_ref",
                localField:"id",
                foreignField:"messageId",
                as:"ref"
            },
         },
         { $match:{"ref.receiverId": 1} },
         { $sort: {sendTime : -1} },
         { $skip: 0 },
         { $limit: 50 }
        ])
        * */
        json.set("$toString", "$_id");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.addFields().addField("id").withValue(json).build(),
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),
                Aggregation.skip(start),
                Aggregation.limit(length)
        );
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = results.getMappedResults();
        // 封装返回数据，转回北京时间
        list.forEach(one -> {
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            MessageRefEntity entity = refList.get(0);
            boolean readFlag = entity.getReadFlag();
            String refId = entity.get_id();
            one.put("readFlag", readFlag);
            one.put("refId", refId);
            one.remove("ref");
            one.remove("_id");
            Date sendTime = (Date) one.get("sendTime");
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);

            String today = DateUtil.today();
            // 如果是当天的通知消息，那么不用显示年份
            if (today.equals(DateUtil.date(sendTime).toString())){
                one.put("sendTime", DateUtil.format(sendTime, "HH:mm"));
            }else {
                one.put("sendTime", DateUtil.format(sendTime, "yyyy/MM/dd"));
            }
        });
        return list;
    }
    public HashMap searchMessageById(String id){
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");
        Date sendTime = (Date) map.get("sendTime");
        // 转回北京时间
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);
        map.replace("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm"));
        return map;
    }
}
