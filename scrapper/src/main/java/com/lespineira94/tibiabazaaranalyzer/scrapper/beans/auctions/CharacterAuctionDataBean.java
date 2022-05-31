package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class CharacterAuctionDataBean {

    private CharacterBean character;
    private AuctionDataBean auctionData;

}
