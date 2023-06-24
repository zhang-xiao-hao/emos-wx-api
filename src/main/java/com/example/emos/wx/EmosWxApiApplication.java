package com.example.emos.wx;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.mapper.SysConfigMapper;
import com.example.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MapperScan("com.example.emos.wx.db.mapper")
@Slf4j
@SpringBootApplication
@ServletComponentScan // Servlet、Filter、Listener可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册到Spring容器中。
@EnableAsync //开启异步
public class EmosWxApiApplication {
    @Resource
    private SysConfigMapper sysConfigMapper;
    @Resource
    private SystemConstants constants;
    @Value("${emos.image-folder}")
    private String imageFolder;

    /**
     * 项目启动初始化
     */
    @PostConstruct
    public void init(){
        // 保存数据库中的上下班时间常量
        List<SysConfig> list = sysConfigMapper.selectAllParam();
        Map<String, String> map = new HashMap<>();
        list.forEach(one->{
            // 转成SystemConstants的变量形式
            String key = StrUtil.toCamelCase(one.getParamKey());
            String value = one.getParamValue();
            map.put(key, value);
        });
        BeanUtil.fillBeanWithMap(map, constants, false);
        // 初始化签到自拍的文件路径
        new File(imageFolder).mkdirs();
    }

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

}
