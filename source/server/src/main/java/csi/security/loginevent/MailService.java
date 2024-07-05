package csi.security.loginevent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import csi.config.Configuration;
import csi.config.MailConfig;

public class MailService {
    private MailConfig mailConfig = Configuration.getInstance().getMailConfig();
    private boolean useMaxActive = mailConfig.getUseMaxActive();
    private boolean useMaxKnown = mailConfig.getUseMaxKnown();
    private boolean useTotalUnique = mailConfig.getUseTotalUnique();
    private String toAddress = mailConfig.getDefaultToEmailAddress();
    private String fromAddress = mailConfig.getDefaultFromEmailAddress();
    private LocalDate localDate = LocalDate.now();
    private String host = "localhost";
    private Properties properties = System.getProperties();

    public void send(PeriodLoginEvents periodLoginEvents) {
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.setSubject("Daily Concurrency Report for " + localDate);
            message.setText(buildText(periodLoginEvents));
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private String buildText(PeriodLoginEvents periodLoginEvents) {
        StringBuilder sb = new StringBuilder();
        List<String> userNames = new ArrayList<String>();
        int maxActiveUsers = 0;
        int maxKnownUsers = 0;
        int totalUniqueUsers = 0;
        for(LoginEvent event : periodLoginEvents.getPeriodEvents()) {
            if(useTotalUnique && !userNames.contains(event.getUserName())){
                userNames.add(event.getUserName());
                totalUniqueUsers = totalUniqueUsers + 1;
            }
            if(useMaxActive && (event.getActiveUsersAtEventTime() > maxActiveUsers)) {
                maxActiveUsers = event.getActiveUsersAtEventTime();
            }
            if(useMaxKnown && (event.getKnownUsersAtEventTime() > maxKnownUsers)) {
                maxKnownUsers = event.getKnownUsersAtEventTime();
            }
        }
        if(useTotalUnique) {
            sb.append("Total Unique Users: " + totalUniqueUsers);
            sb.append(System.lineSeparator());
        }
        if(useMaxActive) {
            sb.append("Max Active Users at one time: " + maxActiveUsers);
            sb.append(System.lineSeparator());
        }
        sb.append("Max Known Users at one time: " + maxKnownUsers);

        return sb.toString();
    }
}
