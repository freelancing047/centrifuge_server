package csi.server.util.sql;

import java.util.Comparator;
import java.util.regex.Pattern;

import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.util.CacheUtil;

/**
 * Created by centrifuge on 10/4/2018.
 */
public class CacheCommands {
   public static final int BATCH_INSERT_SIZE = 128;

   public static final Pattern CALL_PATTERN = Pattern.compile("(?i)\\bCALL\\b");
   public static final Pattern UNION_PATTERN = Pattern.compile("(?i)\\bUNION\\b");

   public static final String SELECT_ALL_QUERY = "select * from %1$s ";

   public static final String SELECT_QUERY = "select %1$s from %2$s ";

   public static final String SELECT_SUBSET_QUERY = "select * from %1$s WHERE "
         + CacheUtil.quote(CacheTokens.CSI_ROW_ID) + " IN (%2$s)";

   public static final String SELECT_COUNT_QUERY = "select count(" + CacheUtil.quote(CacheTokens.CSI_ROW_ID)
         + ") from \"%1$s\" ";

   public static final String RENAME_CACHE_TABLE = "ALTER TABLE %1$s RENAME TO %2$s";

   public static final String RENAME_CACHE_VIEW = "ALTER VIEW IF EXISTS %1$s RENAME TO %2$s";

   public static final String RENAME_CACHE_IDENTITY = "ALTER SEQUENCE %1$s RENAME TO %2$s";

   public static final String RENAME_CACHE_COLUMN = "ALTER TABLE %1$s RENAME COLUMN %2$s TO %3$s";

   public static final String DROP_CACHE_TABLE = "DROP TABLE IF EXISTS %1$s CASCADE";

   public static final String DROP_CACHE_VIEW = "DROP VIEW IF EXISTS %1$s CASCADE";

   public static final String DROP_CACHE_IDENTITY = "DROP SEQUENCE IF EXISTS %s CASCADE";

   public static final String ADD_CACHE_FIELD = "ALTER TABLE %1$s ADD %2$s %3$s";

   public static final String DROP_CACHE_FIELD = "ALTER TABLE %1$s DROP COLUMN %2$s";

   public static final String INSTALLED_TABLE_KEY = "installedtabledriver";

   public static final String GRANT_SELECT_PERMISSION = "GRANT SELECT ON %1$s TO csiuser";

   public static final String GRANT_INSERT_PERMISSION = "GRANT INSERT ON %1$s TO csiuser";

   public static final String GRANT_UPDATE_PERMISSION = "GRANT UPDATE ON %1$s TO csiuser";

   public static final String REVOKE_SELECT_PERMISSION = "REVOKE SELECT ON %1$s FROM csiuser";

   public static final String REVOKE_INSERT_PERMISSION = "REVOKE INSERT ON %1$s FROM csiuser";

   public static final String REVOKE_UPDATE_PERMISSION = "REVOKE UPDATE ON %1$s FROM csiuser";

   public static final String COUNT_TABLE_QUERY = "SELECT COUNT(*) FROM \"information_schema\".\"tables\""
         + " WHERE \"table_type\" = 'BASE TABLE' AND \"table_name\" = %1$s";

   public static final String COUNT_VIEW_QUERY = "SELECT COUNT(*) FROM \"information_schema\".\"tables\""
         + " WHERE \"table_type\" = 'VIEW' AND \"table_name\" = %1$s";

   public static final String TRUNCATE_TABLE_COMMAND = "TRUNCATE %1$s";

   public static final Comparator<? super FieldDef> COMPARE_VIEW_FIELDLIST_ORDER = new Comparator<FieldDef>() {
      @Override
      public int compare(FieldDef f1, FieldDef f2) {
         return f1.getOrdinal() - f2.getOrdinal();
      }
   };

   public static final Comparator<? super FieldDef> COMPARE_UI_FIELDLIST_ORDER = new Comparator<FieldDef>() {
      @Override
      public int compare(FieldDef f1, FieldDef f2) {
         int type = f1.getFieldType().compareTo(f2.getFieldType());

         if (type == 0) {
            /**/
            if ((f1.getFieldType() == FieldType.COLUMN_REF) || (f1.getFieldType() == FieldType.LINKUP_REF)) {
               return f1.getOrdinal() - f2.getOrdinal();
            } else {
               return compareStrings(f1.getFieldName(), f2.getFieldName());
            }
            /*
             * return f1.getOrdinal() - f2.getOrdinal();
             */
         } else {
            return type;
         }
      }
   };

   public static final Comparator<TableViewSortField> SORTFIELD_COMPARATOR = new Comparator<TableViewSortField>() {
      @Override
      public int compare(TableViewSortField o1, TableViewSortField o2) {
         Integer p1 = Integer.valueOf(o1.getListPosition());
         Integer p2 = Integer.valueOf(o2.getListPosition());
         return p1.compareTo(p2);
      }
   };

   public static final Comparator<VisibleTableField> VISIBLE_FIELD_COMPARATOR = new Comparator<VisibleTableField>() {
      @Override
      public int compare(VisibleTableField o1, VisibleTableField o2) {
         Integer p1 = Integer.valueOf(o1.getListPosition());
         Integer p2 = Integer.valueOf(o2.getListPosition());
         return p1.compareTo(p2);
      }
   };

   public static int compareStrings(String s1, String s2) {
      if (s1 == null) {
         if (s2 == null) {
            return 0; // s1 == s2
         } else {
            return -1; // s1 < s2
         }
      } else if (s2 == null) {
         return 1; // s1 > s2
      }
      return s1.compareToIgnoreCase(s2);
   }
}
