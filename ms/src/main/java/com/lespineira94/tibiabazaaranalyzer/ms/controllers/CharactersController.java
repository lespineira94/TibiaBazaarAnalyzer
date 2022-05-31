package com.lespineira94.tibiabazaaranalyzer.ms.controllers;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;
import com.lespineira94.tibiabazaaranalyzer.api.services.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
public class CharactersController {

    @Autowired
    private ScrapperService scrapperService;

    @GetMapping("/characters")
    ResponseEntity<CharacterAuctionDataWrapperDTO> getAllCharactersData() {
        CharacterAuctionDataWrapperDTO data = new CharacterAuctionDataWrapperDTO();

        try {
            data = this.scrapperService.getCharacterAuctionDataList();
        } catch (final IOException | ParseException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(data);
    }
}
