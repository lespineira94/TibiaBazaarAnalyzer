package com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;
import com.lespineira94.tibiabazaaranalyzer.api.services.ScrapperService;
import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.mappers.CharacterAuctionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Service
public class ScrapperServiceImpl implements ScrapperService {

    @Autowired
    private Scrapper scrapper;

    @Autowired
    private CharacterAuctionMapper characterAuctionMapper;

    @Override
    public CharacterAuctionDataWrapperDTO getCharacterAuctionDataList() throws IOException, ParseException {
        final CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO = new CharacterAuctionDataWrapperDTO();
        final List<CharacterAuctionDataBean> characterAuctionDataBeanList = this.scrapper.doScrap();

        final List<CharacterAuctionDataDTO> characterAuctionDataDTOList = this.characterAuctionMapper.toCharacterAuctionDataDTOList(characterAuctionDataBeanList);
        characterAuctionDataWrapperDTO.setCharactersAuctionData(characterAuctionDataDTOList);

        return characterAuctionDataWrapperDTO;
    }
}
