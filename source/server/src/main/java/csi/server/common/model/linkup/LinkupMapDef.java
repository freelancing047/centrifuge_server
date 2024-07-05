package csi.server.common.model.linkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.ParamMapEntry;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LinkupMapDef extends ModelObject{

    private int ordinal;
    private String linkupName;
    private String templateUuid;
    private String templateName;
    private String templateOwner;

    private boolean prompt = true;
    private boolean editOk = false;
    private boolean returnAll = false;
    private boolean noNulls = true;
    private boolean monitor = true;
    private int useCount = 0;

    private String linkin = "";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	public List<LooseMapping> fieldsMap; // template --> DataView FieldDef mapping

    // Will be removed as part of linkupExtenders removal -- occupies slot zero
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    public List<ParamMapEntry> linkupParms;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    public List<LinkupExtender> linkupExtenders = new ArrayList<LinkupExtender>();

    @Transient
    Map<String, ParamMapEntry> _mappedParams = null;    // by DataView Field LocalId

    @Transient
    Map<String, LooseMapping> _mappedIdMap = null;      // by DataView Field LocalId

    @Transient
    Map<String, LooseMapping> _mappedNameMap = null;    // by DataView Field Name

    @Transient
    Map<String, LooseMapping> _mappingIdMap = null;     // by Template Field LocalId

    @Transient
    Map<String, LooseMapping> _mappingNameMap = null;   // by Template Field Name

    @Transient
    Map<String, String> _columnMap = null;

    @Transient
    List<String> _mappedIds = null;         // DataView Field LocalIds

    @Transient
    List<String> _mappedNames = null;       // DataView Field Names

    @Transient
    List<String> _mappingIds = null;        // Template Field LocalIds

    @Transient
    List<String> _mappingNames = null;      // Template Field Names

    public LinkupMapDef() {
    	super();
    }

    public void setOrdinal(int ordinalIn) { ordinal = ordinalIn; }

    public int getOrdinal() {
        return ordinal;
    }

    public String getLinkupName() {
        return linkupName;
    }

    public void setLinkupName(String linkupNameIn) {
        linkupName = linkupNameIn;
    }

	public String getTemplateUuid() {
		return templateUuid;
	}

	public void setTemplateUuid(String templateUuidIn) {
		templateUuid = templateUuidIn;
	}

	public List<LooseMapping> getFieldsMap() {
		return fieldsMap;
	}

	public void setFieldsMap(List<LooseMapping> fieldsMapIn) {
		fieldsMap = fieldsMapIn;
	}

    public List<ParamMapEntry> getLinkupParms() {
        return linkupParms;
    }

    public void setLinkupParms(List<ParamMapEntry> linkupParmsIn) {
        linkupParms = linkupParmsIn;
    }

    public List<LinkupExtender> getLinkupExtenders() {
        return linkupExtenders;
    }

    public void setLinkupExtenders(List<LinkupExtender> linkupExtendersIn) {
        if (linkupExtendersIn == null) {
            linkupExtenders.clear();
        }else {
            linkupExtenders = linkupExtendersIn;
        }
    }

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateNameIn) {
		templateName = templateNameIn;
	}

    public String getTemplateOwner() {
        return templateOwner;
    }

    public void setTemplateOwner(String templateOwnerIn) {
        templateOwner = templateOwnerIn;
    }

    public boolean getPrompt() {
        return prompt;
    }

    public void setPrompt(boolean promptIn) {
        prompt = promptIn;
    }

    public boolean getMonitor() {
        return monitor;
    }

    public void setMonitor(boolean monitorIn) {
        monitor = monitorIn;
    }

    public boolean getEditOk() {
        return editOk;
    }

    public void setEditOk(boolean editOkIn) {
        editOk = editOkIn;
    }

    public boolean getReturnAll() {
        return returnAll;
    }

    public void setReturnAll(boolean returnAllIn) {
        returnAll = returnAllIn;
    }

    public boolean getNoNulls() {

        return noNulls;
    }

    public void setNoNulls(boolean noNullsIn) {

        noNulls = noNullsIn;
    }

    public int getUseCount() {

        return useCount;
    }

    public void setUseCount(int useCountIn) {

        useCount = useCountIn;
    }

    public void incrementUseCount() {

        useCount++;
    }

    public boolean isInUse() {

        return (0 < useCount);
    }

    public void decrementUseCount() {

        useCount--;
    }

    public void resetUseCount() {

        useCount = 0;
    }

    public Map<String, String> getColumnMap() {

        if (null != _columnMap) {

            _columnMap = new TreeMap<String, String>();

            for (LooseMapping myMapping : fieldsMap) {

                _columnMap.put(myMapping.getMappedLocalId(), myMapping.getMappedLocalId());
            }
        }

        return _columnMap;
    }

    public boolean isIdRequired(String localIdIn) {

        return getMappedIdMap().containsKey(localIdIn) || getParameterIdMap().containsKey(localIdIn);
    }

    public Map<String, ParamMapEntry> getParameterIdMap() {

        if (null == _mappedParams) {

            _mappedParams = new HashMap<String, ParamMapEntry>();

            for (ParamMapEntry myParmeterDef : linkupParms) {

                String myLocalId = myParmeterDef.getFieldLocalId();

                if (null != myLocalId) {

                    _mappedParams.put(myLocalId, myParmeterDef);
                }
            }
            for (LinkupExtender myExtender : linkupExtenders) {

                List<ParamMapEntry> myParameters = myExtender.getParameterList();

                for (ParamMapEntry myParmeterDef : myParameters) {

                    String myLocalId = myParmeterDef.getFieldLocalId();

                    if (null != myLocalId) {

                        _mappedParams.put(myLocalId, myParmeterDef);
                    }
                }
            }
        }
        return _mappedParams;
    }

    public Map<String, LooseMapping> getMappedIdMap() {

        if (null == _mappedIdMap) {

            _mappedIdMap = new HashMap<String, LooseMapping>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappedIdMap.put(myMapping.getMappedLocalId(), myMapping);
            }
        }
        return _mappedIdMap;
    }

    public Map<String, LooseMapping> getMappedNameMap() {

        if (null == _mappedNameMap) {

            _mappedNameMap = new HashMap<String, LooseMapping>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappedNameMap.put(myMapping.getMappedName(), myMapping);
            }
        }
        return _mappedNameMap;
    }

    public Map<String, LooseMapping> getMappingIdMap() {

        if (null == _mappingIdMap) {

            _mappingIdMap = new HashMap<String, LooseMapping>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappingIdMap.put(myMapping.getMappingLocalId(), myMapping);
            }
        }
        return _mappingIdMap;
    }

    public Map<String, LooseMapping> getMappingNameMap() {

        if (null == _mappingNameMap) {

            _mappingNameMap = new HashMap<String, LooseMapping>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappingNameMap.put(myMapping.getMappingName(), myMapping);
            }
        }
        return _mappingNameMap;
    }

    public List<String> getMappedIds() {

        if (null == _mappedIds) {

            _mappedIds = new ArrayList<String>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappedIds.add(myMapping.getMappedLocalId());
            }
        }
        return _mappedIds;
    }

    public List<String> getMappedNames() {

        if (null == _mappedNames) {

            _mappedNames = new ArrayList<String>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappedNames.add(myMapping.getMappedName());
            }
        }
        return _mappedNames;
    }

    public List<String> getMappingIds() {

        if (null == _mappingIds) {

            _mappingIds = new ArrayList<String>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappingIds.add(myMapping.getMappedLocalId());
            }
        }
        return _mappingIds;
    }

    public List<String> getMappingNames() {

        if (null == _mappingNames) {

            _mappingNames = new ArrayList<String>();

            for (LooseMapping myMapping : fieldsMap) {

                _mappingNames.add(myMapping.getMappingName());
            }
        }
        return _mappingNames;
    }

    public LooseMapping getMappingByMappedLocalId(String localIdIn) {

        return getMappedIdMap().get(localIdIn);
    }

    public LooseMapping getMappingByMappedName(String nameIn) {

        return getMappedNameMap().get(nameIn);
    }

    public LooseMapping getMappingByMappingLocalId(String localIdIn) {

        return getMappingIdMap().get(localIdIn);
    }

    public LooseMapping getMappingByMappingName(String nameIn) {

        return getMappingNameMap().get(nameIn);
    }

   public boolean hasMappedFields() {
      return ((fieldsMap != null) && !fieldsMap.isEmpty());
   }

    public LinkupMapDef resetMaps() {

        _mappedIdMap = null;      // by DataView Field LocalId
        _mappedNameMap = null;    // by DataView Field Name
        _mappingIdMap = null;     // by Template Field LocalId
        _mappingNameMap = null;   // by Template Field Name
        _columnMap = null;
        _mappedIds = null;         // DataView Field LocalIds
        _mappedNames = null;       // DataView Field Names
        _mappingIds = null;        // Template Field LocalIds
        _mappingNames = null;      // Template Field Names

        return this;
    }

    @Override
	public String toString() {
	    return linkupName;
	}

    public String getLinkin() {
        return linkin;
    }

    public void setLinkin(String linkin) {
        this.linkin = linkin;
    }
}
