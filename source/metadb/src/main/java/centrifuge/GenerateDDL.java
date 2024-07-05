package centrifuge;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class GenerateDDL {
   public static void main(String[] args) {
      Configurator.setLevel(LogManager.getRootLogger().getName(), Level.WARN);

      String outdir = args[0];
      Map<String, String> props = new HashMap<String, String>();
      Ejb3Configuration config = new Ejb3Configuration();

      config.configure("meta", props);

      AnnotationConfiguration hibernateConfiguration = config.getHibernateConfiguration();
      SchemaExport exporter = new SchemaExport(hibernateConfiguration);

      exporter.setOutputFile(outdir + "/createDDL.jdbc");
      exporter.setDelimiter(";");

      boolean reportToConsole = false;
      boolean pushToDB = false;
      boolean create = true;
      boolean drop = false;

      exporter.execute(reportToConsole, pushToDB, drop, create);
      System.out.println("hello");
      exporter.setOutputFile(outdir + "/dropDDL.jdbc");

      drop = true;
      create = false;

      exporter.execute(pushToDB, reportToConsole, drop, create);
   }
}
