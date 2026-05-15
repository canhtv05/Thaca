package com.thaca.notification.configs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class EmailTemplateConfig {

    @Value("${app.base-url:http://localhost:1003}")
    private String appBaseUrl;

    @Bean
    ClassLoaderTemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean
    SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(emailTemplateResolver());
        engine.addDialect(new GlobalVariableDialect(appBaseUrl));
        return engine;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static class GlobalVariableDialect extends AbstractDialect implements IExpressionObjectDialect {

        private final String baseUrl;

        public GlobalVariableDialect(String baseUrl) {
            super("GlobalVariableDialect");
            this.baseUrl = baseUrl;
        }

        @Override
        public IExpressionObjectFactory getExpressionObjectFactory() {
            return new IExpressionObjectFactory() {
                @Override
                public Set<String> getAllExpressionObjectNames() {
                    return Collections.singleton("app");
                }

                @Override
                public Object buildObject(IExpressionContext context, String expressionObjectName) {
                    if ("app".equals(expressionObjectName)) {
                        Map<String, Object> app = new HashMap<>();
                        app.put("baseUrl", baseUrl);
                        app.put("logoUrl", baseUrl + "/assets/images/logo.png");
                        app.put("systemName", "ThaCa");
                        return app;
                    }
                    return null;
                }

                @Override
                public boolean isCacheable(String expressionObjectName) {
                    return true;
                }
            };
        }
    }
}
