package me.yangbajing.share2016.data;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * LastModifiedDate、CreatedDate等注解需要添加EnableMongoAuditing才可生效
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
@Configuration
@EnableMongoAuditing
public class MongoDbConfig {
}
