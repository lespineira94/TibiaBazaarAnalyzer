package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionDataDTO implements Serializable {

    private String startDate;
    private String endDate;
    private Integer minimumBid;
}
