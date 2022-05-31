package com.lespineira94.tibiabazaaranalyzer.boot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.lespineira94.tibiabazaaranalyzer.ms.controllers", "com.lespineira94.tibiabazaaranalyzer.scrapper"})
public class AppConfiguration {


}
