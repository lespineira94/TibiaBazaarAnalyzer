package com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.services.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Service
public class ScrapperServiceImpl implements ScrapperService {

    @Autowired
    private Scrapper scrapper;

    @Override
    public List<CharacterAuctionDataDTO> getCharacterAuctionDataList() throws IOException, ParseException {
        final List<CharacterAuctionDataBean> characterAuctionDataBeanList = this.scrapper.doScrap();
        
        return null;
    }
}
