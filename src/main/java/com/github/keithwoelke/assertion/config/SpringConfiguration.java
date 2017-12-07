package com.github.keithwoelke.assertion.config;

import com.github.keithwoelke.assertion.TestNGAssertionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@ComponentScan(basePackages = "com.sinclairdigital.qa.assertion")
@Configuration
public class SpringConfiguration {

    @Bean
    public TestNGAssertionListener testNGAssertionListener() {
        return new TestNGAssertionListener();
    }
}
