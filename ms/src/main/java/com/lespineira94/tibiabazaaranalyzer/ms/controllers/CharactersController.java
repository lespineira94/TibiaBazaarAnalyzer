package com.lespineira94.tibiabazaaranalyzer.ms.controllers;

import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CharactersController {

    @Autowired
    private Scrapper scrapper;

    @GetMapping("/characters")
    ResponseEntity<List<CharacterAuctionDataBean>> getAllCharactersData() {
        List<CharacterAuctionDataBean> data = new ArrayList<>();

        try {
            data = this.scrapper.doScrap();
        } catch (final IOException | ParseException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(data);
    }
}
