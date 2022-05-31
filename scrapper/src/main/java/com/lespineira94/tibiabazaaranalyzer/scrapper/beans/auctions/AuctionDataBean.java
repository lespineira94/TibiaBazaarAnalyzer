package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionDataBean {

    private String startDate;
    private String endDate;
    private Integer minimumBid;
}
