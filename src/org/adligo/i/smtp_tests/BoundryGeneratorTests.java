package org.adligo.i.smtp_tests;

import org.adligo.i.smtp.BoundryGenerator;
import org.adligo.tests.ATest;

public class BoundryGeneratorTests extends ATest {

	public void testGen() {
		String result = BoundryGenerator.gen(4);
		assertEquals(4, result.length());
		
		result = BoundryGenerator.gen(5);
		assertEquals(5, result.length());
		
		result = BoundryGenerator.gen(40);
		assertEquals(40, result.length());
		
		result = BoundryGenerator.gen(40);
		assertEquals(40, result.length());
	}
}
