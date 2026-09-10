package com.example.epopeyaap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * MailConfig
 * ----------
 * ANTES: clase vacía. Como "spring-boot-starter-mail" está en el pom.xml pero
 * sin ninguna propiedad "spring.mail.*" en application.properties, Spring
 * Boot ni siquiera configuraba un JavaMailSender por defecto.
 *
 * Aquí se construye explícitamente a partir de propiedades (para que si
 * faltan, el arranque de la app no falle: MailService ya controla el error
 * y no rompe el flujo de "solicitar-reset" aunque el email no llegue a
 * enviarse, por ejemplo en un entorno de pruebas sin credenciales SMTP reales).
 *
 * Variables a añadir en application.properties (o como variables de entorno):
 *   spring.mail.host=smtp.gmail.com
 *   spring.mail.port=587
 *   spring.mail.username=${MAIL_USERNAME}
 *   spring.mail.password=${MAIL_PASSWORD}
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
