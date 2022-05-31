package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionDataDTO {

    private Date startDate;
    private Date endDate;
    private Integer minimumBid;
}
