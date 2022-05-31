package com.lespineira94.tibiabazaaranalyzer.ms.controllers;

import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
public class CharactersController {

    @Autowired
    private Scrapper scrapper;

    @GetMapping("/characters")
    ResponseEntity<String> getAllCharactersData() {
        String data = "";

        try {
            data = this.scrapper.doScrap();
        } catch (final IOException | ParseException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(data);
    }
}
