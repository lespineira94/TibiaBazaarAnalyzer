package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class CharacterAuctionDataDTO {

    private CharacterDTO character;
    private AuctionDataDTO auctionData;

}
