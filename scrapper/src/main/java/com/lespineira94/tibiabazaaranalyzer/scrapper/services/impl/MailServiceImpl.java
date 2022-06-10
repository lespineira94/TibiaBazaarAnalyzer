package com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl;

import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;
import com.lespineira94.tibiabazaaranalyzer.api.services.MailService;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataWrapperBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.mappers.CharacterAuctionMapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl.bsnss.MailBsnss;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private CharacterAuctionMapper characterAuctionMapper;

    @Value("${app.mail.enabled}")
    private boolean sendMailEnabled;

    @Value("${app.mail.minutesLimitToSendMail}")
    private Integer minutesLimitToSendMail;

    @Value("${app.mail.minLevel}")
    private Integer minLevel;

    @Value("${app.mail.maxLevel}")
    private Integer maxLevel;

    @Value("${app.mail.maxTibiaCoins}")
    private Integer maxTibiaCoins;

    @Override
    public void sendMail(final CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO) {
        final CharacterAuctionDataWrapperBean characterAuctionDataWrapperBean = this.characterAuctionMapper.toCharacterAuctionDataWrapperBean(characterAuctionDataWrapperDTO);
        final MailBsnss mailBsnss = MailBsnss.getInstance();
        final CharacterAuctionDataWrapperBean charactersThatAuctionEndDateLessThanConfigminutesLeft = mailBsnss.getCharactersThatAuctionEndDateLessThanConfigMinutesLeft(this.minutesLimitToSendMail, characterAuctionDataWrapperBean, this.minLevel, this.maxLevel, this.maxTibiaCoins);

        final boolean areCharactersToNotify = charactersThatAuctionEndDateLessThanConfigminutesLeft != null && !CollectionUtils.isEmpty(charactersThatAuctionEndDateLessThanConfigminutesLeft.getCharactersAuctionData());

        final boolean doSendMail = this.sendMailEnabled && areCharactersToNotify;

        if (doSendMail) {
            final CharacterAuctionDataWrapperDTO characterAuctionDataToNofity = this.characterAuctionMapper.toCharacterAuctionDataWrapperDTO(charactersThatAuctionEndDateLessThanConfigminutesLeft);
            this.createMailAndSend(characterAuctionDataToNofity);
        }
    }

    private void createMailAndSend(final CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO) {
        try {
            final List<CharacterAuctionDataDTO> listSorted = characterAuctionDataWrapperDTO.getCharactersAuctionData().stream().sorted(Comparator.comparing(characterAuctiondata -> characterAuctiondata.getAuctionData().getEndDate())).collect(Collectors.toList());
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@baeldung.com");
            message.setTo("luesve94@gmail.com");
            message.setSubject("test");
            message.setText(listSorted.toString());
            this.javaMailSender.send(message);
        } catch (final Exception e) {
            throw new InternalError("Error sending mail", e);
        }
    }
}
