package org.adligo.i.smtp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.Pool;
import org.adligo.i.smtp.authenticators.SmtpLoginAuthenticator;
import org.adligo.i.smtp.authenticators.SmtpUserPassword;
import org.adligo.i.smtp.models.EMailMessageMutant;
import org.adligo.i.smtp.models.LocalFileEmailAttachment;
import org.adligo.i.util.client.StringUtils;
import org.adligo.jse.util.JSECommonInit;
import org.adligo.models.core.client.EMailAddress;
import org.adligo.models.core.client.InvalidParameterException;

/**
 * example api usage
 * 
 * @author scott
 *
 */
public class MainMailer {
	private static final Log log = LogFactory.getLog(MainMailer.class);
	
	public static void main(String [] args) {
		JSECommonInit.callLogDebug("MainMailer init");
		
		SmtpConnectionFactoryConfigMutant config = new SmtpConnectionFactoryConfigMutant();
		config.setHost("localhost");
		config.setPort(25);
		config.setAuthenticator(new SmtpLoginAuthenticator());
		config.setCredentials(new SmtpUserPassword("user", "password"));

		String from = "from_user@example.com";
		String to = "user@example.com";
		String cc = "";
		String bcc = "";
		String fileName = "";
		if (args.length == 1) {
			try {
				Properties props = new Properties();
				File file = new File(args[0]);
				fileName = file.getName();
				FileInputStream fis = new FileInputStream(file);
				props.load(fis);
				String host = props.getProperty("host");
				config.setHost(host);
				String port = props.getProperty("port");
				config.setPort(Integer.parseInt(port));
				String user = props.getProperty("user");
				String pass = props.getProperty("password");
				config.setCredentials(new SmtpUserPassword(user, pass));
				to = props.getProperty("to");
				from = props.getProperty("from");
				cc = props.getProperty("cc");
				bcc = props.getProperty("bcc");
			} catch (FileNotFoundException fif) {
				log.error(fif.getMessage(), fif);
				return;
			} catch (IOException fif) {
				log.error(fif.getMessage(), fif);
				return;
			}
		}
		
		EMailMessageMutant message = new EMailMessageMutant();
		try {
			message.setFrom(new EMailAddress(from));
			message.addTo(new EMailAddress(to));
			if (!StringUtils.isEmpty(cc)) {
				message.addCc(new EMailAddress(cc));
			}
			if (!StringUtils.isEmpty(bcc)) {
				message.addBcc(new EMailAddress(bcc));
			}
			message.setSubject("Hi from Eclipse 13");
			message.setHtmlBody(true);
			message.setBody("<HTML><BODY>Hey this is a html email :)<br>" +
					"<img src=\"http://www.argon-evolution.com/ScottMorgan/scott2.gif\"> " +
					"<br><br>" +
					"Cheers<br>Scott<br>");
			//message.addAttachment(new LocalFileEmailAttachment("TestCoverage.txt","text/plain"));
			//message.addAttachment(new LocalFileEmailAttachment("scott2.gif","image/gif"));
		} catch (InvalidParameterException ipe) {
			log.error(ipe.getMessage(), ipe);
			return;
		}
		Pool<SmtpConnection> smtpConnectionPool = new Pool<SmtpConnection>(
				new SmtpConnectionFactoryConfig(config));
		SmtpConnection connection = smtpConnectionPool.getConnection();
		try {
			connection.send(message);
			smtpConnectionPool.shutdown();
			connection.returnToPool();
		} catch (IOException x) {
			log.error(x.getMessage(), x);
		}
		log.error("done");
	}
}
