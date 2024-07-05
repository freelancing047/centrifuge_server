package csi.client.gwt.i18n;

import java.util.List;

import com.google.gwt.user.client.Window;

import csi.client.gwt.i18n.MessageFormatParser.TemplateChunk;

public class MessagesFunctionImpl implements MessagesFunction {
	private String methodName;
	private List<TemplateChunk> chunks;
	private Parameters parameters;

	public MessagesFunctionImpl(String methodName, String template, Parameter[] params) {
		try {
			if (template == null)
				throw new MessagesFunctionException(methodName, "message doesn't exist in properties file");
			this.methodName = methodName;
			chunks = MessageFormatParser.parse(methodName, template);
			parameters = new ParametersImpl(params);
		} catch (MessagesFunctionException e) {
			Window.alert(e.toString());
		}
	}

	public String run(Object... arguments) {
		String value = "";
		try {
			if (arguments.length < parameters.getCount()) {
				throw new MessagesFunctionException(methodName, "not enough arguments (has " + arguments.length + ",  need " + parameters.getCount() + ") provided for this message");
			}
//			int index = 0;
//			while (index < parameters.getNumPoints()) {
//				Parameter parameter = parameters.getParameter(index);
//				String parameterName = parameter.getName();
//				String parameterType = parameter.getQualifiedSourceName();
//				Object argument = arguments[index];
//				Class argumentClass = argument.getClass();
//				try {
//					boolean isAssignable = Class.forName(parameter.getQualifiedSourceName()).isAssignableFrom(argumentClass);
//					if (!isAssignable)
//					{
//						throw new MessagesFunctionException(methodName, "parameter " + parameterName + " (arguments " + index + ") expects a " + parameterType + " but gets a " + argumentClass.getName() + ".");
//					}
//				} catch (ClassNotFoundException e) {
//					throw new MessagesFunctionException(methodName, "type " + parameterType + " of parameter " + parameterName + " (arguments " + index + ") not found.");
//				}
//			}

			try {
				value = MessageFormatter.format(chunks, parameters, arguments);
			} catch (MessageFormatterException e) {
				throw new MessagesFunctionException(methodName, e.toString());
			}
		} catch (MessagesFunctionException e) {
			Window.alert(e.toString());
		} 
		return value;
	}
}
