package csi.startup;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import prefuse.data.expression.parser.ExpressionParser;

public class JULIInitializer {
   public void initialize() {
      Configurator.setLevel(LogManager.getLogger(ExpressionParser.class).getName(), Level.WARN);
   }
}
