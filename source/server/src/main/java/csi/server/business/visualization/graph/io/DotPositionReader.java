package csi.server.business.visualization.graph.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.visual.VisualItem;

public class DotPositionReader {
   private static final Logger LOG = LogManager.getLogger(DotPositionReader.class);

   private static final Pattern DOT_POSITION_PATTERN = Pattern.compile("(\\w*?)=\"([^\"]*?)\"(,\\s*?|$)");

   boolean graphStarted = false;
   boolean seenNode = false;
   boolean skipToEOL = false;

   protected static String getEntry(BufferedReader reader) throws IOException {
      StringBuilder builder = new StringBuilder();
      String line = null;

      do {
         line = reader.readLine();
         builder.append(line);
      } while ((line != null) && !line.endsWith(";"));

      return builder.toString().trim();
   }

   public void read(Graph graph, BufferedReader reader) throws IOException {
      // prologue....
      reader.readLine();

      String line = getEntry(reader);

      while (line.startsWith("graph") || line.startsWith("node")) {
         line = getEntry(reader);
      }
      // deal with nodes
      while ((line.length() > 0) && !line.startsWith("}")) {
         if (line.indexOf("->") == -1) {
            int pos = line.indexOf(' ');

            if (pos == -1) {
               LOG.debug("Line has no separator for node identifier: " + line);
            } else {
               String id = line.substring(0, line.indexOf(' '));
               int nodeId = Integer.parseInt(id);
               String attrs = line.substring(line.indexOf('[') + 1, line.lastIndexOf("];"));
               double sX = 2.0d;
               double sY = 1.5d;
               Matcher matcher = DOT_POSITION_PATTERN.matcher(attrs);

               while (matcher.find()) {
                  if ("pos".equals(matcher.group(1))) {
                     String vals = matcher.group(2);
                     String[] split = vals.split(",");

                     try {
                        double x = Double.parseDouble(split[0]);
                        double y = Double.parseDouble(split[1]);

                        if (graph != null) {
                           VisualItem item = (VisualItem) graph.getNode(nodeId);

                           item.setX((sX * x));
                           item.setY((sY * y));
                        }
                     } catch (NumberFormatException nfe) {
                        // todo: need to adjust for arbitrary numbers!
                     }
                  }
               }
            }
         } else {
            LOG.debug("Skipping edge");
         }
         line = getEntry(reader);
      }
   }
}
