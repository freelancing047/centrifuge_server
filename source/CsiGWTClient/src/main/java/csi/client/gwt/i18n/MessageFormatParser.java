package csi.client.gwt.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFormatParser {
	public abstract static class TemplateChunk {
		protected static String quoteMessageFormatChars(String str) {
			str = str.replace("'", "''");
			str = str.replace("{", "'{'");
			str = str.replace("}", "'}'");
			return str;
		}

		protected static String quoteMessageFormatChars(String str, boolean quote) {
			return quote ? quoteMessageFormatChars(str) : str;
		}

		public abstract void accept(TemplateChunkVisitor visitor) throws MessageFormatterException;

		public String getAsMessageFormatString() {
			return getStringValue(true);
		}

		public String getString() {
			return getStringValue(false);
		}

		public boolean isLiteral() {
			return false;
		}

		protected abstract String getStringValue(boolean quote);
	}
	
	public interface TemplateChunkVisitor {
	    void visit(ArgumentChunk argChunk) throws MessageFormatterException;

	    void visit(StaticArgChunk staticArgChunk) throws MessageFormatterException;

	    void visit(StringChunk stringChunk) throws MessageFormatterException;
	  }

	public static List<TemplateChunk> parse(String methodName, String template) throws MessagesFunctionException {
		int curPos = 0;
		boolean inQuote = false;
		int templateLen = template.length();
		ArrayList<TemplateChunk> chunks = new ArrayList<TemplateChunk>();
		TemplateChunk curChunk = null;
		while (curPos < templateLen) {
			char ch = template.charAt(curPos++);	    
			switch (ch) {	    
	        case '\'':
		          if (curPos < templateLen && template.charAt(curPos) == '\'') {
			            curChunk = appendString(chunks, curChunk, "'");
			            ++curPos;
			            break;
		          }
		          inQuote = !inQuote;
		          break;
	        case '{':
		          if (inQuote) {
			            curChunk = appendString(chunks, curChunk, "{");
			            break;
		          }
		          StringBuilder argBuf = new StringBuilder();
		          boolean argQuote = false;
		          while (curPos < templateLen) {
			            ch = template.charAt(curPos++);
			            if (ch == '\'') {
				              if (curPos < templateLen && template.charAt(curPos) == '\'') {
					                argBuf.append(ch);
					                ++curPos;
				              } else {
					                argQuote = !argQuote;
				              }
			            } else {
				              if (!argQuote && ch == '}') {
					                break;
				              }
				              argBuf.append(ch);
			            }
		          }
		          if (ch != '}') {
			            throw new MessagesFunctionException(methodName, "Invalid message format - { not start of valid argument" + template);
		          }
		          if (curChunk != null) {
		        	  chunks.add(curChunk);
		          }
		          String arg = argBuf.toString();
		          int firstComma = arg.indexOf(',');
		          String format = null;
				if (firstComma > 0) {
					format = arg.substring(firstComma + 1);
			            arg = arg.substring(0, firstComma);
		          }
		          if (!"#".equals(arg) && !Character.isDigit(arg.charAt(0))) {
			            // static argument
			            chunks.add(new StaticArgChunk(arg, format));
		          } else {
			            int argNumber = -1;
			            if (!"#".equals(arg)) {
				              argNumber = Integer.valueOf(arg);
			            }
			            Map<String, String> formatArgs = new HashMap<String, String>();
			            Map<String, String> listArgs = null;
			            String subFormat = null;
			            if (format != null) {
				              int comma = format.indexOf(',');
				              if (comma >= 0) {
					                subFormat = format.substring(comma + 1);
					                format = format.substring(0, comma);
				              }
				              format = parseFormatArgs(format, formatArgs);
				              if ("list".equals(format)) {
					                listArgs = formatArgs;
					                formatArgs = new HashMap<String, String>();
					                format = subFormat;
					                subFormat = null;
					                if (format != null) {
						                  comma = format.indexOf(',');
						                  if (comma >= 0) {
							                    subFormat = format.substring(comma + 1);
							                    format = format.substring(0, comma);
						                  }
						                  format = parseFormatArgs(format, formatArgs);
					                }
				              }
			            }
			            chunks.add(new ArgumentChunk(argNumber, listArgs, format, formatArgs, subFormat));
		          }
		          curChunk = null;
		          break;

//	        case '\n':
//	          curChunk = appendString(chunks, curChunk, "\\n");
//	          break;
//
//	        case '\r':
//	          curChunk = appendString(chunks, curChunk, "\\r");
//	          break;
//
//	        case '\\':
//	          curChunk = appendString(chunks, curChunk, "\\\\");
//	          break;
//
//	        case '"':
//	          curChunk = appendString(chunks, curChunk, "\\\"");
//	          break;

	        default:
		          curChunk = appendString(chunks, curChunk, String.valueOf(ch));
		          break;
		    }
	    }
	    if (inQuote) {
		      throw new MessagesFunctionException(methodName, "Unterminated single quote: " + template);
	    }
	    if (curChunk != null) {
		      chunks.add(curChunk);
	    }
	    return chunks;
	}
	
	private static TemplateChunk appendString(ArrayList<TemplateChunk> chunks, TemplateChunk curChunk, String string) {
		if (curChunk != null && !curChunk.isLiteral()) {
			chunks.add(curChunk);
			curChunk = null;
		}
		if (curChunk == null) {
			curChunk = new StringChunk(string);
		} else {
			((StringChunk) curChunk).append(string);
		}
		return curChunk;
	}
	
	private static String parseFormatArgs(String format, Map<String, String> formatArgs) {
		int colon = format.indexOf(':');
		if (colon >= 0) {
			for (String tagValue : format.substring(colon + 1).split(":")) {
				int equals = tagValue.indexOf('=');
				String value = "";
				if (equals >= 0) {
					value = tagValue.substring(equals + 1).trim();
					tagValue = tagValue.substring(0, equals);
				}
				formatArgs.put(tagValue.trim(), value);
			}
			format = format.substring(0, colon);
		}
		return format;
	}		
		
	
	public static class StringChunk extends TemplateChunk {

		private StringBuilder buf = new StringBuilder();

		public StringChunk() {
		}

		public StringChunk(String str) {
			buf.append(str);
		}

		@Override
		public void accept(TemplateChunkVisitor visitor) throws MessageFormatterException {
			visitor.visit(this);
		}

		public void append(String str) {
			buf.append(str);
		}

		@Override
		public boolean isLiteral() {
			return true;
		}

		@Override
		public String toString() {
			return "StringLiteral: \"" + buf.toString() + "\"";
		}

		@Override
		protected String getStringValue(boolean quote) {
			String str = buf.toString();
			return quoteMessageFormatChars(str, quote);
		}
	}
	
	public static class StaticArgChunk extends TemplateChunk {

		private final String argName;
		private final String replacement;

		public StaticArgChunk(String argName, String replacement) {
			this.argName = argName;
			this.replacement = replacement;
		}

		@Override
		public void accept(TemplateChunkVisitor visitor) throws MessageFormatterException {
			visitor.visit(this);
		}

		public String getArgName() {
			return argName;
		}

		public String getReplacement() {
			return replacement;
		}

		@Override
		protected String getStringValue(boolean quoted) {
			StringBuilder buf = new StringBuilder();
			buf.append('{').append(argName);
			if (replacement != null) {
				buf.append(',').append(quoteMessageFormatChars(replacement, quoted));
			}
			buf.append('}');
			return buf.toString();
		}
	}
	
	public static class ArgumentChunk extends TemplateChunk {

		private final int argNumber;
		private final String format;
		private final Map<String, String> formatArgs;
		private final String subFormat;
		private final Map<String, String> listArgs;

		public ArgumentChunk(int argNumber, Map<String, String> listArgs, String format, Map<String, String> formatArgs, String subformat) {
			this.argNumber = argNumber;
			this.format = format;
			this.subFormat = subformat;
			this.listArgs = listArgs;
			this.formatArgs = formatArgs;
		}

		@Override
		public void accept(TemplateChunkVisitor visitor) throws MessageFormatterException {
			visitor.visit(this);
		}

		/**
		 * Get the argument number this chunk refers to.
		 * 
		 * @return the argument number or -1 to refer to the right-most plural
		 *         argument
		 */
		public int getArgumentNumber() {
			return argNumber;
		}

		public String getFormat() {
			return format;
		}

		public Map<String, String> getFormatArgs() {
			return formatArgs;
		}

		public Map<String, String> getListArgs() {
			return listArgs;
		}

		public String getSubFormat() {
			return subFormat;
		}

		public boolean isList() {
			return listArgs != null;
		}

		@Override
		public String toString() {
			return "Argument: #=" + argNumber + ", format=" + format + ", subformat=" + subFormat;
		}

		@Override
		protected String getStringValue(boolean quote) {
			StringBuilder buf = new StringBuilder();
			buf.append('{');
			if (argNumber < 0) {
				buf.append('#');
			} else {
				buf.append(argNumber);
			}
			Map<String, String> map = listArgs;
			if (map != null) {
				buf.append(",list");
				appendArgs(buf, map, quote);
			}
			if (format != null || subFormat != null) {
				buf.append(',');
			}
			if (format != null) {
				buf.append(quoteMessageFormatChars(format, quote));
				appendArgs(buf, formatArgs, quote);
			}
			if (subFormat != null) {
				buf.append(',');
				buf.append(subFormat);
			}
			buf.append('}');
			return buf.toString();
		}

		/**
		 * @param buf
		 * @param map
		 * @param quote
		 */
		private void appendArgs(StringBuilder buf, Map<String, String> map, boolean quote) {
			char prefix = ':';
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				if (quote) {
					key = quoteMessageFormatChars(key);
				}
				buf.append(prefix).append(key);
				String value = entry.getValue();
				if (value != null) {
					if (quote) {
						value = quoteMessageFormatChars(value);
					}
					buf.append('=').append(value);
				}
				prefix = ',';
			}
		}
	}
}
