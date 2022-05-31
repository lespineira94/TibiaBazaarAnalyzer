package com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
@ToString
public class CharacterBean {

    private String name;
    private Integer level;
    private String vocation;
    private String gender;
    private String world;
    private String imgUrl;

}
