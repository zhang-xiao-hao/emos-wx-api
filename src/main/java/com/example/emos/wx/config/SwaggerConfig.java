package com.example.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 13:43
 * @Description: Swagger配置类
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi(){

        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        ApiInfoBuilder builder = new ApiInfoBuilder();
        // 设置了API的标题
        builder.title("EMOS在线办公系统");
        // 构建 ApiInfo 对象
        ApiInfo info = builder.build();
        // 将 Api 基本信息传递给 Docket 对象以便进行显示。
        docket.apiInfo(info);

        ApiSelectorBuilder selectorBuilder = docket.select();
        //  指定 API 接口的根路径
        selectorBuilder.paths(PathSelectors.any());
        // 指定注解@ApiOperation来确定哪些类中的方法会被生成文档。
        selectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = selectorBuilder.build();

        //  指定 API 文档中需要使用的认证方式，封装token到Header来传递身份验证信息。
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> apiKeyList = new ArrayList<>();
        apiKeyList.add(apiKey);
        docket.securitySchemes(apiKeyList);
        // 定义权限域。
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopes = {scope};
        // 引用上面定义的 token 认证方式，并将权限域数组设置到其中。
        SecurityReference reference = new SecurityReference("token", scopes);
        List<SecurityReference> refList = new ArrayList();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();
        List<SecurityContext> ctxList = new ArrayList();
        ctxList.add(context);
        docket.securityContexts(ctxList);

        return docket;
    }
}
