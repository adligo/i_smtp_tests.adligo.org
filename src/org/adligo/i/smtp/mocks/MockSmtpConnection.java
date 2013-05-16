package org.adligo.i.smtp.mocks;

import java.io.IOException;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.smtp.I_SmtpConnection;
import org.adligo.i.smtp.models.I_EMailMessage;

public class MockSmtpConnection implements I_SmtpConnection {
	private static final Log log = LogFactory.getLog(MockSmtpConnection.class);
	public static final String [] AUTHSMTP_EHLO = new String[] {
		"250-ENHANCEDSTATUSCODES", "250-PIPELINING",
		"250-8BITMIME", "250-SIZE 52428800",
		"250-AUTH CRAM-MD5 DIGEST-MD5 LOGIN PLAIN",
		"250-STARTTLS", "250 HELP"
	};
	private MockCommandCallback commandCallback;
	private String boundry;
	private String []  ehlo = AUTHSMTP_EHLO;
	
	@Override
	public void returnToPool() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isReadWrite() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isOK() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String[] sendCommand(String command) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug(command);
		}
		return commandCallback.onCommand(command);
	}
	@Override
	public String[] getEhloResp() {
		return ehlo;
	}
	@Override
	public void send(I_EMailMessage message) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String generateBoundry(int lenght) {
		return boundry;
	}
	
	public String getBoundry() {
		return boundry;
	}
	public void setBoundry(String boundry) {
		this.boundry = boundry;
	}
	public String[] getEhlo() {
		return ehlo;
	}
	public void setEhlo(String[] ehlo) {
		this.ehlo = ehlo;
	}
	public MockCommandCallback getCommandCallback() {
		return commandCallback;
	}
	public void setCommandCallback(MockCommandCallback commandCallback) {
		this.commandCallback = commandCallback;
	}
	@Override
	public void sendCommandPart(String command) throws IOException {
		commandCallback.onCommandPart(command);
	}
	
}
