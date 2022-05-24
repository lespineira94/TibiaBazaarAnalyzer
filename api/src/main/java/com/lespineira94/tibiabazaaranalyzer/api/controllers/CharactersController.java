package com.lespineira94.tibiabazaaranalyzer.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CharactersController {

    @GetMapping("/characters")
    String getAllCharactersData() {
        return "not implemented";
    }
}
