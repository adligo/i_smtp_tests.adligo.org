package org.adligo.i.smtp.authenticators;

import org.adligo.tests.ATest;

public class SmtpUserPasswordTests extends ATest {

	public void testExc() {
		SmtpUserPassword up = new SmtpUserPassword();
		up.setPassword("password");
		up.setUser("user");
		assertEquals("user", up.getUser());
		assertEquals("password", up.getPassword());
	}
}
