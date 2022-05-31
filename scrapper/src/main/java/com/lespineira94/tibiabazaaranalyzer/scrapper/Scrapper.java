package com.lespineira94.tibiabazaaranalyzer.scrapper;

import java.io.IOException;
import java.text.ParseException;

public interface Scrapper {

    String doScrap() throws IOException, ParseException;
}
