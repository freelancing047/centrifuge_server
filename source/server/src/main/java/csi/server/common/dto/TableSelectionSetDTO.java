package csi.server.common.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


//TODO: ensure unique names for tables
public class TableSelectionSetDTO implements IsSerializable {

    private CsiMap<String, TableSelectionItemDTO> selItemMap = new CsiMap<String, TableSelectionItemDTO>();
    
    private List<String> catalogs = new ArrayList<String>();
    
    private List<String> schemas  = new ArrayList<String>();
    
    private List<String> tableTypes  = new ArrayList<String>();
    
    public TableSelectionSetDTO(){
        
    }
    
    public void addTableDefDto(TableSelectionItemDTO dto) {
        addToMap(dto);
    }
    
    public void addTableDefDtos(List<TableSelectionItemDTO> dtoList){
        for(TableSelectionItemDTO dto: dtoList){
            addToMap(dto);
        }
    }
    
    private void addToMap(TableSelectionItemDTO tsiDto){
        String name = tsiDto.getName();
        selItemMap.put(name, tsiDto);
    }
    
    /**
     * Returns a filtered list of TableSelectionSetDtos matching the specified criteria.  
     * 
     */
    public CsiMap<String, TableSelectionItemDTO> getFilteredTableSelectionSets(boolean useCatalog, String catalog,
            boolean useSchema, String schema, boolean useType, String type) {
        
        CsiMap<String, TableSelectionItemDTO> matchingDtos = new CsiMap<String, TableSelectionItemDTO>();
        for (TableSelectionItemDTO dto : selItemMap.values()) {
            boolean catalogMatch = !useCatalog || dto.getCatalog().equals(catalog);
            boolean schemaMatch = !useSchema || dto.getSchema().equals(schema);
            boolean typeMatch = !useType || dto.getType().equals(type);
            if (catalogMatch && schemaMatch && typeMatch) {
                matchingDtos.put(dto.getName(),dto);
            }
        }

        return matchingDtos;
    }
    
    public CsiMap<String, TableSelectionItemDTO> getTableSelectionSets(){
        return selItemMap;
    }
    

    
    public List<String> getCatalogs() {
        return catalogs;
    }

    
    public List<String> getSchemas() {
        return schemas;
    }

    
    public List<String> getTableTypes() {
        return tableTypes;
    }

    
    public void setCatalogs(List<String> catalogs) {
        this.catalogs = catalogs;
    }

    
    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    
    public void setTableTypes(List<String> types) {
        this.tableTypes = types;
    }

}
