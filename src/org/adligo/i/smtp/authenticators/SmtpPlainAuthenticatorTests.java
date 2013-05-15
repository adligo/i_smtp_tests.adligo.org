package org.adligo.i.smtp.authenticators;

import java.io.IOException;

import org.adligo.i.adi.client.InvocationException;
import org.adligo.i.smtp.mocks.MockCommandCallback;
import org.adligo.i.smtp.mocks.MockSmtpConnection;
import org.adligo.tests.ATest;

public class SmtpPlainAuthenticatorTests extends ATest {

	SmtpPlainAuthenticator auth = new SmtpPlainAuthenticator();
	boolean authLogin = false;
	boolean userPasswordMatch = false;
	
	public void setUp() {
		authLogin = false;
		userPasswordMatch = false;
	}
	
	public void testAuthenticationSuccess() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH PLAIN":
							authLogin = true;
							return new String[] {"334 VXNlcm5hbWU6"};
					case "AHNvbWVVc2VyAHNvbWVQYXNz":
						userPasswordMatch = true;
						return new String[] {"235 2.0.0 OK Authenticated"};
				}
				return new String[] {};
			}
		});
		SmtpUserPassword userPass = new SmtpUserPassword("someUser", "somePass");
		
		auth.authenticate(mockCon, userPass);
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
		assertTrue("The base 64 username/password should have been sent.", userPasswordMatch);
	}

	public void testAuthenticationFailureServerDoesNOTKnowLogin() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		mockCon.setEhlo(new String[] {"250-ENHANCEDSTATUSCODES","250-PIPELINING"});
		
		SmtpUserPassword userPass = new SmtpUserPassword("someUser", "somePass");
		
		InvocationException caught = null;
		try {
			auth.authenticate(mockCon, userPass);
		} catch (InvocationException x) {
			caught = x;
		}
		assertNotNull(caught);
		assertEquals(SmtpPlainAuthenticator.THE_SERVER_DOES_NOT_UNDERSTAND_THE_AUTH_PLAIN_COMMAND, caught.getMessage());
	}
	
	public void testAuthenticationFailureAtAuthPlain() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH PLAIN":
							authLogin = true;
							return new String[] {"VXNlcm5hbWU6"};
				}
				return new String[] {};
			}
		});
		SmtpUserPassword userPass = new SmtpUserPassword("someUser", "somePass");
		
		InvocationException caught = null;
		try {
			auth.authenticate(mockCon, userPass);
		} catch (InvocationException x) {
			caught = x;
		}
		assertNotNull(caught);
		assertEquals(SmtpPlainAuthenticator.ERROR_WITH_AUTH_PLAIN + 
				"VXNlcm5hbWU6\n" , caught.getMessage());
		assertTrue("The AUTH PLAIN command should have been sent.", authLogin);
	}
	
	public void testAuthenticationFailureAtUserPassword() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH PLAIN":
							authLogin = true;
							return new String[] {"334 VXNlcm5hbWU6"};
					case "AHNvbWVVc2VyAHNvbWVQYXNz":
						userPasswordMatch = true;
						return new String[] {"250 2.0.0 Authenticate Failed!"};
				}
				return new String[] {};
			}
		});
		SmtpUserPassword userPass = new SmtpUserPassword("someUser", "somePass");
		
		InvocationException caught = null;
		try {
			auth.authenticate(mockCon, userPass);
		} catch (InvocationException x) {
			caught = x;
		}

		assertNotNull(caught);
		assertEquals(SmtpPlainAuthenticator.THE_USER_DID_NOT_AUTHENTICATE + 
				"250 2.0.0 Authenticate Failed!\n" , caught.getMessage());
		
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
		assertTrue("The base 64 username/password should have been sent.", userPasswordMatch);
	}
	

}
