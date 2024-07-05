package csi.server.business.cachedb.querybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.util.CacheUtil;
import csi.server.util.sql.CacheTokens;

public class TableQueryBuilder {

    public static final String SELECT_QUERY = "select %1$s from %2$s ";

    private DataView dataview;
    private final TableViewDef tableDef;
    private Collection<String> selectColumns;

    private boolean hasOffset = false;
    private long offset = 0;

    private boolean hasLimit = false;
    private long limit;

    private boolean includeOrdering = true;
    private FilterActionsService filterActionsService;

    private List<TableViewSortField> clientSortFields;

    private String dateFormat;

    public TableQueryBuilder(DataView dataviewIn, TableViewDef tableDef, List<TableViewSortField> clientSortFields) {
        if (dataviewIn == null) {
            throw new IllegalArgumentException("Null Dataview");
        }

        if (tableDef == null) {
            throw new IllegalArgumentException("Null TableDef");
        }

        this.clientSortFields = clientSortFields;
        this.dataview = dataviewIn;
        this.tableDef = tableDef;
    }

    public void setOffset(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Offset value cannot be negative");
        }
        hasOffset = true;
        offset = value;
    }

    public void setLimit(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }

        hasLimit = true;
        limit = value;
    }

    public boolean isIncludeOrdering() {
        return includeOrdering;
    }

    public void setIncludeOrdering(boolean includeOrdering) {
        this.includeOrdering = includeOrdering;
    }

    public String buildQuery() {
        return buildQuery(new IntegerRowsSelection(), false);
    }

    public String buildQuery(IntegerRowsSelection limitBySelection, boolean ignoreBroadcast) {
        StringBuilder builder = new StringBuilder();

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        String selection = buildSelection(dcHelper);

        List<TableViewSortField> sortFields = new ArrayList<TableViewSortField>();
        if(clientSortFields != null){
            sortFields.addAll(clientSortFields);
        }
        List<TableViewSortField> tableSettingsSortFields = tableDef.getTableViewSettings().getSortFields();
        if(tableSettingsSortFields != null) {
            sortFields.addAll(tableSettingsSortFields);
        }

        String orderByClause = dcHelper.buildOrderByClause(dataview.getMeta().getModelDef(), sortFields);

        String filterClause = buildFilterClause(dcHelper, limitBySelection, ignoreBroadcast);

        builder.append(String.format(SELECT_QUERY, selection, CacheUtil.getQuotedCacheTableName(dataview.getUuid())));
        if ((filterClause != null) && !filterClause.isEmpty()) {
            builder.append(" WHERE ").append(filterClause);
        }

        if (isIncludeOrdering() && (orderByClause != null) && !orderByClause.trim().isEmpty()) {
            builder.append(" ORDER BY ").append(orderByClause);
        }

        // NB: offset & limit are Postgres specific
        if (hasOffset) {
            builder.append(" OFFSET ").append(offset);
        }

        if (hasLimit) {
            builder.append(" LIMIT ").append(limit);
        }
        return builder.toString();
    }

    private String buildFilterClause(DataCacheHelper dcHelper, IntegerRowsSelection limitBySelection, boolean ignoreBroadcast) {
        StringJoiner joiner = new StringJoiner(" AND ");
        if (tableDef.getFilterUuid() != null) {
            String filterClause = dcHelper.buildFilterClause(tableDef, dataview);
            if ((filterClause != null) && (filterClause.length() > 0)) {
                joiner.add(filterClause);
            }
        }

        if(!limitBySelection.isCleared())
        {
            String attachFilterClause = DataCacheHelper.buildTableSelectionFilterClause(limitBySelection, false, dataview);
            if (attachFilterClause.length() > 0) {
                joiner.add(attachFilterClause);
            }
        }

        if (!ignoreBroadcast) {
            BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(tableDef.getUuid());
            String attachFilterClause = DataCacheHelper.buildTableSelectionFilterClause(result.getBroadcastFilter(), result.isExcludeRows(), dataview);
            if (attachFilterClause.length() > 0) {
                joiner.add(attachFilterClause);
            }
        }
        return joiner.toString();
    }

    private String buildSelection(DataCacheHelper dcHelper) {
        String selection;
        if ((selectColumns == null) || selectColumns.isEmpty()) {
            selection = "*";

        } else /*if (!selectColumns.isEmpty())*/ {
            // build our local select list....
            selection = selectColumns.stream().collect(Collectors.joining(", "));
//TODO: unreachable
//        } else {
//            selection = dcHelper.buildSelectItems(dataview.getMeta().getModelDef(), tableDef.getTableViewSettings().getVisibleFields());
        }
        return selection;
    }

   private String buildTextSearchFilter(DataCacheHelper dcHelper, final String searchText) {
      Collection<String> columns;
      String usingSearchText = StringEscapeUtils.escapeSql(searchText);

      if (selectColumns == null) {
         columns = new ArrayList<String>();

         for (VisibleTableField vf : tableDef.getTableViewSettings().getVisibleFields()) {
            FieldDef myField = vf.getFieldDef(dataview.getMeta().getModelDef());
            FieldType ftype = myField.getFieldType();

            if (FieldType.STATIC == ftype) {
               columns.add("'" + myField.getStaticText() + "'");
               continue;
            }
            String quotedDbUuid = CacheUtil.getQuotedColumnName(myField);

            switch (myField.getDataType()) {
               case Time:
               case Date:
               case DateTime:
                  quotedDbUuid = "to_char(" + quotedDbUuid + ",'" + getDateFormat() + "')";
                  break;
               case Integer:
               case Number:
                  quotedDbUuid = "cast(" + quotedDbUuid + " AS text)";
               case String:
               case Boolean:
                  quotedDbUuid = "cast(" + quotedDbUuid + " AS text)";
               case Unsupported:
               default:
                  // No-op the other types
                  break;
            }
            columns.add(quotedDbUuid);
         }
      } else {
         columns = new ArrayList<String>(selectColumns);
      }
      return columns.stream()
                    .map(i -> new StringBuilder("(").append(i).append(" LIKE '%").append(usingSearchText).append("%')").toString())
                    .collect(Collectors.joining(" OR ", "(", ")"));
   }

    private String getDateFormat() {
        return dateFormat;
    }

    public void setSelectColumns(Collection<String> selectColumns) {
        this.selectColumns = new ArrayList<String>(selectColumns);
    }

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }

    public String buildSearchQuery(String searchText) {
        StringBuilder builder = new StringBuilder();

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());


        List<TableViewSortField> sortFields = new ArrayList<TableViewSortField>();
        if(clientSortFields != null){
            sortFields.addAll(clientSortFields);
        }
        List<TableViewSortField> tableSettingsSortFields = tableDef.getTableViewSettings().getSortFields();
        if(tableSettingsSortFields != null) {
            sortFields.addAll(tableSettingsSortFields);
        }

        String orderByClause = dcHelper.buildOrderByClause(dataview.getMeta().getModelDef(), sortFields);

        String filterClause = buildFilterClause(dcHelper, new IntegerRowsSelection(), false);

        builder.append(String.format(SELECT_QUERY, CacheUtil.INTERNAL_ID_NAME, CacheUtil.getQuotedCacheTableName(dataview.getUuid())));
        if ((filterClause != null) && !filterClause.isEmpty()) {
            builder.append(" WHERE ").append(filterClause);
            builder.append(" AND ");
            builder.append(buildTextSearchFilter(dcHelper, searchText));
        } else {
            builder.append(" WHERE ").append(buildTextSearchFilter(dcHelper, searchText));
        }



        if (isIncludeOrdering() && (orderByClause != null) && !orderByClause.trim().isEmpty()) {
            builder.append(" ORDER BY ").append(orderByClause);
        }

        // NB: offset & limit are Postgres specific
        //builder.append(" OFFSET ").append(offset);
        return builder.toString();
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String buildCopyQuery(List<FieldDef> fields) {
        StringBuilder builder = new StringBuilder();

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        String selection = buildCopySelect(dcHelper, fields);

        List<TableViewSortField> sortFields = new ArrayList<TableViewSortField>();
        if(clientSortFields != null){
            sortFields.addAll(clientSortFields);
        }
        List<TableViewSortField> tableSettingsSortFields = tableDef.getTableViewSettings().getSortFields();
        if(tableSettingsSortFields != null) {
            sortFields.addAll(tableSettingsSortFields);
        }

        String orderByClause = dcHelper.buildOrderByClause(dataview.getMeta().getModelDef(), sortFields);

        String filterClause = buildFilterClause(dcHelper, new IntegerRowsSelection(), false);

        builder.append(String.format(SELECT_QUERY, selection, CacheUtil.getQuotedCacheTableName(dataview.getUuid())));
        if ((filterClause != null) && !filterClause.isEmpty()) {
            builder.append(" WHERE ").append(filterClause);
        }

        if (isIncludeOrdering() && (orderByClause != null) && !orderByClause.trim().isEmpty()) {
            builder.append(" ORDER BY ").append(orderByClause);
        }

        // NB: offset & limit are Postgres specific
        if (hasOffset) {
            builder.append(" OFFSET ").append(offset);
        }

        if (hasLimit) {
            builder.append(" LIMIT ").append(limit);
        }
        return builder.toString();
    }

   private String buildCopySelect(DataCacheHelper dcHelper, List<FieldDef> fields) {
      String selection;

      if ((fields == null) || fields.isEmpty()) {
         selection = "*";
      } else {
         StringBuilder buf = new StringBuilder();

         buf.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));

         for (FieldDef field : fields) {
            FieldType ftype = field.getFieldType();

            if (FieldType.STATIC == ftype) {
               continue;
            }
            buf.append(',').append(CacheUtil.getQuotedColumnName(field));
         }
         //TODO: buf is not used
         selection = dcHelper.buildSelectItems(dataview.getMeta().getModelDef(),
                                               tableDef.getTableViewSettings().getVisibleFields());
      }
      return selection;
   }
}
