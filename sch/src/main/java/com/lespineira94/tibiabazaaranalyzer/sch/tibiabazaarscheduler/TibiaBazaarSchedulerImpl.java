package com.lespineira94.tibiabazaaranalyzer.sch.tibiabazaarscheduler;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;
import com.lespineira94.tibiabazaaranalyzer.api.services.MailService;
import com.lespineira94.tibiabazaaranalyzer.api.services.ScrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;

@Component
public class TibiaBazaarSchedulerImpl implements ITibiaBazaarScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TibiaBazaarSchedulerImpl.class);

    @Autowired
    private ScrapperService scrapperService;

    @Autowired
    private MailService mailService;

    @Override
    @Scheduled(cron = "${app.scheduler.tibiabazaar.cronExpresion}")
    public void scrapTrask() {
        LOGGER.info("Starting tibiabazaarTask at: {}", LocalDateTime.now());
        try {
            final CharacterAuctionDataWrapperDTO characterAuctionDataList = this.scrapperService.getCharacterAuctionDataList();
            this.mailService.sendMail(characterAuctionDataList);
            LOGGER.info("Task finished succesfully at: {}", LocalDateTime.now());
        } catch (final IOException | ParseException e) {
            LOGGER.error("Error executing task at: {}", LocalDateTime.now());
        }
    }
}
