package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.mapper.*;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * @Author: itxiaohao
 * @date: 2023-05-31 22:46
 * @Description: 考勤
 */
@Service
@Slf4j
@Scope("prototype")
public class CheckinServiceImpl implements CheckinService {
    @Resource
    private SystemConstants systemConstants;
    @Resource
    private TbHolidaysMapper holidaysMapper;
    @Resource
    private TbWorkdayMapper workdayMapper;
    @Resource
    private TbCheckinMapper checkinMapper;
    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;
    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;
    @Resource
    private TbFaceModelMapper faceModelMapper;
    @Resource
    private TbCityMapper cityMapper;
    @Value("${emos.email.hr}")
    private String hrEmail;
    @Resource
    private EmailTask emailTask;
    @Resource
    private TbUserMapper userMapper;
    @Value("${emos.code}")
    private String code;

    // todo 这个date参数貌似没用到
    @Override
    public String validCanCheckin(int userId, String date) {
        boolean bool_1 = holidaysMapper.searchTodayIsHolidays() != null;
        boolean bool_2 = workdayMapper.searchTodayIsWorkdays() != null;
        String type = "工作日";
        // 平常
        if (DateUtil.date().isWeekend()){
            type = "节假日";
        }
        // 特殊节日
        if (bool_1){
            type = "节假日";
        }else if (bool_2){
            type = "工作日";
        }

        if ("节假日".equals(type)){
            return "节假日无需考勤";
        }else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            if (now.isBefore(DateUtil.parse(start))){
                return "没有到上班考勤开始时间";
            } else if (now.isAfter(DateUtil.parse(end))){
                return "超过了上班考勤开始时间";
            }else {
                Map<String, Object> param = new HashMap<>();
                param.put("userId", userId);
                param.put("start", start);
                param.put("end", end);
                boolean bool = checkinMapper.haveCheckin(param) != null;
                return bool ? "请勿重复考勤" : "可以考勤";
            }
        }
    }
    // 签到
    @Override
    public void checkin(Map<String, Object> param) {
        Date now = DateUtil.date();
        Date attendanceTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);
        Date attendanceEndTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
        int status = 1; //正常签到
        //签到晚于上班时间，早于下班时间，记作迟到
        if (now.compareTo(attendanceTime) > 0 && now.compareTo(attendanceEndTime) < 0){
            status = 2;
        }
        int userId = (Integer) param.get("userId");
        String faceModel = faceModelMapper.searchFaceModel(userId); //每个用户对应的人脸模型
        if (faceModel == null){
            throw new EmosException("人脸模型不存在");
        }else {
            String path = (String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常");
            }
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
                throw new EmosException(body);
            }else if ("False".equals(body)){
                throw new EmosException("签到无效，非本人签到");
                //没有人脸识别的code，会返回icode不存在，模拟一下就当是识别成功了
            }else if ("True".equals(body) || "icode不存在".equals(body)){
                // 查询疫情风险等级
                int risk = 0; //常态化防控
                // 查询城市、区县
                String address = (String) param.get("address");
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)){
                    // 查询城市编码
                    String cityCode = cityMapper.searchCode(city);
                    // 查询地区风险 (本地宝H5页面)
                    String url = "http://m." + cityCode + ".bendibao.com/news/yqdengji/?qu=" + district;
                    try {
                        // jsoup解析html
                        Document document = Jsoup.connect(url).get();
                        Elements elements = document.getElementsByClass("cls17");
                        if (elements.size()>0){
                            String result = elements.select("p").text();
                            if ("高风险".equals(result)){
                                risk = 3;
                                // 发送警告邮件
                                HashMap<String, String> map = userMapper.searchNameAndDept(userId);
                                String name = map.get("name");
                                String deptName = map.get("dept_name");
                                deptName = deptName !=null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");
                                message.setText(deptName + "员工" + name +"，"
                                        + DateUtil.format(new Date(), "yyyy年MM月dd日") +
                                        "处于" + address + "，属于疫情高风险地区，请及时与该员工联系，合核实况！");
                                emailTask.sendAsync(message);
                            }else if ("中风险".equals(result)){
                                risk = 2;
                            }else if ("低风险".equals(result)){
                                risk = 1;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 保存签到记录
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                TbCheckin checkin = new TbCheckin();
                checkin.setUserId(userId);
                checkin.setAddress(address);
                checkin.setCountry(country);
                checkin.setProvince(province);
                checkin.setCity(city);
                checkin.setDistrict(district);
                checkin.setStatus((byte)status);
                checkin.setRisk(risk);
                checkin.setDate(DateUtil.today());
                checkin.setCreateTime(now);
                checkinMapper.insert(checkin);
            }
        }
    }
    // 建立人脸模型（python接口）
    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code", code);
        HttpResponse response = request.execute();
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
            throw new EmosException(body);
        }else {
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelMapper.insert(entity);
        }
    }

    @Override
    public HashMap<String, Object> searchTodayCheckin(int userId) {
        return checkinMapper.searchTodayCheckin(userId);
    }

    @Override
    public long searchCheckinDays(int userId) {
        return checkinMapper.searchCheckinDays(userId);
    }

    @Override
    public List<HashMap<String, Object>> searchWeekCheckin(HashMap<String, Object> param) {
        List<HashMap<String, Object>> checkinList = checkinMapper.searchWeekCheckin(param);
        List<String> holidaysList = holidaysMapper.searchHolidaysInRange(param);
        List<String> workdayList = workdayMapper.searchWorkdayInRange(param);

        // 根据本周的起始和结束日期生成一周的日期
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        List<HashMap<String, Object>> list = new ArrayList<>();
        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            String type = "工作日";
            if (one.isWeekend()){
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)){
                type = "节假日";
            }else if(workdayList != null && workdayList.contains(date)){
                type = "工作日";
            }
            String status = ""; // 是工作日，并且当前这一天还未到来，status为""
            // 是工作日，并且当前这一天已经过了
            if ("工作日".equals(type) && DateUtil.compare(one, DateUtil.date()) <= 0){
                status = "缺勤";
                boolean flag = false;
                for (HashMap<String, Object> map : checkinList) {
                    // 考过勤了
                    if (map.containsValue(date)){
                        status = (String) map.get("status");
                        flag = true;
                        break;
                    }
                }
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
                String today = DateUtil.today();
                // 当前这天是今天，还未签到，并且时间在签到截至之前（还能签到）
                if (date.equals(today) && DateUtil.date().isBefore(endTime) && !flag){
                    status = "";
                }
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public List<HashMap<String, Object>> searchMonthCheckin(HashMap<String, Object> param) {
        // 根据起始日期和结束日期查询考勤
        return this.searchWeekCheckin(param);
    }
}
