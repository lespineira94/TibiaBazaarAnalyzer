package com.lespineira94.tibiabazaaranalyzer.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {
    private static final String MAIL_PROTOCOL_PROP = "mail.transport.protocol";

    private static final String MAIL_AUTH_PRO = "mail.smtp.auth";

    private static final String MAIL_STARTTLS_PROP = "mail.smtp.starttls.enable";

    private static final String MAIL_DEBUG_PROP = "mail.debug";

    private static final String SSL_TRUST = "mail.smtp.ssl.trust";

    @Value("${spring.mail.transport.protocol}")
    private String protocol;

    @Value("${spring.mail.smtp.auth}")
    private String auth;

    @Value("${spring.mail.smtp.starttls.enable}")
    private String starttls;

    @Value("${spring.mail.debug}")
    private String debug;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    @Value("${spring.mail.email}")
    private String email;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(Integer.parseInt(this.port));

        mailSender.setUsername(this.email);
        mailSender.setPassword(this.password);

        final Properties props = mailSender.getJavaMailProperties();
        props.put(MAIL_PROTOCOL_PROP, this.protocol);
        props.put(MAIL_AUTH_PRO, this.auth);
        props.put(MAIL_STARTTLS_PROP, this.starttls);
        props.put(MAIL_DEBUG_PROP, this.debug);
        props.put(SSL_TRUST, this.host);

        return mailSender;
    }
}
