package csi.server.common.model.linkup;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

/**
 * Created by centrifuge on 12/18/2014.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LooseMapping extends ModelObject {

    private String _mappedLocalId;      // DataView
    private String _mappedName;         // DataView
    private String _mappingLocalId;     // Template
    private String _mappingName;        // Template

    public LooseMapping() {

    }

    public LooseMapping(String mappedLocalIdIn, String mappedNameIn, String mappingLocalIdIn, String mappingNameIn) {

        _mappedLocalId = mappedLocalIdIn;
        _mappedName = mappedNameIn;
        _mappingLocalId = mappingLocalIdIn;
        _mappingName = mappingNameIn;
    }

    public void setMappedLocalId(String mappedLocalIdIn) {

        _mappedLocalId = mappedLocalIdIn;
    }

    public String getMappedLocalId() {

        return _mappedLocalId;
    }

    public void setMappedName(String mappedNameIn) {

        _mappedName = mappedNameIn;
    }

    public String getMappedName() {

        return _mappedName;
    }

    public void setMappingLocalId(String mappingLocalIdIn) {

        _mappingLocalId = mappingLocalIdIn;
    }

    public String getMappingLocalId() {

        return _mappingLocalId;
    }

    public void setMappingName(String mappingNameIn) {

        _mappingName = mappingNameIn;
    }
    public String getMappingName() {

        return _mappingName;
    }
}
