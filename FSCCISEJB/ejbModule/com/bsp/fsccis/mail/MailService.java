package com.bsp.fsccis.mail;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bsp.fsccis.entity.RefUserAccounts;
import com.bsp.fsccis.entity.RefUserRole;
import com.bsp.fsccis.entity.tag.U_Enabled_Tag;

@Singleton
public class MailService {
	
	private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());

	@Resource(lookup = "mail/fsf-eis")
	private static Session mailSession;
	
	private static MailService instance;
	
	public static MailService getInstance() {
		
		if(instance == null)
			instance = new MailService();
		
		return instance;
	}
	
	private String fsf = "FSF-EIS";
	private String TelNo = "(632)87087556";		
	private String TelNo2 = "(632)53062516";
	private String agency;
	//private String group;
	
	private boolean isAdmin = false;
	
	public void sendEMail(RefUserAccounts user, String password) throws MessagingException {
		LOGGER.info("INIT sendEMail");
	/*	Properties props = new Properties();
		props.put("mail.smtp.socketFactory.port", 465);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", 465);
		props.put("mail.smtp.ssl.enable", "false"); 
		String host = "10.2.2.21";
		
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", 25);
		props.put("mail.smtp.ssl.enable", "false");
		props.put("mail.smtp.auth", "false"); */
		
		//Session session = Session.getInstance(props);
		//session.setDebug(true);
		//LOGGER.info("NEW PASSWORD: " + password);
		
		try {
			// Create a default MimeMessage object
			Message message = new MimeMessage(mailSession);
			
			// Set from: header field of the header
			message.setFrom(new InternetAddress(fsf + "<noreply_fsf-eis@bsp.gov.ph>"));
			
			// Set to: header field of the header
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
			
			// Set Subject: header field
			message.setSubject("Registered New User to the FSF-EIS portal");
			
			// Now set the actual message
			BodyPart messageBodyPart = new MimeBodyPart();
			
			//Fill the msg
			String msgContent = "Dear MR./MS. " + user.getFirstName() + " " + user.getLastName() + ", \n";
			
			agency = user.getAgencyId().getAgencyShortname();
			//group = user.getRefAgencyGroupId().getAgencyGroupShortname();
			
			StringBuilder sb = new StringBuilder();
			
			for(RefUserRole rur : user.getRefUserRoleList()){
				if(rur.getUEnabled() == U_Enabled_Tag.ENABLED){
					LOGGER.info("ROLE: " + rur.getSysRoles().getRoleName());
					sb.append(rur.getSysRoles().getRoleName()).append(", ");
				}
				
				if (rur.getSysRoles().getRoleName().equalsIgnoreCase("System Admin")) {
					System.out.println("ADMIN TRUE");
					isAdmin = true;
				
					break;
				}
			}
			
			if(!sb.toString().isEmpty()){
				user.setDisplayRoleList(sb.toString().substring(0,sb.toString().length() - 2));
			}
			
			msgContent += agency + "\n \n \n";
			msgContent += "Your account as a " + user.getDisplayRoleList() + " for the Financial Sector Forum - Electronic Information Sharing online facility (FSF-EIS) has been created as follows: \n \n";
			msgContent += "Username: " + user.getUserName() + "\n";
			msgContent += "Temporary Password: " + password + "\n \n \n";
			
			if (user.getDisplayRoleList().contains("System Admin")) {
				msgContent += "Please log-in to your account (https://fsfeisadmin.bsp.gov.ph) "
						+ "You will be prompted to change your password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";

			} else {
				msgContent += "Please log-in to your account (https://fsfeis.bsp.gov.ph) "
						+ "You will be prompted to change your password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";

			}
			
			msgContent += "Kindly acknowledge receipt of this email. \n \n";
			msgContent += "Thank you. \n \n";

			messageBodyPart.setText(msgContent);
			
			// Create multipart message for attachment
			Multipart multipart = new MimeMultipart();
			
			// Set text message part
			multipart.addBodyPart(messageBodyPart);
			
			// Second part attachment
			// Send complete message parts
			
			message.setContent(multipart);
			
			//send message
			Transport.send(message);
			LOGGER.info("Sent message successfully....");
			
		} catch (MessagingException | SecurityException e) {
			System.out.println("----ERROR MAIL!! ");
			LOGGER.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			
			throw e;
		}
		return;
		
	}
	
	public void sendEMailResetPassword(RefUserAccounts user, String password) throws MessagingException {
		LOGGER.info("INIT sendEMailResetPassword");
		try {
			// Create a default MimeMessage object
			Message message = new MimeMessage(mailSession);
			
			// Set from: header field of the header
			message.setFrom(new InternetAddress(fsf + "<noreply_fsf-eis@bsp.gov.ph>"));
			
			// Set to: header field of the header
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
			
			// Set Subject: header field
			message.setSubject("User Password Reset to the FSF-EIS portal");
			
			// Now set the actual message
			BodyPart messageBodyPart = new MimeBodyPart();
			
			//Fill the msg
			String msgContent = "Dear MR./MS. " + user.getFirstName() + " " + user.getLastName() + ", \n";
			
			agency = user.getAgencyId().getAgencyShortname();
			//group = user.getRefAgencyGroupId().getAgencyGroupShortname();
			
			StringBuilder sb = new StringBuilder();
			for(RefUserRole rur : user.getRefUserRoleList()){
				if(rur.getUEnabled() == U_Enabled_Tag.ENABLED){
					LOGGER.info("ROLE: " + rur.getSysRoles().getRoleName());
					sb.append(rur.getSysRoles().getRoleName()).append(", ");
				}
			}
			
			if(!sb.toString().isEmpty()){
				user.setDisplayRoleList(sb.toString().substring(0,sb.toString().length() - 2));
			}
			
			msgContent += agency + "\n \n \n";
			msgContent += "Your account as a " + user.getDisplayRoleList() + " for the Financial Sector Forum - Electronic Information Sharing online facility (FSF-EIS) has been reset: \n \n";
			msgContent += "Temporary Password: " + password + "\n \n \n";
			
			if (user.getDisplayRoleList().contains("System Admin")) {

				msgContent += "Please log-in to your account (https://fsfeisadmin.bsp.gov.ph) "
						+ "You will be prompted to change your password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";

			} else {
				msgContent += "Please log-in to your account (https://fsfeis.bsp.gov.ph) "
						+ "You will be prompted to change your password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";
			}
					 

			msgContent += "Kindly acknowledge receipt of this email. \n \n";
			msgContent += "Thank you. \n \n";

			messageBodyPart.setText(msgContent);
			
			// Create multipart message for attachment
			Multipart multipart = new MimeMultipart();
			
			// Set text message part
			multipart.addBodyPart(messageBodyPart);
			
			// Second part attachment
			// Send complete message parts
			
			message.setContent(multipart);
			
			//send message
			Transport.send(message);
			LOGGER.info("Sent message successfully....");
			
		} catch (MessagingException | SecurityException e) {
			System.out.println("----ERROR MAIL!! ");
			LOGGER.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			
			throw e;
		}
		return;
		
	}
	
	public void sendEmail2FAPassword(RefUserAccounts user) throws MessagingException {
		LOGGER.info("INIT sendEmail2FAPassword");
		try {
			Message message = new MimeMessage(mailSession);
			
			message.setFrom(new InternetAddress(fsf + "<noreply_fsf-eis@bsp.gov.ph>"));
			
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
			
			message.setSubject("User 2FA Password Reset to the FSF-EIS portal");
			
			BodyPart messageBodyPart = new MimeBodyPart();
			
			String msgContent = "Dear MR./MS. " + user.getFirstName() + " " + user.getLastName() + ", \n";
			
			agency = user.getAgencyId().getAgencyShortname();
			
			StringBuilder sb = new StringBuilder();
			
			for(RefUserRole rur : user.getRefUserRoleList()){
				if(rur.getUEnabled() == U_Enabled_Tag.ENABLED){
					LOGGER.info("ROLE: " + rur.getSysRoles().getRoleName());
					sb.append(rur.getSysRoles().getRoleName()).append(", ");
				}
				
				if (rur.getSysRoles().getRoleName().equalsIgnoreCase("System Admin")) {
					System.out.println("ADMIN TRUE");
					isAdmin = true;
				
					break;
				}
			}
			
			if(!sb.toString().isEmpty()){
				user.setDisplayRoleList(sb.toString().substring(0,sb.toString().length() - 2));
			}
			
			msgContent += agency + "\n \n \n";
			msgContent += "Your account as a " + user.getDisplayRoleList() + " for the Financial Sector Forum - Electronic Information Sharing online facility (FSF-EIS) has been reset: \n \n \n";
			//msgContent += "Temporary Password: " + password + "\n \n \n";
			
			if (user.getDisplayRoleList().contains("System Admin")) {

				msgContent += "Please log-in to your account (https://fsfeisadmin.bsp.gov.ph) "
						+ "You will be prompted to change your 2FA password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";

			} else {
				msgContent += "Please log-in to your account (https://fsfeis.bsp.gov.ph) "
						+ "You will be prompted to change your 2FA password at the initial log-in. "
						+ "For any other queries and concern please email fsf-eis@bsp.gov.ph "
						+ "or call BSP at Tel. No. " + TelNo + " | " + TelNo2 + " \n \n";
			}
			
			msgContent += "Kindly acknowledge receipt of this email. \n \n";
			msgContent += "Thank you. \n \n";
			
			messageBodyPart.setText(msgContent);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			
			Transport.send(message);
			LOGGER.info("Sent message successfully....");
			
		} catch (MessagingException | SecurityException e) {
			System.out.println("----ERROR MAIL!! ");
			LOGGER.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			
			throw e;
		}
		
		return;
	}
}
