package com.tgl.redis.migrate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket testApi() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("test").genericModelSubstitutes(DeferredResult.class)
				// .genericModelSubstitutes(ResponseEntity.class)
				.useDefaultResponseMessages(false).forCodeGeneration(true).pathMapping("/")// base，最终调用接口后会和paths拼接在一起
				.select().paths(PathSelectors.any())// 过滤的接口
				.build().apiInfo(testApiInfo());
	}

	private ApiInfo testApiInfo() {
		ApiInfo apiInfo = new ApiInfo("Electronic Health Record(EHR) Platform API", // 大标题
				"EHR Platform's REST API, all the applications could access the Object model data via JSON.", // 小标题
				"0.1", // 版本
				"NO terms of service", "365384722@qq.com", // 作者
				"The Apache License, Version 2.0", // 链接显示文字
				"http://www.apache.org/licenses/LICENSE-2.0.html"// 网站链接
		);

		return apiInfo;
	}

}
