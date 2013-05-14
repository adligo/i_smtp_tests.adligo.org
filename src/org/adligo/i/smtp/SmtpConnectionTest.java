package org.adligo.i.smtp;

import org.adligo.tests.ATest;

public class SmtpConnectionTest extends ATest {

	public void testContains() {
		String [] ehloExample = new String[] {
				"250-Hello TEST","250-8BITMIME","250-AUTH PLAIN LOGIN", "250 SIZE 5242880"
		};
		SmtpConnectionFactoryConfigMutant config = new SmtpConnectionFactoryConfigMutant();
		config.setHost("localhost");
		config.setPort(2525);
		SmtpConnection connection = new SmtpConnection(config);
		assertTrue(connection.contains(new String[] {"AUTH","PLAIN"}, ehloExample));
	}
}
