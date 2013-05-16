package org.adligo.i.smtp.authenticators;

import java.io.IOException;

import org.adligo.i.adi.client.InvocationException;
import org.adligo.i.smtp.mocks.MockCommandCallback;
import org.adligo.i.smtp.mocks.MockSmtpConnection;
import org.adligo.tests.ATest;

public class SmtpLoginAuthenticatorTests extends ATest {

	SmtpLoginAuthenticator auth = new SmtpLoginAuthenticator();
	boolean authLogin = false;
	boolean userMatch = false;
	boolean passwordMatch = false;
	
	public void setUp() {
		authLogin = false;
		userMatch = false;
		passwordMatch = false;
	}
	
	public void testAuthenticationSuccess() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH LOGIN":
							authLogin = true;
							return new String[] {"334 VXNlcm5hbWU6"};
					case "c29tZVVzZXI=":
						userMatch = true;
						return new String[] {"334 VXNlcm5hbWU6"};
					case "c29tZVBhc3M=":
						passwordMatch = true;
						return new String[] {"235 2.0.0 OK Authenticated"};
				}
				return new String[] {};
			}

			@Override
			public void onCommandPart(String p) {
				
			}
		});
		SmtpUserPassword userPass = new SmtpUserPassword("someUser", "somePass");
		
		auth.authenticate(mockCon, userPass);
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
		assertTrue("The base 64 username should have been sent.", userMatch);
		assertTrue("The base 64 password should have been sent.", passwordMatch);
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
		assertEquals(SmtpLoginAuthenticator.THE_SERVER_DOES_NOT_UNDERSTAND_THE_AUTH_LOGIN_COMMAND, caught.getMessage());
	}
	
	public void testAuthenticationFailureAtAuthLogin() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH LOGIN":
							authLogin = true;
							return new String[] {"VXNlcm5hbWU6"};
				}
				return new String[] {};
			}
			@Override
			public void onCommandPart(String p) {
				
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
		assertEquals(SmtpLoginAuthenticator.AUTH_PLAIN_DID_NOT_RETURN_334 + 
				"VXNlcm5hbWU6\n" , caught.getMessage());
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
	}
	
	public void testAuthenticationFailureAtUser() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH LOGIN":
							authLogin = true;
							return new String[] {"334 VXNlcm5hbWU6"};
					case "c29tZVVzZXI=":
						userMatch = true;
						return new String[] {"VXNlcm5hbWU6"};
				}
				return new String[] {};
			}
			@Override
			public void onCommandPart(String p) {
				
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
		assertEquals(SmtpLoginAuthenticator.USER_BASE64_DID_NOT_RETURN_334 + 
				"VXNlcm5hbWU6\n" , caught.getMessage());
		
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
		assertTrue("The base 64 username should have been sent.", userMatch);
	}
	
	public void testAuthenticationFailureAtPassword() throws Exception {
		MockSmtpConnection mockCon = new MockSmtpConnection();
		
		
		mockCon.setCommandCallback(new MockCommandCallback() {
			@Override
			public String[] onCommand(String p) {
				switch (p) {
					case "AUTH LOGIN":
							authLogin = true;
							return new String[] {"334 VXNlcm5hbWU6"};
					case "c29tZVVzZXI=":
						userMatch = true;
						return new String[] {"334 VXNlcm5hbWU6"};
					case "c29tZVBhc3M=":
						passwordMatch = true;
						return new String[] {"250 2.0.0 Authenticate Failed!"};
				}
				return new String[] {};
			}
			@Override
			public void onCommandPart(String p) {
				
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
		assertEquals(SmtpLoginAuthenticator.PASSWORD_BASE64_DID_NOT_RETURN_235 + 
				"250 2.0.0 Authenticate Failed!\n" , caught.getMessage());
		
		assertTrue("The AUTH LOGIN command should have been sent.", authLogin);
		assertTrue("The base 64 username should have been sent.", userMatch);
		assertTrue("The base 64 password should have been sent.", passwordMatch);
	}
}
