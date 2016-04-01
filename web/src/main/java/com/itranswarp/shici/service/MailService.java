package com.itranswarp.shici.service;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

public class MailService {

	@Value("${mail.single.sender.host}")
	String singleSenderHost;

	@Value("${mail.single.sender.port}")
	int singleSenderPort;

	@Value("${mail.single.sender.from}")
	String singleSenderFrom;

	@Value("${mail.single.sender.username}")
	String singleSenderUsername;

	@Value("${mail.single.sender.password}")
	String singleSenderPassword;

	@Value("${mail.single.sender.tls}")
	boolean singleSenderTls;

	@Value("${mail.batch.sender.host}")
	String batchSenderHost;

	@Value("${mail.batch.sender.port}")
	int batchSenderPort;

	@Value("${mail.batch.sender.from}")
	String batchSenderFrom;

	@Value("${mail.batch.sender.username}")
	String batchSenderUsername;

	@Value("${mail.batch.sender.password}")
	String batchSenderPassword;

	@Value("${mail.batch.sender.tls}")
	boolean batchSenderTls;

	JavaMailSender singleSender;

	JavaMailSender batchSender;

	@PostConstruct
	public void init() {
		singleSender = createJavaMailSender(singleSenderHost, singleSenderPort, singleSenderUsername,
				singleSenderPassword, singleSenderTls);
		batchSender = createJavaMailSender(batchSenderHost, batchSenderPort, batchSenderUsername, batchSenderPassword,
				batchSenderTls);
	}

	JavaMailSender createJavaMailSender(String host, int port, String username, String password, boolean tls) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setPassword(password);
		sender.setPort(port);
		sender.setUsername(username);
		sender.setDefaultEncoding("UTF-8");
		Properties props = new Properties();
		props.setProperty("mail.smtp.auth", "true");
		if (tls) {
			props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
		}
		sender.setJavaMailProperties(props);
		return sender;
	}

	public void sendMail(String to, String subject, String body) throws MessagingException, IOException {
		MimeMessage msg = singleSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, false);
		helper.setFrom(this.singleSenderUsername, this.singleSenderFrom);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, body.startsWith("<html>"));
		singleSender.send(msg);
	}

	public void sendBatchMails(String to, String subject, String body) {

	}
}
