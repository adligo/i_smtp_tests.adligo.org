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
			message.setSubject("Hi from Eclipse");
			message.setBody("This is a really\n" +
					"nice email api. The file was " + fileName + "\n" +
					"\n" +
					"Cheers,\n" +
					"Scott");
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
