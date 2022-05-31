package com.lespineira94.tibiabazaaranalyzer.scrapper;

import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface Scrapper {

    List<CharacterAuctionDataBean> doScrap() throws IOException, ParseException;
}
