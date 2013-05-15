package org.adligo.i.smtp;

import org.adligo.tests.ATest;

public class SmtpHelperTests extends ATest {
	
	public void testContains() {
		String [] ehloExample = new String[] {
				"250-Hello TEST","250-8BITMIME","250-AUTH PLAIN LOGIN", "250 SIZE 5242880"
		};
		assertTrue(SmtpHelper.contains(new String[] {"AUTH","PLAIN"}, ehloExample));
		assertTrue(SmtpHelper.contains(new String[] {"5242880"}, ehloExample));
	}
	
	public void testToExceptionMessage() {
		String [] ehloExample = new String[] {
				"250-Hello TEST","250-8BITMIME","250-AUTH PLAIN LOGIN", "250 SIZE 5242880"
		};
		String p = SmtpHelper.toExceptionMessage(ehloExample);
		
		assertEquals("250-Hello TEST\n" +
				"250-8BITMIME\n" +
				"250-AUTH PLAIN LOGIN\n" +
				"250 SIZE 5242880\n", p);
	}
}
