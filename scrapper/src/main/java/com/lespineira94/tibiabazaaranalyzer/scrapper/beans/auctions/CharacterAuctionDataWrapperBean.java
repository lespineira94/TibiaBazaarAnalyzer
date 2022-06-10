package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CharacterAuctionDataWrapperBean {

    List<CharacterAuctionDataBean> charactersAuctionData;
}
