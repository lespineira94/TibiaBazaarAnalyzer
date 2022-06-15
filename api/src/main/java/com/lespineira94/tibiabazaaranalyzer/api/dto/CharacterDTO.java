package com.lespineira94.tibiabazaaranalyzer.api.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
@ToString
public class CharacterDTO implements Serializable {

    private String name;
    private Integer level;
    private String vocation;
    private String gender;
    private String world;
    private String imgUrl;
    private String characterInfoUrl;

}
