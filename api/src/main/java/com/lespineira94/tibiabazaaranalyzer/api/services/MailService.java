package com.lespineira94.tibiabazaaranalyzer.api.services;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;

public interface MailService {

    void sendMail(CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO);
}
