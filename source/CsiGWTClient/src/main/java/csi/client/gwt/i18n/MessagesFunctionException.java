package csi.client.gwt.i18n;

public class MessagesFunctionException extends Exception {
	private String methodName;
	private String errorMessage;
	public MessagesFunctionException(String methodName, String errorMessage) {
		this.methodName = methodName;
		this.errorMessage = errorMessage;
	}
	
	public String toString() {
		return methodName + " : " + errorMessage;
	}
}
