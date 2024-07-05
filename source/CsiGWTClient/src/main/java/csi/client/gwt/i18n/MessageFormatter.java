package csi.client.gwt.i18n;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.TimeZone;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.MessageFormatParser.ArgumentChunk;
import csi.client.gwt.i18n.MessageFormatParser.StaticArgChunk;
import csi.client.gwt.i18n.MessageFormatParser.StringChunk;
import csi.client.gwt.i18n.MessageFormatParser.TemplateChunk;
import csi.client.gwt.i18n.MessageFormatParser.TemplateChunkVisitor;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.InternationalizationServiceProtocol;

public class MessageFormatter {
	private static NumberFormat cachedIntegerFormat;

	public static NumberFormat getIntegerFormat() {
		if (cachedIntegerFormat == null) {
			cachedIntegerFormat = NumberFormat.getFormat("#,###");
		}
		return cachedIntegerFormat;
	}

	private static Map<String, ValueFormatter> formatters = new HashMap<String, ValueFormatter>();

	private static Map<String, String> bestPatterns = new TreeMap<String, String>();

	private interface ValueFormatter {
		public void format(StringBuffer out, Map<String, String> formatArgs, Parameters params, String subformat, Object argExpr, Object[] values) throws MessageFormatterException;
	}

	private static class DateFormatter implements ValueFormatter {
		public void format(StringBuffer out, Map<String, String> formatArgs, Parameters params, String subformat, Object argExpr, Object[] values) throws MessageFormatterException {
			if (!(argExpr instanceof java.util.Date)) {
		        throw new MessageFormatterException("Only java.util.Date acceptable for date format");
		    }
			TimeZone tzValue = null;
			String tzArg = formatArgs.get("tz");
			if (tzArg != null) {
				if (tzArg.startsWith("$")) {
					int paramNum = params.getParameterIndex(tzArg.substring(1));
					if (paramNum < 0) {
						throw new MessageFormatterException("Unable to resolve tz argument " + tzArg);
					} else if (!(values[paramNum] instanceof TimeZone)) {
						throw new MessageFormatterException("Currency code parameter must be TimeZone");
					} else {
						tzValue = (TimeZone)values[paramNum];
					}
				} else {
					tzValue = TimeZone.createTimeZone(tzArg);
				}
			}
		      
			if (subformat == null || "medium".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getMediumDateFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getMediumDateFormat().format((Date) argExpr, tzValue));
				}
			} else if ("full".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getFullDateFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getFullDateFormat().format((Date) argExpr, tzValue));
				}
			} else if ("long".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getLongDateFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getLongDateFormat().format((Date) argExpr, tzValue));
				}
			} else if ("short".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getShortDateFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getShortDateFormat().format((Date) argExpr, tzValue));
				}
			} else {
				if (tzValue == null) {
					out.append(DateTimeFormat.getFormat(subformat).format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getFormat(subformat).format((Date) argExpr, tzValue));
				}
			}
		}
	}

	private static class LocalDateTimeFormatter implements ValueFormatter {
		private static final String PREDEF = "predef:";

		public void format(StringBuffer out, Map<String, String> formatArgs, Parameters params, String subformat, Object argExpr, Object[] values) throws MessageFormatterException {
			if (!(argExpr instanceof Date)) {
				throw new MessageFormatterException("Only java.util.Date acceptable for localdatetime format");
			}
			if (subformat == null || subformat.length() == 0) {
				throw new MessageFormatterException("localdatetime format requires a skeleton pattern");
			}
			TimeZone tzValue = null;
			String tzArg = formatArgs.get("tz");
			if (tzArg != null) {
				if (tzArg.startsWith("$")) {
					int paramNum = params.getParameterIndex(tzArg.substring(1));
					if (paramNum < 0) {
						throw new MessageFormatterException("Unable to resolve tz argument " + tzArg);
					} else if (!(values[paramNum] instanceof TimeZone)) {
						throw new MessageFormatterException("Currency code parameter must be TimeZone");
					} else {
						tzValue = (TimeZone) values[paramNum];
					}
				} else {
					tzValue = TimeZone.createTimeZone(tzArg);
				}
			}
			if (subformat.startsWith(PREDEF)) {
				PredefinedFormat predef;
				try {
					predef = PredefinedFormat.valueOf(subformat.substring(PREDEF.length()));
				} catch (IllegalArgumentException e) {
					throw new MessageFormatterException("Unrecognized predefined format '" + subformat + "'");
				}
				if (tzValue == null) {
					out.append(DateTimeFormat.getFormat(predef).format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getFormat(predef).format((Date) argExpr, tzValue));
				}
			} else {
				String pattern = getBestPattern(subformat);
				if (pattern == null) {
					throw new MessageFormatterException("Invalid localdatetime skeleton pattern \"" + subformat + "\"");
				} else {
					if (tzValue == null) {
						out.append(DateTimeFormat.getFormat(pattern).format((Date) argExpr));
					} else {
						out.append(DateTimeFormat.getFormat(pattern).format((Date) argExpr, tzValue));
					}
				}
			}
		}
	}
	
	private static String getBestPattern(final String subformat) {
		if (!bestPatterns.containsKey(subformat)) {
			VortexFuture<String> vortexFuture = WebMain.injector.getVortex().createFuture();
			vortexFuture.execute(InternationalizationServiceProtocol.class).getBestPattern(WebMain.getLanguage(), subformat);
			vortexFuture.addEventHandler(new AbstractVortexEventHandler<String>() {
				@Override
				public void onSuccess(String pattern) {
					bestPatterns.put(subformat, pattern);
				}

				@Override
				public boolean onError(Throwable t) {
					bestPatterns.put(subformat, null);
					return false;
				}

				@Override
				public void onUpdate(int taskProgess, String taskMessage) {
					bestPatterns.put(subformat, null);
				}
			});
		}
		return bestPatterns.get(subformat);
	}

	private static class NumberFormatter implements ValueFormatter {
		public void format(StringBuffer out, Map<String, String> formatArgs, Parameters params, String subformat, Object argExpr,
				Object[] values) throws MessageFormatterException {
			boolean isPrimitive = argExpr.getClass().isPrimitive();
			if (isPrimitive) {
				if (argExpr instanceof Boolean || argExpr instanceof Void) {
					throw new MessageFormatterException("Illegal argument type for number format");
				}
			} else {
				if (!(argExpr instanceof Number)) {
					throw new MessageFormatterException("Only Number subclasses may be formatted as a number");
				}
			}
			String curCodeParam = "";
			String curCode = formatArgs.get("curcode");
			if (curCode != null) {
				if (curCode.startsWith("$")) {
					int paramNum = params.getParameterIndex(curCode.substring(1));
					if (paramNum < 0) {
						throw new MessageFormatterException("Unable to resolve curcode argument " + curCode);
					} else if (!(values[paramNum] instanceof String)) {
						throw new MessageFormatterException("Currency code parameter must be String");
					} else {
						curCodeParam = "arg" + paramNum;
					}
				} else {
					curCodeParam = '"' + curCode + '"';
				}
			}
			if (subformat == null) {
				out.append(NumberFormat.getDecimalFormat().format((Number) argExpr));
			} else if ("integer".equals(subformat)) {
				out.append(getIntegerFormat().format((Number) argExpr));
			} else if ("currency".equals(subformat)) {
				out.append(NumberFormat.getCurrencyFormat(curCode).format((Number) argExpr));
			} else if ("percent".equals(subformat)) {
				out.append(NumberFormat.getPercentFormat().format((Number) argExpr));
			} else {
				if (curCodeParam.length() > 0) {
					out.append(NumberFormat.getFormat(subformat, curCode).format((Number) argExpr));
				} else {
					out.append(NumberFormat.getFormat(subformat).format((Number) argExpr));
				}
			}
		}
	}

	private static class TimeFormatter implements ValueFormatter {
		public void format(StringBuffer out, Map<String, String> formatArgs, Parameters params, String subformat, Object argExpr, Object[] values) throws MessageFormatterException {
			if (!(argExpr instanceof Date)) {
				throw new MessageFormatterException("Only java.util.Date acceptable for date format");
			}
			TimeZone tzValue = null;
			String tzArg = formatArgs.get("tz");
			if (tzArg != null) {
				if (tzArg.startsWith("$")) {
					int paramNum = params.getParameterIndex(tzArg.substring(1));
					if (paramNum < 0) {
						throw new MessageFormatterException("Unable to resolve tz argument " + tzArg);
					} else if (!(values[paramNum] instanceof TimeZone)) {
						throw new MessageFormatterException("Currency code parameter must be TimeZone");
					} else {
						tzValue = (TimeZone) values[paramNum];
					}
				} else {
					tzValue = TimeZone.createTimeZone(tzArg);
				}
			}
			if (subformat == null || "medium".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getMediumTimeFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getMediumTimeFormat().format((Date) argExpr, tzValue));
				}
			} else if ("full".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getFullTimeFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getFullTimeFormat().format((Date) argExpr, tzValue));
				}
			} else if ("long".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getLongTimeFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getLongTimeFormat().format((Date) argExpr, tzValue));
				}
			} else if ("short".equals(subformat)) {
				if (tzValue == null) {
					out.append(DateTimeFormat.getShortTimeFormat().format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getShortTimeFormat().format((Date) argExpr, tzValue));
				}
			} else {
				if (tzValue == null) {
					out.append(DateTimeFormat.getFormat(subformat).format((Date) argExpr));
				} else {
					out.append(DateTimeFormat.getFormat(subformat).format((Date) argExpr, tzValue));
				}
			}
		}
	}

	static {
		formatters.put("date", new DateFormatter());
		formatters.put("number", new NumberFormatter());
		formatters.put("time", new TimeFormatter());
		formatters.put("localdatetime", new LocalDateTimeFormatter());
	}

	public static String format(List<TemplateChunk> chunks, Parameters parameters, Object... arguments) throws MessageFormatterException {
		return doFormat(chunks, parameters, arguments);
	}

	private static String doFormat(List<TemplateChunk> chunks, final Parameters parameters, final Object[] arguments) throws MessageFormatterException {
		final StringBuffer buf = new StringBuffer();

		for (TemplateChunk chunk : chunks) {
			chunk.accept(new TemplateChunkVisitor() {
				@Override
				public void visit(ArgumentChunk argChunk) throws MessageFormatterException {
					String format = argChunk.getFormat();
					Object argExpr = arguments[argChunk.getArgumentNumber()];
					if (format != null) {
						String subformat = argChunk.getSubFormat();
						ValueFormatter formatter = formatters.get(format);
						formatter.format(buf, argChunk.getFormatArgs(), parameters, subformat, argExpr, arguments);
					} else {
						buf.append(argExpr);
					}
				}

				@Override
				public void visit(StaticArgChunk staticArgChunk) throws MessageFormatterException {
					buf.append(staticArgChunk.getReplacement());
				}

				@Override
				public void visit(StringChunk stringChunk) throws MessageFormatterException {
					buf.append(stringChunk.getString());
				}
			});
		}

		return buf.toString();
	}
}
