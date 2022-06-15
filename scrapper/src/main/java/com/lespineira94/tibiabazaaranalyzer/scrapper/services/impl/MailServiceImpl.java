package com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl;

import com.lespineira94.tibiabazaaranalyzer.api.dto.AuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterAuctionDataWrapperDTO;
import com.lespineira94.tibiabazaaranalyzer.api.dto.CharacterDTO;
import com.lespineira94.tibiabazaaranalyzer.api.services.MailService;
import com.lespineira94.tibiabazaaranalyzer.scrapper.beans.auctions.CharacterAuctionDataWrapperBean;
import com.lespineira94.tibiabazaaranalyzer.scrapper.mappers.CharacterAuctionMapper;
import com.lespineira94.tibiabazaaranalyzer.scrapper.services.impl.bsnss.MailBsnss;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value("${spring.mail.email}")
    private String emailFrom;

    @Value("${app.mail.mailTo}")
    private String mailTo;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Async
    public void sendMail(final CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO) {
        final CharacterAuctionDataWrapperBean characterAuctionDataWrapperBean = this.characterAuctionMapper.toCharacterAuctionDataWrapperBean(characterAuctionDataWrapperDTO);
        final MailBsnss mailBsnss = MailBsnss.getInstance();
        final CharacterAuctionDataWrapperBean charactersThatAuctionEndDateLessThanConfigminutesLeft = mailBsnss.getCharactersThatAuctionEndDateLessThanConfigMinutesLeft(this.minutesLimitToSendMail, characterAuctionDataWrapperBean, this.minLevel, this.maxLevel, this.maxTibiaCoins);

        final boolean areCharactersToNotify = !CollectionUtils.isEmpty(charactersThatAuctionEndDateLessThanConfigminutesLeft.getCharactersAuctionData());

        final boolean doSendMail = this.sendMailEnabled && areCharactersToNotify;

        if (doSendMail) {
            final CharacterAuctionDataWrapperDTO characterAuctionDataToNofity = this.characterAuctionMapper.toCharacterAuctionDataWrapperDTO(charactersThatAuctionEndDateLessThanConfigminutesLeft);
            this.createMailAndSend(characterAuctionDataToNofity);
        }
    }

    private void createMailAndSend(final CharacterAuctionDataWrapperDTO characterAuctionDataWrapperDTO) {
        try {
            final List<CharacterAuctionDataDTO> listSorted = characterAuctionDataWrapperDTO.getCharactersAuctionData().stream().sorted(Comparator.comparing(characterAuctiondata -> characterAuctiondata.getAuctionData().getEndDate())).collect(Collectors.toList());
            final String subjectDate = LocalDateTime.now().format(this.formatter);
            final MimeMessage message = this.javaMailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true);
            message.setFrom(this.emailFrom);
            helper.setTo(this.mailTo);
            message.setSubject("Tibia Bazaar info at: " + subjectDate);

            final StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<body>");

            listSorted.forEach(characterAuctionDataDTO -> {
                final CharacterDTO character = characterAuctionDataDTO.getCharacter();
                final AuctionDataDTO auctionData = characterAuctionDataDTO.getAuctionData();

                sb.append("<a href='" + character.getCharacterInfoUrl() + "'> <h3 style ='margin: 0px; color:blue'>" + character.getName() + "</h3></a>");
                sb.append("<p style ='margin: 2px; margin-top:0px'><img src='" + character.getImgUrl() + "' </p>");
                sb.append("<p style ='margin: 2px; color:black; font-weight: bold'>" + character.getLevel() + " " + character.getVocation() + "</p>");
                sb.append("<p style ='margin: 2px; color:black'>" + auctionData.getMinimumBid() + "<img src='https://static.tibia.com/images//account/icon-tibiacointrusted.png' /> </p>");
                sb.append("<p style ='margin: 2px; color:black'> Auction end: " + auctionData.getEndDate().format(this.formatter) + "</p>");
                sb.append("<br>");

            });
            sb.append("</body></html>");

            helper.setText(sb.toString(), true);
            this.javaMailSender.send(message);
        } catch (final Exception e) {
            throw new InternalError("Error sending mail", e);
        }
    }
}
