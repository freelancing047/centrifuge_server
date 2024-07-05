package csi.server.business.visualization.graph.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.spi.SelectSQLSpi;

public class GraphQueryBuilder extends AbstractQueryBuilder<RelGraphViewDef> {
    private static final Logger LOG = LogManager.getLogger(GraphQueryBuilder.class);
    
    public SelectSQL getQuery(){

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        Set<FieldDef> fieldsToQuery = new HashSet<FieldDef>();
        
        searchBundlesForFields(getViewDef().getBundleDefs(), fieldsToQuery);
        searchLinkDefsForFields(getViewDef().getLinkDefs(), fieldsToQuery);
        searchNodeDefsForFields(getViewDef().getNodeDefs(), fieldsToQuery);
        searchPlayerSettingsForFields(getViewDef().getPlayerSettings(), fieldsToQuery);
        
        fieldsToQuery = validateFields(fieldsToQuery);
        
        for(FieldDef fieldDef: fieldsToQuery){
            Column column = tableSource.getColumn(fieldDef);
            column.setAliasEnabled(false);
            sql.select(column);
        }
        
        Column idColumn = tableSource.getRawIdColumn();
        idColumn.setAlias(INTERNAL_ID_COLUMN_NAME);
        sql.select(idColumn);
        
        // Filter
        applyFilters(tableSource,sql,false);

        if(LOG.isDebugEnabled()){
            String sqlString = ((SelectSQLSpi) sql).getSQL(); 
            LOG.debug(sqlString);
        }
        
        return sql;
    }

    private Set<FieldDef> validateFields(Set<FieldDef> fieldsToQuery) {
        Set<FieldDef> validatedFields = new HashSet<FieldDef>();
        for(FieldDef dataViewField: getDataView().getMeta().getModelDef().getFieldDefs()){
            if(fieldsToQuery.contains(dataViewField) && dataViewField.getName() != null){
                validatedFields.add(dataViewField);
            }
        }
        
        return validatedFields;
    }

    private void searchPlayerSettingsForFields(GraphPlayerSettings playerSettings, Set<FieldDef> fields) {
        if(playerSettings != null){
            fields.add(playerSettings.getEndField());
            fields.add(playerSettings.getStartField());
        }
        
    }

    private void searchNodeDefsForFields(List<NodeDef> nodeDefs, Set<FieldDef> fields) {
        for(NodeDef nodeDef: nodeDefs){
            searchAttributeDefsForFields(nodeDef.getAttributeDefs(), fields);
        }
    }

    private void searchAttributeDefsForFields(Set<AttributeDef> attributeDefs, Set<FieldDef> fields) {
        for(AttributeDef attributeDef: attributeDefs){
            fields.add(attributeDef.getFieldDef());
            fields.add(attributeDef.getTooltipLinkFeildDef());
        }
        
    }

    private void searchLinkDefsForFields(List<LinkDef> linkDefs, Set<FieldDef> fields) {
        for(LinkDef linkDef: linkDefs){
            searchAttributeDefsForFields(linkDef.getAttributeDefs(), fields);
            DirectionDef directionDef = linkDef.getDirectionDef();
            if(directionDef != null){
                fields.add(directionDef.getFieldDef());
            }
        }
        
    }

    private void searchBundlesForFields(List<BundleDef> bundleDefs, Set<FieldDef> fields) {
        for(BundleDef bundle: bundleDefs){
            for(BundleOp operation: bundle.getOperations()){
                fields.add(operation.getField());
            }
        }
    }
    
    

}
