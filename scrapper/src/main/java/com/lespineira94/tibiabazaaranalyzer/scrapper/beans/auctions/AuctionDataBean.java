package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionDataBean {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minimumBid;
}
