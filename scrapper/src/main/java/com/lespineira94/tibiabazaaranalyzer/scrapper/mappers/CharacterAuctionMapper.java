package com.lespineira94.tibiabazaaranalyzer.scrapper.mappers;

import com.lespineira94.tibiabazaaranalyzer.api.dto.AuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterDTO;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.AuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterBean;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CharacterAuctionMapper {

    CharacterBean toCharacterBean(CharacterDTO dto);

    CharacterDTO toCharacterDTO(CharacterBean bean);

    List<CharacterBean> toCharacterBeanList(List<CharacterDTO> dtoList);

    List<CharacterDTO> toCharacterDTOList(List<CharacterBean> characterBeanList);

    AuctionDataBean toAuctionDataBean(AuctionDataDTO dto);

    AuctionDataDTO toAuctionDataDTO(AuctionDataBean bean);

    List<AuctionDataBean> toAuctionDataBeanList(List<AuctionDataDTO> dtoList);

    List<AuctionDataDTO> toAuctionDataDTOList(List<AuctionDataBean> characterBeanList);

    CharacterAuctionDataBean toCharacterAuctionDataBean(CharacterAuctionDataDTO dto);

    CharacterAuctionDataDTO toCharacterAuctionDataDTO(CharacterAuctionDataBean bean);

    List<CharacterAuctionDataBean> toCharacterAuctionDataBeanList(List<CharacterAuctionDataDTO> dto);

    List<CharacterAuctionDataDTO> toCharacterAuctionDataDTOList(List<CharacterAuctionDataBean> bean);

}
