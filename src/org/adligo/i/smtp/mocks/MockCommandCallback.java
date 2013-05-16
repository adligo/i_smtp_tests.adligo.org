package org.adligo.i.smtp.mocks;

public interface MockCommandCallback {
	public void onCommandPart(String p);
	public String [] onCommand(String p);
}
