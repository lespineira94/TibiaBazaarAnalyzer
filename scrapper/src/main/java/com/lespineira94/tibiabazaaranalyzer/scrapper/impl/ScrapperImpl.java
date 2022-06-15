package com.lespineira94.tibiabazaaranalyzer.scrapper.impl;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.lespineira94.tibiabazaaranalyzer.scrapper.Scrapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.AuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.parser.AuctionHeaderInfoBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.util.ScrapperUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ScrapperImpl implements Scrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperImpl.class.getName());
    private static final String NO_BREAK_SPACE = "[\\s\u00A0]+";
    private static final String CURRENT_AUCTIONS_BASE_URL = "https://www.tibia.com/charactertrade/?subtopic=currentcharactertrades&currentpage=";
    private static final Integer NUMBER_OF_CHARACTERS_PER_PAGE = 25;
    private static final Integer RETRIES = 3;

    @Autowired
    private WebClient webClient;

    @Override
    public List<CharacterAuctionDataBean> doScrap() throws IOException, ParseException {
        final List<CharacterAuctionDataBean> characterBeanList = this.parseData();

        LOGGER.info("Characters parsed: {}", characterBeanList);
        LOGGER.info("Number of characters obtained: {}", characterBeanList.size());

        return characterBeanList;
    }

    private List<CharacterAuctionDataBean> parseData() throws IOException, ParseException {
        final List<CharacterBean> characterBeanList = new ArrayList<>();
        final List<AuctionDataBean> auctionDataBeanList = new ArrayList<>();
        final List<String> pageUrlErrors = new ArrayList<>();

        // Gets the total of characters for sale and then calculates the number of pages to parse by partitioning by the number of characters per page(by default 25)
        //TODO eliminar
        final List<Integer> numberOfPages = Arrays.asList(1, 2, 3, 4, 5);

        for (final Integer pageNumber : numberOfPages) {
            final String currentPageUrl = CURRENT_AUCTIONS_BASE_URL.concat(String.valueOf(pageNumber));

            try {
                this.parseHtmlPage(characterBeanList, auctionDataBeanList, currentPageUrl);
            } catch (final FailingHttpStatusCodeException e) {
                pageUrlErrors.add(currentPageUrl);
            }
        }

        // If there was any error, it will try to parse the error pages
        this.checkIfRetryToParseData(characterBeanList, auctionDataBeanList, pageUrlErrors);

        return this.mergeCharacterAndAuctionData(characterBeanList, auctionDataBeanList);
    }

    private void checkIfRetryToParseData(final List<CharacterBean> characterBeanList, final List<AuctionDataBean> auctionDataBeanList, final List<String> pageUrlErrors) throws IOException, ParseException {
        //TODO
     /*   final boolean doRetryParseData = !CollectionUtils.isEmpty(pageUrlErrors);

        if (doRetryParseData) {
            final AtomicInteger currentRetry = new AtomicInteger(1);
            final List<String> pagesReparsedOK = new ArrayList<>();

            while (currentRetry.get() <= RETRIES || (currentRetry.get() < RETRIES && CollectionUtils.isEmpty(pageUrlErrors))) {
                final AtomicInteger errors = new AtomicInteger(0);
                pageUrlErrors.forEach(pageUrl -> {
                    try {
                        this.parseHtmlPage(characterBeanList, auctionDataBeanList, pageUrl);
                        pagesReparsedOK.add(pageUrl);
                    } catch (final Exception e) {
                        errors.getAndIncrement();

                        final boolean doIncrementRetries = pageUrlErrors.size() == (pagesReparsedOK.size() + errors.get());
                        if (doIncrementRetries) {
                            currentRetry.getAndIncrement();
                        }
                    }
                });

                // Checks the parsed pages that now were ok, and removes from the list to break the loop
                pageUrlErrors.removeAll(pagesReparsedOK);
            }
        }*/
    }

    private void parseHtmlPage(final List<CharacterBean> characterBeanList, final List<AuctionDataBean> auctionDataBeanList, final String currentPageUrl) throws IOException {
        final HtmlPage htmlPage = this.webClient.getPage(currentPageUrl);

        final List<CharacterBean> characterBeanListPerPage = this.parseCharacterData(htmlPage);
        characterBeanList.addAll(characterBeanListPerPage);

        final List<AuctionDataBean> auctionDataBeanPerPage = this.parseAuctionData(htmlPage);
        auctionDataBeanList.addAll(auctionDataBeanPerPage);
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

    private List<AuctionDataBean> parseAuctionData(final HtmlPage htmlPage) {
        final List<AuctionDataBean> auctionDataBeanList = new ArrayList<>();
        final List<DomText> auctionDatesElements = htmlPage.getByXPath("//div[contains(@class, 'ShortAuctionDataValue')]/text()");
        final List<String> auctionDatesList = auctionDatesElements.stream().map(DomNode::getTextContent).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        final List<LocalDateTime> datesList = this.getAuctionDates(auctionDatesList);

        this.validateAuctionTimersList(datesList);

        // Loop for every 2 elements so i can get start date and end date in pairs. Start date is the i and end date i+1
        for (int i = 0; i < datesList.size(); i += 2) {
            final AuctionDataBean auctionDataBean = AuctionDataBean.builder().build();

            // Parse start date
            final LocalDateTime startDate = datesList.get(i);
            auctionDataBean.setStartDate(startDate);
            // Parse end date
            final LocalDateTime endDate = datesList.get(i + 1);
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

    private List<LocalDateTime> getAuctionDates(final List<String> auctionDatesList) {
        return auctionDatesList.stream().map(auctiondatesString -> {
            final String splittedByCommas = auctiondatesString.replaceAll(NO_BREAK_SPACE, ";").replace(",", "");

            final String[] datesSplitted = splittedByCommas.split(";");
            try {
                final String monthString = datesSplitted[0];
                final Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(monthString);

                final int day = Integer.parseInt(datesSplitted[1]);
                final int year = Integer.parseInt(datesSplitted[2]);
                final int month = date.getMonth() + 1;

                final String time = datesSplitted[3];
                final String[] timeSplitted = time.split(":");
                final int hour = Integer.parseInt(timeSplitted[0]);
                final int minutes = Integer.parseInt(timeSplitted[1]);

                return LocalDateTime.of(year, month, day, hour, minutes);

            } catch (final ParseException e) {
                throw new InternalError("Error parsing dates", e);
            }
        }).collect(Collectors.toList());
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
            throw new InternalError("AcutionDataLists are invalid");
        }
    }

    private void validateAuctionTimersList(final List<LocalDateTime> auctionTimers) {
        //TODO out to another class validations
        if (auctionTimers.size() % 2 != 0) {
            throw new InternalError("The auction data should be divisible by 2");
        }
    }

    private List<CharacterBean> parseCharacterData(final HtmlPage htmlPage) {
        final List<CharacterBean> characterBeanList = new ArrayList<>();

        // Character names
        final List<String> characterNames = ScrapperUtil.getCharacterInfoStringListByXPathExpression(htmlPage, "//div[contains(@class, 'AuctionCharacterName')]/a/text()");
        // Character info url
        final List<String> charactersInfoUrls = ScrapperUtil.getCharacterInfoStringListByXPathExpression(htmlPage, "//div[contains(@class, 'AuctionCharacterName')]/a/@href");
        // Info about levels, vocations and genders
        final AuctionHeaderInfoBean auctionHeaderInfo = this.getAuctionHeaderInfo(htmlPage);
        // Worlds
        final List<String> worlds = ScrapperUtil.getCharacterInfoStringListByXPathExpression(htmlPage, "//div[contains(@class, 'AuctionHeader')]/a/text()");
        // Character images
        final List<String> imageUrls = ScrapperUtil.getCharacterInfoStringListByXPathExpression(htmlPage, "//img[contains(@class, 'AuctionOutfitImage')]/@src");

        // Validates all the lists and adds all the data to the return list
        this.validateAndAddCharacterDataToList(characterBeanList, characterNames, auctionHeaderInfo, worlds, imageUrls, charactersInfoUrls);

        return characterBeanList;
    }

    private void validateAndAddCharacterDataToList(final List<CharacterBean> characterBeanList, final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls, final List<String> charactersInfoUrls) {
        //Validates that in all the character data lists are the same number of elements, so i know that im not missing any data
        this.validateLists(characterNames, auctionHeaderInfo, worlds, imageUrls, charactersInfoUrls);

        this.addDataToList(characterBeanList, characterNames, auctionHeaderInfo, worlds, imageUrls, charactersInfoUrls);
    }

    private void addDataToList(final List<CharacterBean> characterBeanList, final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls, final List<String> charactersInfoUrls) {
        for (int i = 0; i < characterNames.size(); i++) {
            final String name = characterNames.get(i);
            final Integer level = auctionHeaderInfo.getLevels().get(i);
            final String vocation = auctionHeaderInfo.getVocations().get(i);
            final String gender = auctionHeaderInfo.getGenders().get(i);
            final String world = worlds.get(i);
            final String imageUrl = imageUrls.get(i);
            final String characterInfoUrl = charactersInfoUrls.get(i);

            final CharacterBean characterBean = CharacterBean.builder()
                    .name(name)
                    .level(level)
                    .vocation(vocation)
                    .gender(gender)
                    .world(world)
                    .imgUrl(imageUrl)
                    .characterInfoUrl(characterInfoUrl)
                    .build();
            characterBeanList.add(characterBean);
        }
    }

    private void validateLists(final List<String> characterNames, final AuctionHeaderInfoBean auctionHeaderInfo, final List<String> worlds, final List<String> imageUrls, final List<String> charactersInfoUrls) {
        final List<Integer> levels = auctionHeaderInfo.getLevels();
        final List<String> vocations = auctionHeaderInfo.getVocations();
        final List<String> genders = auctionHeaderInfo.getGenders();

        Validate.notEmpty(characterNames);
        Validate.notEmpty(levels);
        Validate.notEmpty(vocations);
        Validate.notEmpty(genders);
        Validate.notEmpty(worlds);
        Validate.notEmpty(imageUrls);
        Validate.notEmpty(charactersInfoUrls);

        Validate.noNullElements(characterNames);
        Validate.noNullElements(levels);
        Validate.noNullElements(vocations);
        Validate.noNullElements(genders);
        Validate.noNullElements(worlds);
        Validate.noNullElements(imageUrls);
        Validate.noNullElements(charactersInfoUrls);

        final int size = characterNames.size();

        final boolean areListsSameSize = levels.size() == size && vocations.size() == size && genders.size() == size && worlds.size() == size && imageUrls.size() == size && charactersInfoUrls.size() == size;
        if (!areListsSameSize) {
            throw new IllegalArgumentException("The validated lists have not the same size");
        }

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
