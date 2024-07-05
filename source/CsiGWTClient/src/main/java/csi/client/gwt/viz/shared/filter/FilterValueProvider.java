package csi.client.gwt.viz.shared.filter;

import com.sencha.gxt.core.client.ValueProvider;

import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.ScalarValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 3/7/2019.
 */
public class FilterValueProvider implements ValueProvider<FilterExpression,String> {
   /**
    * Returns the property value of the given object.
    *
    * @param expressionIn
    * @return the property value
    */
   public String getValue(FilterExpression expressionIn) {
      String value = null;

      if (expressionIn != null) {
         ValueDefinition valueDefinition = expressionIn.getValueDefinition();

         if (valueDefinition != null) {
            if (RelationalOperator.LIKE.equals(expressionIn.getOperator()) &&
                (valueDefinition instanceof ScalarValueDefinition)) {

               value = StringUtil.quoteAndEscape(
                          StringUtil.patternFromSql((String) ((ScalarValueDefinition) valueDefinition).getValue().getValue()), '\'');
            } else {
               value = valueDefinition.getShortValueDescription();
            }
         }
      }
      return value;
   }

   /**
    * Sets the value of the given object
    *
    * @param expressionIn
    * @param valueIn
    */
   public void setValue(FilterExpression expressionIn, String valueIn) {
   }

   /**
    * Returns the path that this ValueProvider makes available, from the object, to
    * the value.
    *
    * @return the path from the object to the value
    */
   public String getPath() {
      return ""; //$NON-NLS-1$
   }
}
