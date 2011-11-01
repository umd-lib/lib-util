package edu.umd.lib.util.EmailSender;

import java.io.IOException;
import javax.mail.MessagingException;

import org.junit.Test;

public class EmailSenderTest {

	@Test
	public void testPdfFileAttachment() throws MessagingException, IOException {
		String[] content = {"This is a Test Email","Test encoding"," テイスド."};
		/*this email address is private. should be replaced by tester's email address
		*in order to receive test email.
		**/
		String[] recipients = {"jiangshaoxiong18@gmail.com"};
		EmailSender sender = new EmailSender();
		sender.setText(content);
		sender.setSubject("This is a テイスド");
		sender.setFile("src/test/resources/edu/umd/lib/util/testfile.txt");
		sender.setHost("129.2.17.241", 87);
		sender.sendEmail(recipients);
		System.out.print("this");
	}
	
	@Test
	public void testTextFileAttachment() throws MessagingException, IOException {
		String[] content = {"This is a Test Email","Test encoding"," テイスド."};
		/*this email address is private. should be replaced by tester's email address
		*in order to receive test email.
		**/
		String[] recipients = {"jiangshaoxiong18@gmail.com"};
		EmailSender sender = new EmailSender();
		sender.setText(content);
		sender.setSubject("This is a テイスド");
		sender.setFile("src/test/resources/edu/umd/lib/util/TestPDF.pdf");
		sender.setHost("129.2.17.241", 87);
		sender.sendEmail(recipients);
	}

}
