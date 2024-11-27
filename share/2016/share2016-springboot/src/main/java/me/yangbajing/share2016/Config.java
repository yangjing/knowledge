package me.yangbajing.share2016;

import me.yangbajing.share2016.common.JsonHelpers;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * Spring 应用总体配置
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
@Configuration
public class Config extends WebMvcConfigurationSupport {

    /**
     * 设置Spring Web MVC默认返回数据的转换方式：Json
     *
     * @param converters
     */
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(JsonHelpers.objectMapper);
        converters.add(converter);
    }
}
