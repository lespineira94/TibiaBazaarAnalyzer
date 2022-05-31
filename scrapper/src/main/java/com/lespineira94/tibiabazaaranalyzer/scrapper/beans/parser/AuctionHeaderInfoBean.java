package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.parser;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class AuctionHeaderInfoBean {

    private List<Integer> levels;
    private List<String> vocations;
    private List<String> genders;
    
}
