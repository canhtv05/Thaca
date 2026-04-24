package com.thaca.gateway;

import com.thaca.framework.core.annotations.ServletOnly;
import com.thaca.framework.core.configs.FrameworkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
    basePackages = "com.thaca",
    excludeFilters = { @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ServletOnly.class) }
)
@EnableConfigurationProperties(FrameworkProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
