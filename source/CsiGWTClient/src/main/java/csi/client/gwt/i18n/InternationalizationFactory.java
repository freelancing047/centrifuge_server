package csi.client.gwt.i18n;

public class InternationalizationFactory {
	public static ConstantFunction createConstantFunction(String methodName, String value) {
		return new ConstantFunctionImpl(methodName, value);
	}

	public static MessagesFunction createMessagesFunction(String methodName, String template, Parameter[] params) {
		return new MessagesFunctionImpl(methodName, template, params);
	}
}
