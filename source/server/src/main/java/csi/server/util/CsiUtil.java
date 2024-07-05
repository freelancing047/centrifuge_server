package csi.server.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsiUtil {
   private static final Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile("\"");

    static Throwable Generic = new Throwable();

   public static String getStackTraceString(Throwable t) {
      String result = null;

      if (t == null) {
         t = Generic;
      }
      try (ByteArrayOutputStream os = new ByteArrayOutputStream();
           PrintStream ps = new PrintStream(os)) {
         t.printStackTrace(ps);
         result = os.toString();
      } catch (IOException ioe) {
      }
      return result;
   }

    public static void quietClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    public static int compareStrings(String s1, String s2, boolean caseSensitive) {
        if (s1 == null) {
            if (s2 == null) {
                return 0; // s1 == s2
            } else {
                return -1; // s1 < s2
            }
        } else if (s2 == null) {
            return 1; // s1 > s2
        }

        if (caseSensitive) {
            return s1.compareTo(s2);
        } else {
            return s1.compareToIgnoreCase(s2);
        }

    }

    public static String toDelimitedString(Collection<? extends Object> list, String delimiter) {
        return toDelimitedString(list, delimiter, null);
    }

    public static String toDelimitedString(Collection<? extends Object> list, String delimiter, String quotechar) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        for (Object obj : list) {
            if (i > 0) {
                buf.append(delimiter);
            }

            if ((quotechar != null) && !quotechar.isEmpty()) {
                buf.append(quotechar);
                buf.append(obj.toString());
                buf.append(quotechar);
            } else {
                buf.append(obj.toString());
            }
            i++;
        }

        return buf.toString();
    }

    public static String getDistinctName(Set<String> currentNames, String name) {
        String distinct = name;
        int cnt = 1;
        while (currentNames.contains(distinct.toLowerCase())) {
            distinct = name + cnt;
            cnt++;
        }
        return distinct;
    }

    public static String getDistinctName2(Collection<String> currentNames, String name) {
        int i = findNextNameSequence(currentNames, name);
        if (i > -1) {
            return name + " (" + i + ")";
        } else {
            return name;
        }
    }

    public static int findNextNameSequence(Collection<String> currentNames, String basename) {
    	// convert to lowercase otherwise we can't handle case
    	// where current name is Phone1 and basename is Phone1
    	// or phone1
    	List<String> lowercaseNames = new ArrayList<String>();
    	for (String s : currentNames) {
    		if ((s == null) || s.isEmpty()) {
    			continue;
    		}
			lowercaseNames.add(s.toLowerCase());
		}

        int max = -1;
        String regex = "(?i)\\Q" + basename + "\\E \\(([0-9]*)\\)";
        Pattern pattern = Pattern.compile(regex);
        for (String name : lowercaseNames) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                int i = -1;
                try {
                    String num = matcher.group(1);
                    if (num != null) {
                        i = Integer.parseInt(num);
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                i = i + 1;
                if (i > max) {
                    max = i;
                }
            }
        }
        if ((max == -1) && lowercaseNames.contains(basename.toLowerCase())) {
            max = 1;
        }
        return max;
    }

   public static String csvEscape(String text) {
      return (text == null)
                ? ""
                : new StringBuilder("\"")
                            .append(DOUBLE_QUOTE_PATTERN.matcher(text).replaceAll("\"\""))
                            .append("\"")
                            .toString();
    }
}
