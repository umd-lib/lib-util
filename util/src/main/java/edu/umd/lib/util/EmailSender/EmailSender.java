package edu.umd.lib.util.EmailSender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * EmailSender object allows you to send email.
 * Before calling the sendEmail method
 * make sure you set all the necessary parameters
 * in the object.
 * (recipients, subjects, contents, server and port, etc.)
 * 
 * @author Allan Shaoxiong Jiang
 *
 */
public class EmailSender {
	private String subject;
	private String content;
	private String fileName;
	private String host;
	private int port;
	
	private File attachment = null;
	
	/**
	 * Constructor.
	 */
	public EmailSender(){
		/*ENPTY*/
	}
	
	public void setSubject(String subject) throws UnsupportedEncodingException{
		this.subject = this.toUTF_8(subject);
	}
	
	/**
	 * Set the content of the email.
	 * @param text
	 * @throws UnsupportedEncodingException
	 */
	public void setText(String [] text) throws UnsupportedEncodingException{
		StringBuffer result = new StringBuffer();
	    if (text.length > 0) {
	        result.append(text[0]);
	        for (int i=1; i<text.length; i++) {
	            result.append(" \n");
	            result.append(toUTF_8(text[i]));
	        }
	    }
	    this.content = result.toString();
	}
	
	/**
	 * Add an attachment to the Email.
	 * @param filePath
	 */
	public void setFile(String filePath){
		
		attachment = new File(filePath);
		fileName = attachment.getName();

	}
	
	
	/**
	 * return content of the email.
	 * @return
	 */
	public String getText(){
		return content;
	}
	
	
	/**
	 * return subject of the email.
	 * @return
	 */
	public String getSubject(){
		return subject;
	}

	
	/**
	 * return attachment of this email.
	 * if there is no attachment, return null.
	 * @return
	 */
	public File getAttachment(){
		return this.attachment;
	}

	
	/**
	 * return the name of the attachment.
	 * @return
	 */
	public String getAttachmentName(){
		return this.fileName;
	}

	/**
	 * Host server and port information must be set up
	 * using by calling this method.
	 * @param server
	 * @param port
	 */
	public void setHost(String server, int port){
		this.host = server;
		this.port = port;
	}
	/**
	 * Send email to a array of recipients.
	 * The server and port data of gmail is just for testing
	 * and will be modified later.
	 * @param recipients
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void sendEmail(String[] recipients) throws MessagingException, IOException{
		
		/*Set up a properties object contains connection information*/
		Properties props = new Properties();
		props.put("mail.smtp.host",  host);
		props.put("mail.smtp.port", port);
		
		Session session = Session.getDefaultInstance(props,null);
		
		/*Set new message (MIME)*/
		MimeMessage message = new MimeMessage(session);
		
		/*Set recipients address. (multiple recipients supports.)*/
		InternetAddress[] to = new InternetAddress[recipients.length];
		for(int index = 0; index < recipients.length; index ++){
			to[index] = new InternetAddress(recipients[index]);
		}
		message.setRecipients(Message.RecipientType.TO, to);
		
		/*Set subject.*/
		message.setSubject(subject);
	
		MimeBodyPart messageBodyPart = new MimeBodyPart();
	
		/*Set the content.*/
		messageBodyPart.setContent(content, "text/plain; charset=\"UTF-8\"");
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		
		/*Add attachment if an attachment is given.*/
		if(attachment != null){
		MimeBodyPart attachmentPart = new MimeBodyPart();
		
		DataSource dataSource = new ByteArrayDataSource(new FileInputStream(attachment),"application/octet-stream");
		attachmentPart.setDataHandler(new DataHandler(dataSource));
		
		attachmentPart.setFileName(fileName);

		multipart.addBodyPart(attachmentPart);
		}
		
		message.setContent(multipart);
		
		Transport.send(message);
		
	}
	
	
	/**
	 * Encoding the String to UTF-8.
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String toUTF_8(String str) throws UnsupportedEncodingException{
		String Encoding = "UTF-8";
		byte[] byteArr = str.getBytes(Encoding);
		String encoded = new String(byteArr);
		return encoded;
	 }
}
