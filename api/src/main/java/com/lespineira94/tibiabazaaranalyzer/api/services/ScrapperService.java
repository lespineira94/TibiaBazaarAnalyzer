package com.lespineira94.tibiabazaaranalyzer.api.services;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;

import java.io.IOException;
import java.text.ParseException;

public interface ScrapperService {
    CharacterAuctionDataWrapperDTO getCharacterAuctionDataList() throws IOException, ParseException;
}
