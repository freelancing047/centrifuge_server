package csi.server.common.model.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.shared.core.util.HasLabel;

public enum FilterOperatorType implements HasLabel {
   BEGINS_WITH("starts with") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return new ArrayList<FilterOperandType>(Arrays.asList(FilterOperandType.STATIC, FilterOperandType.PARAMETER));
      }
   },
   CONTAINS("contains") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return new ArrayList<FilterOperandType>(Arrays.asList(FilterOperandType.STATIC, FilterOperandType.PARAMETER));
      }
   },
   EMPTY("is empty") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return Collections.emptyList();
      }
   },
   ENDS_WITH("ends with") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return new ArrayList<FilterOperandType>(Arrays.asList(FilterOperandType.STATIC, FilterOperandType.PARAMETER));
      }
   },
   EQUALS("equals"),
   GT("is greater than"),
   GEQ("is greater than or equal to"),
   LT("is less than"),
   LEQ("is less than or equal to"),
   IN_LIST("in list") {
      @Override
      public boolean isApplicableToDSEColumnWhereClause() {
         return false;
      }
   },
   ISNULL("is null") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return Collections.emptyList();
      }
   },
   NULL_OR_EMPTY("is null or empty") {
      @Override
      public List<FilterOperandType> getApplicableOperands() {
         return Collections.emptyList();
      }
   };

   private static final List<FilterOperandType> ALL_OPERANDS =
      new ArrayList<FilterOperandType>(Arrays.asList(FilterOperandType.values()));

   private String label;

   private FilterOperatorType(String label) {
      this.label = label;
   }

   public boolean hasOneOperand() {
      return ((this == EMPTY) || (this == ISNULL) || (this == NULL_OR_EMPTY));
   }

   @Override
   public String getLabel() {
      return label;
   }

   public boolean isApplicableToDSEColumnWhereClause() {
      return true;
   }

   public List<FilterOperandType> getApplicableOperands() {
      return ALL_OPERANDS;
   }

   /*
    * Boolean("boolean", "Boolean", "BOOLEAN", "BOOLEAN", CsiBaseDataType.BOOLEAN, 16), //java.sql.Types.BOOLEAN
    * Integer("integer", "Integer", "BIGINT", "BIGINT", CsiBaseDataType.NUMERIC, -5), //java.sql.Types.BIGINT
    * Number("number", "Float", "DOUBLE PRECISION", "DOUBLE", CsiBaseDataType.NUMERIC, 8), //java.sql.Types.DOUBLE
    * DateTime("datetime", "Date-Time", "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP", CsiBaseDataType.TEMPORAL, 93), //java.sql.Types.TIMESTAMP
    * Date("date", "Date", "DATE", "DATE", CsiBaseDataType.TEMPORAL, 91), // java.sql.Types.DATE
    * Time("time", "Time", "TIME WITHOUT TIME ZONE", "TIME", CsiBaseDataType.TEMPORAL, 92), //java.sql.Types.TIME
    * Unsupported("unsupported", null, "TEXT", "CHAR", null, 12); //java.sql.Types.VARCHAR
    */

   public static List<FilterOperatorType> getApplicableOperators(CsiDataType operandTypeIn) {
      List<FilterOperatorType> operators = new ArrayList<FilterOperatorType>();

      switch (operandTypeIn) {
         case String:
            operators.add(EQUALS);
            operators.add(CONTAINS);
            operators.add(BEGINS_WITH);
            operators.add(ENDS_WITH);
            operators.add(EMPTY);
            operators.add(NULL_OR_EMPTY);
            operators.add(LT);
            operators.add(LEQ);
            operators.add(GEQ);
            operators.add(GT);
            operators.add(IN_LIST);
            break;

         case DateTime:
         case Date:
         case Time:
         case Integer:
         case Number:
            operators.add(ISNULL);
            operators.add(LT);
            operators.add(LEQ);
            operators.add(EQUALS);
            operators.add(GEQ);
            operators.add(GT);
            operators.add(IN_LIST);
            break;

         case Boolean:
         case Unsupported:
            operators.add(ISNULL);
            operators.add(EQUALS);
            operators.add(IN_LIST);
            break;
      }
      return operators;
   }
}
