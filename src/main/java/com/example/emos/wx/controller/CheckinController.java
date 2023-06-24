package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.controller.form.SearchMonthCheckinForm;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-05-31 23:13
 * @Description:
 */
@RestController
@Api("签到模块web接口")
@Slf4j
@RequestMapping("/checkin")
public class CheckinController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private CheckinService checkinService;
    @Value("${emos.image-folder}")
    private String imageFolder;
    @Resource
    private UserService userService;
    @Resource
    private SystemConstants constants;

    @GetMapping("/validCanCheckin")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckin(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckin(userId, DateUtil.today());
        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form,
                     @RequestParam("photo") MultipartFile file,
                     @RequestHeader("token") String token){
        if (file == null){
            return R.error("没有上传照片");
        }
        int userId = jwtUtil.getUserId(token);
        String filename = file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".jpg")){
            return R.error("必须提交JPG格式的照片");
        }else {
            String path = imageFolder + "/" + filename;
            try {
                file.transferTo(Paths.get(path));
                HashMap<String, Object> param = new HashMap<>();
                param.put("userId", userId);
                param.put("path", path);
                param.put("city", form.getCity());
                param.put("district", form.getDistrict());
                param.put("address", form.getAddress());
                param.put("country", form.getCountry());
                param.put("province", form.getProvince());
                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("照片保存错误");
            } finally {
                // 删除签到照片
                FileUtil.del(path);
            }
        }
    }
    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file,
                             @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        if (file == null){
            return R.error("没有上传照片");
        }
        String filename = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + filename;
        if (!filename.endsWith(".jpg")){
            return R.error("必须提交JPG格式的照片");
        }else {
            try {
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("照片保存错误");
            } finally {
                FileUtil.del(path);
            }
        }
    }
    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap<String, Object> map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long checkinDays = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", checkinDays);

        // 判断当前日期是否在用户入职之前
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        // 那么这周入职之前的考勤不算，从入职日期开始算
        if (startDate.isBefore(hiredate)){
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap<String, Object> param = new HashMap<>();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        List<HashMap<String, Object>> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月的签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form,
                                @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        // 查询入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        // 把月份处理成双数字
        String month = form.getMonth() < 10 ? "0"+form.getMonth() : "" + form.getMonth();
        // 某年某月的起始日期
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month+"-01");
        // 查询的起始日期在入职月之前
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))){
            throw new EmosException("只能查询入职之后日期的数据");
        }
        // 如果查询月份与入职月份恰好同月，本月考勤查询的开始日期设置为入职日期
        if (startDate.isBefore(hiredate)){
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        List<HashMap<String, Object>> list = checkinService.searchMonthCheckin(param);
        // 统计一个月的签到信息
        int sum_1=0, sum_2=0, sum_3=0;
        for (HashMap<String, Object> one : list) {
            String type = (String)one.get("type");
            String status = (String) one.get("status");
            if ("工作日".equals(type)){
                if ("正常".equals(status)){
                    sum_1 ++;
                }else if ("迟到".equals(status)){
                    sum_2 ++;
                }else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }
}
