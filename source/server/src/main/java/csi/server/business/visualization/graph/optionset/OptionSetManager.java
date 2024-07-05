package csi.server.business.visualization.graph.optionset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;

import csi.config.RelGraphConfig;
import csi.server.common.exception.CentrifugeException;

public class OptionSetManager {
    public static String                      ICON_URL_BASE = "/Centrifuge/resources/icons/";

    private static HashMap<String, OptionSet> optionSets    = new HashMap<String, OptionSet>();

    private static XStream                    codec         = new XStream();
    static {
        codec.alias("OptionSet", OptionSet.class);
        codec.registerConverter(new OptionSetConverter());
        codec.registerConverter(new OptionConverter());
    }

    public static OptionSet getOptionSetOrDefault(String name) throws CentrifugeException {
        if ((name != null) && (optionSets.get(name) != null)) {
            return optionSets.get(name);
        }
        OptionSet options = null;
        CentrifugeException pending = null;
        if (checkResourceExists(name)) {
            try {
                options = getOptionSet(name);
            } catch (CentrifugeException t) {
                pending = t;

            }
        }

        RelGraphConfig rgc = csi.config.Configuration.getInstance().getGraphConfig();
        String defaultTheme = rgc.getDefaultTheme();
        if( (options == null) && !defaultTheme.equals(name)) {
            try {
                options = getOptionSet(defaultTheme);
            } catch(Throwable t ) {
            }
        }

        // well we got our-selves into a pickle.  we cannot load the option
        // set -- this includes our baseline if that wasn't the one asked for.
        // throw the original error
        if( pending != null ) {
            throw pending;
        }

        return options;

    }

    static boolean checkResourceExists(String name) {
        File file = new File("webapps/Centrifuge/resources/OptionSets/" + name + ".xml");
        return file.exists();
    }

   public synchronized static OptionSet getOptionSet(String name)
         throws CentrifugeException {
      OptionSet set = null;

      if (name != null) {
         set = optionSets.get(name);

         if (set == null) {
            File optionFile = new File("webapps/Centrifuge/resources/OptionSets/" + name + ".xml");

            try (FileInputStream fis = new FileInputStream(optionFile)) {
               set = (OptionSet) codec.fromXML(fis);

               if ((set.name == null) || set.name.isEmpty()) {
                  set.name = name;
               }
               optionSets.put(name, set);
            } catch (FileNotFoundException e) {
               throw new CentrifugeException("Option set file not found: " + optionFile);
            } catch (IOException ioe) {
               throw new CentrifugeException("Option set file not found: " + optionFile);
            }
         }
      }
      return set;
   }

    public static String toResourceUrl(String res) {
        if (res == null) {
            return null;
        }
        return ICON_URL_BASE + res;
    }

   public static void main(String[] args) {
      try {
         OptionSet s = getOptionSet("Baseline");
         System.out.println(codec.toXML(s));
      } catch (CentrifugeException e) {
      }
   }
}
