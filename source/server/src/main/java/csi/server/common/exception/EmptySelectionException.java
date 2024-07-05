package csi.server.common.exception;

public class EmptySelectionException extends CentrifugeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmptySelectionException(String id, String message, Throwable cause) {
		super(id, message, cause);
	}

	public EmptySelectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptySelectionException(String message) {
		super(message);
	}

	public EmptySelectionException(Throwable cause) {
		super(cause);
	}

}
