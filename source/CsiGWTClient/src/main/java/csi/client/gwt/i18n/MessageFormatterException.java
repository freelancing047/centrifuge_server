package csi.client.gwt.i18n;

public class MessageFormatterException extends Exception {
	private String error;
	
	public MessageFormatterException(String error) {
		this.error = error;
	}
	
	public String toString() {
		return error;
	}
}
