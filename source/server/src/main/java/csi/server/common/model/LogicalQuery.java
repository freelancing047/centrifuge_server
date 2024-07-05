package csi.server.common.model;

public class LogicalQuery {

    public DataSourceDef source;
    public DataSetOp parentOp;
    public DataSetOp baseOp;
    public String referenceId;
    public String sqlText;
    public String preSql;
    public String postSql;
    public Integer keyField;
    public Integer rowLimit;

    /**
     * Some connectors (eg. Hive impala can't handle quotes)
     */
    /*
    public void stripQuotes(){
        postSql = postSql == null ? null: sqlText.replace("\"","");
        preSql = preSql == null ? null: preSql.replace("\"","");
        sqlText = sqlText == null ? null: sqlText.replace("\"","");
    }
    */
}
