package com.lespineira94.tibiabazaaranalyzer.scrapper.impl;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.AuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.parser.AuctionHeaderInfoBean;
import com.sun.jdi.InternalException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ScrapperImpl implements Scrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperImpl.class.getName());

    private static final String CURRENT_AUCTIONS_BASE_URL = "https://www.tibia.com/charactertrade/?subtopic=currentcharactertrades&currentpage=";
    private static final Integer NUMBER_OF_CHARACTERS_PER_PAGE = 25;

    @Autowired
    private WebClient webClient;

    @Override
    public String doScrap() throws IOException, ParseException {
        final String result = "";

        final List<CharacterAuctionDataBean> characterBeanList = this.parseData();

        LOGGER.info("Characters parsed: {}", characterBeanList);
        LOGGER.info("Number of characters obtained: {}", characterBeanList.size());

        return result;
    }

    private List<CharacterAuctionDataBean> parseData() throws IOException, ParseException {
        final List<CharacterBean> characterBeanList = new ArrayList<>();
        final List<AuctionDataBean> auctionDataBeanList = new ArrayList<>();

        // Gets the total of characters for sale and then calculates the number of pages to parse by partitioning by the number of characters per page(by default 25)
        final List<Integer> numberOfPages = this.getNumberOfPages();

        for (final Integer pageNumber : numberOfPages) {
            final String currentPageUrl = CURRENT_AUCTIONS_BASE_URL.concat(String.valueOf(pageNumber));
            final HtmlPage htmlPage = this.webClient.getPage(currentPageUrl);

            final List<CharacterBean> characterBeanListPerPage = this.parseCharacterData(htmlPage);
            characterBeanList.addAll(characterBeanListPerPage);

            final List<AuctionDataBean> auctionDataBeanPerPage = this.parseAuctionData(htmlPage);
            auctionDataBeanList.addAll(auctionDataBeanPerPage);
        }

        return this.mergeCharacterAndAuctionData(characterBeanList, auctionDataBeanList);
    }

    private List<CharacterAuctionDataBean> mergeCharacterAndAuctionData(final List<CharacterBean> characterBeanList, final List<AuctionDataBean> auctionDataBeanList) {
        final List<CharacterAuctionDataBean> characterAuctionDataBeanList = new ArrayList<>();

        for (int i = 0; i < characterBeanList.size(); i++) {
            final CharacterBean characterBean = characterBeanList.get(i);
            final AuctionDataBean auctionDataBean = auctionDataBeanList.get(i);

            final CharacterAuctionDataBean characterAuctionDataBean = CharacterAuctionDataBean.builder().character(characterBean).auctionData(auctionDataBean).build();
            characterAuctionDataBeanList.add(characterAuctionDataBean);
        }

        return characterAuctionDataBeanList;
    }

    private List<AuctionDataBean> parseAuctionData(final HtmlPage htmlPage) throws ParseException {
        final List<AuctionDataBean> auctionDataBeanList = new ArrayList<>();

        final List<DomText> auctionDatesElements = htmlPage.getByXPath("//div[contains(@class, 'ShortAuctionDataValue')]/text()");
        final List<String> auctionDatesList = auctionDatesElements.stream().map(DomNode::getTextContent).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        this.validateAuctionTimersList(auctionDatesList);

        // Loop for every 2 elements so i can get start date and end date in pairs. Start date is the i and end date i+1
        for (int i = 0; i < auctionDatesList.size(); i += 2) {
            final AuctionDataBean auctionDataBean = AuctionDataBean.builder().build();

            // Parse start date
            final String startDate = auctionDatesList.get(i);
            auctionDataBean.setStartDate(startDate);
            // Parse end date
            final String endDate = auctionDatesList.get(i + 1);
            auctionDataBean.setEndDate(endDate);

            auctionDataBeanList.add(auctionDataBean);
        }

        // Bids
        final List<String> minimumBids = this.getBids(htmlPage, auctionDataBeanList);

        for (int i = 0; i < minimumBids.size(); i++) {
            final String bid = minimumBids.get(i);
            final AuctionDataBean auctionDataBean = auctionDataBeanList.get(i);
            auctionDataBean.setMinimumBid(Integer.parseInt(bid));

        }

        return auctionDataBeanList;
    }

    private List<String> getBids(final HtmlPage htmlPage, final List<AuctionDataBean> auctionDataBeanList) {
        final List<DomText> minimumBidsElement = htmlPage.getByXPath("//div[contains(@class,'ShortAuctionDataValue')]/b/text()");
        final List<String> minimumBids = minimumBidsElement.stream().map(domText ->
                domText.getTextContent().replace(",", "")
        ).collect(Collectors.toList());
        this.validateMinimumBids(minimumBids, auctionDataBeanList);

        return minimumBids;
    }

    private void validateMinimumBids(final List<String> minimumBids, final List<AuctionDataBean> auctionDataBeanList) {
        Validate.notEmpty(minimumBids);
        Validate.notEmpty(auctionDataBeanList);

        Validate.noNullElements(minimumBids);
        Validate.noNullElements(auctionDataBeanList);

        final boolean areListsValids = minimumBids.size() == auctionDataBeanList.size();
        if (!areListsValids) {
            throw new InternalException("AcutionDataLists are invalid");
        }
    }

    private void validateAuctionTimersList(final List<String> auctionTimers) {
        //TODO out to another class validations
        if (auctionTimers.size() % 2 != 0) {
            throw new InternalException("The auction data should be divisible by 2");
        }
    }

    private List<CharacterBean> parseCharacterData(final HtmlPage htmlPage) {
        final List<CharacterBean> characterBeanList = new ArrayList<>();

        // Character names
        final List<String> characterNames = this.getCharacterNames(htmlPage);
        // Info about levels, vocations and genders
        final AuctionHeaderInfoBean auctionHeaderInfo = this.getAuctionHeaderInfo(htmlPage);
        // Worlds
        final List<String> worlds = this.getWorlds(htmlPage);
        // Character images
        final List<String> imageUrls = this.getImageUrls(htmlPage);

        // Validates all the lists and adds all the data to the return list
        this.validateAndAddCharacterDataToList(characterBeanList, characterNames, auctionHeaderInfo, worlds, imageUrls);

        return characterBeanList;
    }

    private List<String> getImageUrls(final HtmlPage htmlPage) {
        final List<String> imageUrls = new ArrayList<>();

        final List<DomAttr> imageUrlsElements = htmlPage.getByXPath("//img[contains(@class, 'AuctionOutfitImage')]/@src");
        imageUrlsElements.forEach(urlElement -> imageUrls.add(urlElement.getTextContent()));

        return imageUrls;
    }

    private void validateAndAddCharacterDataToList(final List<CharacterBean> characterBeanList, final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls) {
        //Validates that in all the character data lists are the same number of elements, so i know that im not missing any data
        this.validateLists(characterNames, auctionHeaderInfo, worlds, imageUrls);

        this.addDataToList(characterBeanList, characterNames, auctionHeaderInfo, worlds, imageUrls);
    }

    private void addDataToList(final List<CharacterBean> characterBeanList, final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls) {
        for (int i = 0; i < characterNames.size(); i++) {
            final String name = characterNames.get(i);
            final Integer level = auctionHeaderInfo.getLevels().get(i);
            final String vocation = auctionHeaderInfo.getVocations().get(i);
            final String gender = auctionHeaderInfo.getGenders().get(i);
            final String world = worlds.get(i);
            final String imageUrl = imageUrls.get(i);

            final CharacterBean characterBean = CharacterBean.builder()
                    .name(name)
                    .level(level)
                    .vocation(vocation)
                    .gender(gender)
                    .world(world)
                    .imgUrl(imageUrl)
                    .build();
            characterBeanList.add(characterBean);
        }
    }

    private void validateLists(final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls) {
        final List<Integer> levels = auctionHeaderInfo.getLevels();
        final List<String> vocations = auctionHeaderInfo.getVocations();
        final List<String> genders = auctionHeaderInfo.getGenders();

        Validate.notEmpty(characterNames);
        Validate.notEmpty(levels);
        Validate.notEmpty(vocations);
        Validate.notEmpty(genders);
        Validate.notEmpty(worlds);
        Validate.notEmpty(imageUrls);

        Validate.noNullElements(characterNames);
        Validate.noNullElements(levels);
        Validate.noNullElements(vocations);
        Validate.noNullElements(genders);
        Validate.noNullElements(worlds);
        Validate.noNullElements(imageUrls);

        final int size = characterNames.size();

        final boolean areListsSameSize = levels.size() == size && vocations.size() == size && genders.size() == size && worlds.size() == size && imageUrls.size() == size;
        if (!areListsSameSize) {
            throw new IllegalArgumentException("The validated lists have not the same size");
        }

    }

    private List<String> getWorlds(final HtmlPage htmlPage) {
        final List<String> worlds = new ArrayList<>();

        final List<DomText> worldElements = htmlPage.getByXPath("//div[contains(@class, 'AuctionHeader')]/a/text()");
        worldElements.forEach(world -> worlds.add(world.getTextContent()));

        return worlds;
    }

    private AuctionHeaderInfoBean getAuctionHeaderInfo(final HtmlPage htmlPage) {
        final List<Integer> levels = new ArrayList<>();
        final List<String> vocations = new ArrayList<>();
        final List<String> genders = new ArrayList<>();

        final AuctionHeaderInfoBean auctionHeaderInfo = AuctionHeaderInfoBean.builder().levels(levels).vocations(vocations).genders(genders).build();

        final List<DomText> characterInfoElements = htmlPage.getByXPath("//div[contains(@class, 'AuctionHeader')]/text()");
        for (final DomText characterInfoElement : characterInfoElements) {
            final String characterInfo = characterInfoElement.getTextContent();
            final String[] characterInfoSplited = characterInfo.replace(" ", "").split("\\|");

            // Level (1 -> x)
            final Integer level = Integer.parseInt(characterInfoSplited[0].split(":")[1]);
            levels.add(level);

            // Vocation (EK,MS,ED,RP)
            final String vocation = characterInfoSplited[1].split(":")[1];
            vocations.add(vocation);

            // Gender (Male, Female)
            final String gender = characterInfoSplited[2];
            genders.add(gender);
        }

        return auctionHeaderInfo;
    }


    private List<String> getCharacterNames(final HtmlPage htmlPage) {
        final List<String> characterNames = new ArrayList<>();

        final List<DomText> auctionCharacterNamesElements = htmlPage.getByXPath("//div[contains(@class, 'AuctionCharacterName')]/a/text()");
        auctionCharacterNamesElements.forEach(auctionCharacterNameElement -> characterNames.add(auctionCharacterNameElement.getTextContent()));

        return characterNames;
    }

    private List<Integer> getNumberOfPages() throws IOException {
        final Integer totalResults = this.getTotalResults();

        final int numberOfPages = totalResults / NUMBER_OF_CHARACTERS_PER_PAGE;

        return IntStream.range(1, numberOfPages).boxed().collect(Collectors.toList());
    }

    private Integer getTotalResults() throws IOException {
        final int totalResults;

        final HtmlPage htmlPage = this.webClient.getPage(CURRENT_AUCTIONS_BASE_URL.concat("1"));
        final DomText resultsText = htmlPage.getFirstByXPath("//div[contains(@style, 'float: right;')]/b/text()");

        totalResults = Integer.parseInt(resultsText.getTextContent().split(":")[1].trim().replace(",", ""));

        return totalResults;
    }

}
