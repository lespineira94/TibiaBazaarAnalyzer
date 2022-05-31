package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CharacterAuctionDataWrapperDTO implements Serializable {

    List<CharacterAuctionDataDTO> charactersAuctionData;
}
