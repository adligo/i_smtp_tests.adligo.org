package org.adligo.i.smtp_tests.mocks;

public interface MockCommandCallback {
	public void onCommandPart(String p);
	public String [] onCommand(String p);
}
