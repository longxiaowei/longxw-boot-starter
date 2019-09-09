package com.longxw.boot.starter.updater;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = UpdaterDataSourceProperties.class)
public class UpdaterConfiguration {

    @Bean
    public UpdaterListener updaterListener(){
        return new UpdaterListener();
    }
}
