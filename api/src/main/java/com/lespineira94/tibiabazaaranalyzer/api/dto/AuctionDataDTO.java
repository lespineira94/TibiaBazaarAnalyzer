package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionDataDTO implements Serializable {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minimumBid;
}
