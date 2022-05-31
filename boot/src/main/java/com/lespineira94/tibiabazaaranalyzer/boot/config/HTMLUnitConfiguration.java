package com.lespineira94.tibiabazaaranalyzer.boot.config;

import com.gargoylesoftware.htmlunit.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HTMLUnitConfiguration {

    private static final Integer TIMEOUT = 10000;

    @Bean
    public WebClient webClient() {
        final WebClient webClient = new WebClient();

        //TODO implement silent listener
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(TIMEOUT);
        webClient.getOptions().setTimeout(TIMEOUT);

        return webClient;
    }
}
