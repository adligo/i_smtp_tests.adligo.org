package org.adligo.i.smtp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.adligo.i.adi.client.InvocationException;
import org.adligo.i.disk.I_InputProcessor;
import org.adligo.i.disk.ReadOnlyDiskConnection;
import org.adligo.i.disk.ReadOnlyDiskConnectionFactory;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.I_Pool;
import org.adligo.i.pool.Pool;
import org.adligo.i.pool.PoolConfigurationMutant;
import org.adligo.i.smtp.authenticators.SmtpLoginAuthenticator;
import org.adligo.i.smtp.authenticators.SmtpUserPassword;
import org.adligo.i.smtp.models.EMailAttachmentStream;
import org.adligo.i.smtp.models.EMailMessageMutant;
import org.adligo.i.smtp.models.I_EMailAttachmentStream;
import org.adligo.i.smtp.models.EMailAttachmentInRam;
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
	private static String from = "from_user@example.com";
	private static String to = "user@example.com";
	private static String cc = "";
	private static String bcc = "";
	private static String fileName = "";
	private static Pool<ReadOnlyDiskConnection> diskPool = getDiskPool();
	
	public static Pool<ReadOnlyDiskConnection> getDiskPool() {
		try {
			return new Pool<ReadOnlyDiskConnection>(
					new PoolConfigurationMutant<ReadOnlyDiskConnection>(
							"diskPool", new ReadOnlyDiskConnectionFactory(), 1));
		} catch (InvalidParameterException x) {
			log.error(x.getMessage(), x);
		}
		return null;
	}
	
	public static void main(String [] args) {
		JSECommonInit.callLogDebug("MainMailer init");
		
		SmtpConnectionFactoryConfigMutant config = new SmtpConnectionFactoryConfigMutant();
		config.setHost("localhost");
		config.setPort(25);
		config.setAuthenticator(new SmtpLoginAuthenticator());
		config.setCredentials(new SmtpUserPassword("user", "password"));

		
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
		//sendEmailWithRamAttachment(config);
		sendEmailWithStreamAttachment(config);
		log.error("done");
	}

	public static void sendEmailWithStreamAttachment(
			SmtpConnectionFactoryConfigMutant config) {
		
		final ReadOnlyDiskConnection diskCon = diskPool.getConnection();
		
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
			message.setSubject("Hi from Eclipse Streamed 14");
			message.setHtmlBody(true);
			message.setBody("<HTML><BODY>Hey this is a html email :)<br>" +
					"<img src=\"http://www.argon-evolution.com/ScottMorgan/scott2.gif\"> " +
					"<br><br>" +
					"Cheers<br>Scott<br>");
			message.addAttachment(new EMailAttachmentStream("TestCoverage.txt","text/plain",new I_EMailAttachmentStream() {
				
				@Override
				public void gettingData() throws IOException {
					diskCon.startStreamRead("TestCoverage.txt");
				}

				@Override
				public boolean hasMoreData() throws IOException {
					return diskCon.hasMoreBytes();
				}

				@Override
				public byte nextByte() throws IOException  {
					return diskCon.nextByte();
				}

				@Override
				public void finishedGettingData() throws IOException {
					diskCon.endStreamRead();
				}
			} ));
			message.addAttachment(new EMailAttachmentStream("scott2.gif","image/gif",new I_EMailAttachmentStream() {
				@Override
				public void gettingData() throws IOException {
					diskCon.startStreamRead("scott2.gif");
				}

				@Override
				public boolean hasMoreData() throws IOException {
					return diskCon.hasMoreBytes();
				}

				@Override
				public byte nextByte() throws IOException  {
					return diskCon.nextByte();
				}
				
				@Override
				public void finishedGettingData() throws IOException {
					diskCon.endStreamRead();
				}
			}));
		} catch (InvalidParameterException ipe) {
			log.error(ipe.getMessage(), ipe);
			return;
		}
		I_Pool<SmtpConnection> smtpConnectionPool = new Pool<SmtpConnection>(
				new SmtpConnectionFactoryConfig(config));
		SmtpConnection connection = smtpConnectionPool.getConnection();
		try {
			connection.send(message);
			
			
		} catch (IOException x) {
			log.error(x.getMessage(), x);
		} catch (InvocationException x) {
			log.error(x.getMessage(), x);
		} finally {
			smtpConnectionPool.shutdown();
			connection.returnToPool();
			diskCon.returnToPool();
		}
	}
	
	public static void sendEmailWithRamAttachment(
			SmtpConnectionFactoryConfigMutant config) {
		
		
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
			message.setSubject("Hi from Eclipse 14");
			message.setHtmlBody(true);
			message.setBody("<HTML><BODY>Hey this is a html email :)<br>" +
					"<img src=\"http://www.argon-evolution.com/ScottMorgan/scott2.gif\"> " +
					"<br><br>" +
					"Cheers<br>Scott<br>");
			message.addAttachment(new EMailAttachmentInRam("TestCoverage.txt","text/plain", getBytes("TestCoverage.txt")));
			message.addAttachment(new EMailAttachmentInRam("scott2.gif","image/gif", getBytes("scott2.gif")));
		} catch (InvalidParameterException ipe) {
			log.error(ipe.getMessage(), ipe);
			return;
		}
		I_Pool<SmtpConnection> smtpConnectionPool = new Pool<SmtpConnection>(
				new SmtpConnectionFactoryConfig(config));
		SmtpConnection connection = smtpConnectionPool.getConnection();
		try {
			connection.send(message);
			
			
		} catch (IOException x) {
			log.error(x.getMessage(), x);
		} catch (InvocationException x) {
			log.error(x.getMessage(), x);
		} finally {
			smtpConnectionPool.shutdown();
			connection.returnToPool();
		}
	}
	
	public static byte[] getBytes(String fileName) {
		ReadOnlyDiskConnection diskCon = diskPool.getConnection();
		 
		final List<Byte> bytes = new ArrayList<Byte>();
		
		try {
			diskCon.readFile(fileName, new I_InputProcessor() {
				@Override
				public void process(InputStream in, long byteLength) throws IOException {
					int next = in.read();
					while (next != -1) {
						bytes.add(new Byte((byte) next));
						next = in.read();
					}
				}
			});
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			diskCon.returnToPool();
		}
		byte [] bs = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			bs[i] = bytes.get(i);
		}
		return bs;
	}
	
}
