package com.lespineira94.tibiabazaaranalyzer.scrapper.services;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface ScrapperService {
    List<CharacterAuctionDataDTO> getCharacterAuctionDataList() throws IOException, ParseException;
}
