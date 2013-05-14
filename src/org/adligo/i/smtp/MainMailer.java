package org.adligo.i.smtp;

import java.io.IOException;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.Pool;
import org.adligo.i.smtp.authenticators.SmtpPlainAuthenticator;
import org.adligo.i.smtp.authenticators.SmtpUserPassword;
import org.adligo.jse.util.JSECommonInit;

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
		config.setAuthenticator(new SmtpPlainAuthenticator());
		config.setCredentials(new SmtpUserPassword("user", "password"));

		
		Pool<SmtpConnection> smtpConnectionPool = new Pool<SmtpConnection>(
				new SmtpConnectionFactoryConfig(config));
		SmtpConnection connection = smtpConnectionPool.getConnection();
		try {
			connection.reconnect();
			smtpConnectionPool.shutdown();
			connection.returnToPool();
		} catch (IOException x) {
			log.error(x.getMessage(), x);
		}
		log.error("done");
	}
}
