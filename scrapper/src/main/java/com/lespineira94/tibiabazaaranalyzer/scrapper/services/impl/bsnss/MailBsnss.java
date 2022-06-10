package com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl.bsnss;

import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.AuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataWrapperBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterBean;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MailBsnss {

    private static MailBsnss instance;

    private MailBsnss() {
        //Not implemented
    }

    public static MailBsnss getInstance() {
        if (instance == null) {
            instance = new MailBsnss();
        }

        return instance;
    }

    public CharacterAuctionDataWrapperBean getCharactersThatAuctionEndDateLessThanConfigMinutesLeft(final Integer minutesLimitToSendMail, final CharacterAuctionDataWrapperBean characterAuctionDataWrapperBean,
                                                                                                    final Integer minLevel, final Integer maxLevel, final Integer maxTibiaCoins) {
        CharacterAuctionDataWrapperBean result = null;
        final List<CharacterAuctionDataBean> charactersAuctionData = characterAuctionDataWrapperBean.getCharactersAuctionData();

        if (!CollectionUtils.isEmpty(charactersAuctionData)) {
            final Predicate<CharacterAuctionDataBean> filterByAuctionLeftTimeCharacters = this.getFilterByAucntionLeftTimeAndLevel(minutesLimitToSendMail, minLevel, maxLevel, maxTibiaCoins);
            final List<CharacterAuctionDataBean> characterAuctionsWithLessThanConfigMinutes = charactersAuctionData.stream()
                    .filter(filterByAuctionLeftTimeCharacters)
                    .collect(Collectors.toList());

            result = CharacterAuctionDataWrapperBean.builder().charactersAuctionData(characterAuctionsWithLessThanConfigMinutes).build();
        }

        return result;
    }

    private Predicate<CharacterAuctionDataBean> getFilterByAucntionLeftTimeAndLevel(final Integer minutesLimitToSendMail, final Integer minLevel, final Integer maxLevel, final Integer maxTibiaCoins) {
        return characterAuctionDataWrapper -> {
            final AuctionDataBean auctionData = characterAuctionDataWrapper.getAuctionData();
            final long minutesOfAuctionLeft = this.getMinutesOfAuctionLeft(auctionData);
            final boolean isValidAuctionTime = minutesOfAuctionLeft <= minutesLimitToSendMail;

            final CharacterBean character = characterAuctionDataWrapper.getCharacter();
            final Integer level = character.getLevel();
            final boolean isValidLevel = level <= maxLevel && level >= minLevel;

            final boolean isValidBid = auctionData.getMinimumBid() <= maxTibiaCoins;

            return isValidAuctionTime && isValidLevel && isValidBid;
        };
    }

    private long getMinutesOfAuctionLeft(final AuctionDataBean auctionData) {
        final LocalDateTime endDate = auctionData.getEndDate();
        final LocalDateTime currentDate = LocalDateTime.now();
        final long minutesOfDifference = Math.abs(ChronoUnit.MINUTES.between(endDate, currentDate));
        return minutesOfDifference;
    }
}
