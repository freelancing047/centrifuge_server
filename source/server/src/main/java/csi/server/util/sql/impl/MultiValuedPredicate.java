/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.util.sql.impl;

import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.util.SqlUtil;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.PredicateFragmentSpi;
import csi.shared.core.util.CsiArrayUtils;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MultiValuedPredicate extends AbstractSimplePredicate {

    private List<? extends Object> values;
    private CsiDataType dataType;

    public MultiValuedPredicate(PredicateFragmentSpi predicateFragment, List<? extends Object> values,
            CsiDataType dataType) {
        super(predicateFragment);
        this.values = values;
        this.dataType = dataType;
    }

    @Override
    public String getSQL() {
        if (dataType == CsiDataType.String) {
            String value = getValue();
            String checker = ((ColumnSpi)getPredicateFragment().getColumn()).getSQLWithoutTableAlias();

            return "trim(" + checker + ") " + getPredicateFragment().getRelationalOperator() + value ;
        }
        return getPredicateFragment().getSQLFragment() + getValue();
    }

    @Override
    public String getAliasedSQL() {
        if (dataType == CsiDataType.String) {
            String value = getValue();
            String checker = ((ColumnSpi)getPredicateFragment().getColumn()).getSQLWithoutTableAlias();

            return "trim(" + checker + ") " + getPredicateFragment().getRelationalOperator() + value ;

        }
        return getPredicateFragment().getAliasedSQLFragment() + getValue();
    }

    private String getValue() {
       StringBuilder builder = new StringBuilder();
       for (Object value : values) {
           builder.append(",");
           switch (dataType) {
               case Boolean:
               case Integer:
               case Number:
                   builder.append(value);
                   break;
               case Date:
               case DateTime:
               case Time:
                   builder.append("'").append(value).append("'");
                   break;
               case String:
                   builder.append(SqlUtil.singleQuoteWithEscape(value.toString()));
                   break;
               case Unsupported:
                   throw new RuntimeException("Don't know anything about " + dataType);
           }
       }
       String listPart = builder.length() > 0 ? builder.substring(1) : "";
       return "(" + listPart + ")";
   }
//   private String getValue() {
//      StringJoiner builder = new StringJoiner(",", "(", ")");
//
//      for (Object value : values) {
//         switch (dataType) {
//            case Integer:
//            case Boolean:
//            case Number:
//               builder.add((String) value);
//               break;
//            case Date:
//            case DateTime:
//            case Time:
//               builder.add(new StringBuilder("'").append(value).append("'").toString());
//               break;
//            case String:
//               builder.add(SqlUtil.singleQuoteWithEscape(value.toString()));
//               break;
//            case Unsupported:
//               throw new RuntimeException("Don't know anything about " + dataType);
//         }
//      }
//      return builder.toString();
//   }

    /**
     * We use this to determine if these predicates can be merged.
     * Thereby avoiding having multiple IN or other multi-valued operators for the same data
     * @param predicate
     * @return
     */
   public boolean canCombine(MultiValuedPredicate predicate){
      boolean predicateFracgmentNull =
         (getPredicateFragment() != null) &&
         (dataType != null) &&
         (getPredicateFragment().getColumn() != null) &&
         (getPredicateFragment().getSQLFragment() != null);
      boolean predicateNull =
         (predicate != null) &&
         (predicate.getPredicateFragment() != null) &&
         (predicate.getPredicateFragment().getSQLFragment() != null) &&
         (predicate.getDataType() != null) &&
         (getPredicateFragment().getColumn() != null);

      return (!predicateFracgmentNull && !predicateNull && (predicate.getDataType() == dataType) &&
            getPredicateFragment().getSQLFragment().equals(predicate.getPredicateFragment().getSQLFragment()));
   }

    /**
     * Use method canCombine first to check that this won't blow up
     * Does an inner join between the two types of elements
     * aka this "ANDS" two lists.
     *
     * @param predicate
     */
    @SuppressWarnings("unchecked")
    public void join(MultiValuedPredicate predicate){
        Object[] firstValues = values.toArray();
        Object[] secondValues = predicate.getValues().toArray();

        if((firstValues != null) && (firstValues.length != 0)
                && (secondValues != null) && (secondValues.length != 0)){
            Object firstVal = firstValues[0];
            Object secondVal = secondValues[0];
            if(!firstVal.getClass().equals(secondVal.getClass())){
                //This is bad, we have different types
                if((firstVal instanceof String) && (secondVal instanceof Integer)){
                    firstValues = convertStringArrayToInt(firstValues);
                } else if((secondVal instanceof String) && (firstVal instanceof Integer)){
                    secondValues = convertStringArrayToInt(secondValues);
                }
            }
        }

        List<Object> result = CsiArrayUtils.join(firstValues, secondValues);

        values = result;
    }

    private Object[] convertStringArrayToInt(Object[] stringValues) {
        Object[] integerValues = new Object[stringValues.length];

        for(int ii=0; ii<stringValues.length; ii++){
            try{
                integerValues[ii] = Integer.decode((String) stringValues[ii]);
            } catch(Exception e){
                //no-op for now
            }
        }

        return integerValues;
    }

    public CsiDataType getDataType() {
        return dataType;
    }

    public List<? extends Object> getValues() {
        return values;
    }
}
