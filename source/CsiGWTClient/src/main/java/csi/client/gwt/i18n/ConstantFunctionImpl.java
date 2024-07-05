package csi.client.gwt.i18n;

public class ConstantFunctionImpl implements ConstantFunction {

    private static StringBuilder failures = new StringBuilder();

    private String methodName;
	private String value;

	public ConstantFunctionImpl(String methodName, String value) {
		try {
			if (value == null)
				throw new MessagesFunctionException(methodName, "message doesn't exist in properties file");
			this.methodName = methodName;
			this.value = value;
		} catch (MessagesFunctionException e) {
            failures.append(e.toString());
            failures.append("\n");
		}
	}

    public static boolean hasErrors() {

        return (0 < failures.length());
    }

    public static String getFailures() {

        String myFailures = failures.toString();
        failures = null;

        return myFailures;
    }

	@Override
	public String run() {
		return value;
	}

}
