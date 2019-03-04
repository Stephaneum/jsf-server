package tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mysql.Datenbank;

public class EmailManager {
	
	//Senden der E-Mails über SMTP-Protokoll
	//GMail
	
	final static private String HOST_EMAIL = "tien.donam123@gmail.com", HOST_PW = "1A2B3C4d";
	final static public int STATUS_OK = 0, STATUS_EMAIL_NOT_EXIST = 1, STATUS_ADDRESS_ERR = 2, STATUS_MESSAGING_ERR = 3;
	
	//false = E-Mail existiert nicht
	//true = erfolgreich
	//0... OK
	//1... E-Mail existiert nicht
	//2... AddressException
	//3... MessagingException
	public static int sendCodePasswortVergessen(String email) {
		
		String code = Datenbank.addPasswortVergessen(email);
		if(code == null) {
			Konsole.antwort("E-Mail existiert nicht in der Datenbank");
			return STATUS_EMAIL_NOT_EXIST;
		}
		
		DateFormat df = new SimpleDateFormat("dd.MMMM.YYYY HH:mm (zzzz) ");
		Date today = Calendar.getInstance().getTime();
		String content = "Du hattest am "+df.format(today)+" nach einem neuen Passwort beantragt. (Stephaneum700.de)<br/>Dieser Code ist für zwei Stunden gültig.<br/><br/>Code: <b>"+code+"</b>";
		
		return send(email, "Stephaneum: Neues Passwort beantragen", content);
	}
	
	//0... OK
	//2... AddressException
	//3... MessagingException
	static int send(String email_adresse, String titel, String content) {
		Konsole.status(1,6,"Senden der E-Mail beginnt...");
		
		Properties props = System.getProperties();
	    props.put("mail.smtp.starttls.enable", true); // added this line
	    props.put("mail.smtp.host", "smtp.gmail.com");
	    props.put("mail.smtp.user", HOST_EMAIL);
	    props.put("mail.smtp.password", HOST_PW);
	    props.put("mail.smtp.port", "587");
	    props.put("mail.smtp.auth", true);
	    props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

	    Session session = Session.getInstance(props,null);
	    MimeMessage message = new MimeMessage(session);

	    Konsole.status(2,6,"Nutze Port '"+session.getProperty("mail.smtp.port")+"'");

	    try {
	        InternetAddress from = new InternetAddress(HOST_EMAIL);
	        message.setSubject(titel);
	        message.setFrom(from);
	        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email_adresse));
	        
	        Konsole.status(3, 6, "Empfänger = "+email_adresse);

	        Multipart multipart = new MimeMultipart("alternative");

	        BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setText("");

	        multipart.addBodyPart(messageBodyPart);

	        messageBodyPart = new MimeBodyPart();
	        String htmlMessage = content;
	        messageBodyPart.setContent(htmlMessage, "text/html");


	        multipart.addBodyPart(messageBodyPart);

	        message.setContent(multipart);

	        Transport transport = session.getTransport("smtp");
	        Konsole.status(4, 6, "Verbinde mit der Host-Email: '"+HOST_EMAIL+"' ...");
	        transport.connect("smtp.gmail.com", HOST_EMAIL, HOST_PW);
	        Konsole.status(5, 6, "Verbunden! E-Mail wird von dort aus nun gesendet ...");
	        transport.sendMessage(message, message.getAllRecipients());
	        Konsole.status(6, 6, "E-Mail gesendet!");
	        
	        return STATUS_OK;
	    } catch (AddressException e) {
	        e.printStackTrace();
	        return STATUS_ADDRESS_ERR;
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        return STATUS_MESSAGING_ERR;
	    }
	}
}
