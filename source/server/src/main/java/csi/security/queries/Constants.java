package csi.security.queries;

/**
 * Created by centrifuge on 3/29/2018.
 */
public class Constants {
   public static final String WHERE_MODIFIER = " WHERE ";
   public static final String AND_MODIFIER = " AND ";
   public static final String JAVA_RESTRICTIVE_JOIN = ", ACL a WHERE (a.uuid = d.uuid.uuid) AND ";
   public static final String SQL_RESTRICTIVE_JOIN = ", ACL a WHERE (a.uuid = d.uuid) AND ";
   public static final String[] WHERE_MODIFIER_PAIR = new String[] { WHERE_MODIFIER, WHERE_MODIFIER };
   public static final String[] AND_MODIFIER_PAIR = new String[] { AND_MODIFIER, AND_MODIFIER };
   public static final String[] RESTRICTIVE_JOIN = new String[] { JAVA_RESTRICTIVE_JOIN, SQL_RESTRICTIVE_JOIN };

   public static final String[] JAVA_TIMESTAMP_TEST = { "(d.createDate >= :date1)", "(d.createDate < :date2)",
                                                        "(d.lastUpdateDate >= :date3)", "(d.lastUpdateDate < :date4)",
                                                        "(d.lastOpenDate >= :date5)", "(d.lastOpenDate < :date6)" };
   public static final String[] SQL_TIMESTAMP_TEST = { "(d.createdate >= :date1)", "(d.createdate < :date2)",
                                                       "(d.lastupdateDate >= :date3)", "(d.lastupdateDate < :date4)",
                                                       "(d.lastopenDate >= :date5)", "(d.lastopenDate < :date6)" };

   public static final String[][] TIMESTAMP_TEST = { JAVA_TIMESTAMP_TEST, SQL_TIMESTAMP_TEST };

   static enum QueryMode {
      JAVA,
      SQL
   }

   static enum MatchingMode {
      NAME_MATCH("(lower(d.name) LIKE lower(:match) ESCAPE '\\') AND "),
      NAME_REJECT("(lower(d.name) NOT LIKE lower(:reject) ESCAPE '\\') AND "),
      NAME_BOTH("((lower(d.name) LIKE lower(:match) ESCAPE '\\') AND (lower(d.name) NOT LIKE lower(:reject) ESCAPE '\\')) AND "),
      REMARKS_MATCH("(lower(d.remarks) LIKE lower(:match) ESCAPE '\\') AND "),
      REMARKS_REJECT("(lower(d.remarks) NOT LIKE lower(:reject) ESCAPE '\\') AND "),
      REMARKS_BOTH("((lower(d.remarks) LIKE lower(:match) ESCAPE '\\') AND (lower(d.remarks) NOT LIKE lower(:reject) ESCAPE '\\')) AND "),
      BOTH_MATCH("((lower(d.name) LIKE lower(:match) ESCAPE '\\') OR (lower(d.remarks) LIKE lower(:match) ESCAPE '\\')) AND "),
      BOTH_REJECT("((lower(d.name) NOT LIKE lower(:reject) ESCAPE '\\') OR (lower(d.remarks) NOT LIKE lower(:reject) ESCAPE '\\')) AND "),
      BOTH_BOTH("(((lower(d.name) LIKE lower(:match) ESCAPE '\\') AND (lower(d.name) NOT LIKE lower(:reject) ESCAPE '\\')) OR ((lower(d.remarks) LIKE lower(:match) ESCAPE '\\') AND (lower(d.remarks) NOT LIKE lower(:reject) ESCAPE '\\'))) AND ");

      private String snippet;

      private MatchingMode(String snippetIn) {
         snippet = snippetIn;
      }

      public static MatchingMode determineMode(String matchPatternIn, String rejectPatternIn,
                                               boolean testNameIn, boolean testRemarksIn) {
         int targetValue = 0;
         int logicValue = 0;

         if (testNameIn) {
            targetValue += 1;
         }
         if (testRemarksIn) {
            targetValue += 2;
         }
         if ((matchPatternIn != null) && (matchPatternIn.length() > 0)) {
            logicValue += 1;
         }
         if ((rejectPatternIn != null) && (rejectPatternIn.length() > 0)) {
            logicValue += 2;
         }
         return ((targetValue > 0) && (logicValue > 0)) ? values()[((targetValue - 1) * 3) + (logicValue - 1)] : null;
      }

      public String getText() {
         return snippet;
      }
   }
}
