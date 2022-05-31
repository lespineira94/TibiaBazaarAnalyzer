package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class CharacterAuctionDataDTO implements Serializable {

    private CharacterDTO character;
    private AuctionDataDTO auctionData;

}
