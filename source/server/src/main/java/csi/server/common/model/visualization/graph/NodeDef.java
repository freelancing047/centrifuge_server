package csi.server.common.model.visualization.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.DeepCopiable;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.attribute.AttributeDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NodeDef extends ModelObject implements DeepCopiable<NodeDef> {

    @Enumerated(EnumType.STRING)
    protected NodeDefType nodeDefType;

    protected String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    protected Set<AttributeDef> attributeDefs;

    protected boolean initiallyHidden;

    /**
     * if set to true, all nodes of this type will display their labels, otherwise not.
     * By default, the labels will be displayed for all node types.
     */
    protected Boolean hideLabels;

    @OneToOne(cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    protected ConditionalExpression createConditional;

    @OneToOne(cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    protected ConditionalExpression hiddenConditional;

    protected Boolean addPrefixId;

    public NodeDef() {
        super();
    }

    public boolean isInitiallyHidden() {
        return initiallyHidden;
    }

    public void setInitiallyHidden(boolean initiallyHidden) {
        this.initiallyHidden = initiallyHidden;
    }

    public boolean getHideLabels() {
        if (hideLabels == null) {
            hideLabels = Boolean.FALSE;
        }
        return hideLabels;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels = hideLabels;
    }

    public Set<AttributeDef> getAttributeDefs() {
        if (attributeDefs == null) {
            attributeDefs = Sets.newHashSet();
        }
        return attributeDefs;

    }

    public Map<String, FieldDef> getAttributeDefsAsMap() {
        Map<String, FieldDef> map = new HashMap<String, FieldDef>();
        for (AttributeDef adef : this.getAttributeDefs()) {
            map.put(adef.getName(), adef.getFieldDef());
        }

        return map;
    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs) {
        this.attributeDefs = attributeDefs;
    }

    public void removeAttributeDef(AttributeDef def) {
        Set<AttributeDef> set = getAttributeDefs();
        if (set.contains(def)) {
            set.remove(def);
        }
    }

    public void addAttributeDef(AttributeDef def) {
        Set<AttributeDef> set = getAttributeDefs();
        if (set.contains(def)) {
            set.remove(def);
        }
        set.add(def);
    }

    public AttributeDef getAttributeDef(String name) {
        return getAttributeDef(name, false);
    }

    public AttributeDef getAttributeDef(String name, boolean caseSensitive) {
        for (AttributeDef def : getAttributeDefs()) {
            if ((caseSensitive && def.getName().equals(name))
                    || (!caseSensitive && def.getName().equalsIgnoreCase(name))) {
                return def;
            }
        }

        return null;
    }

    public ConditionalExpression getCreateConditional() {
        return createConditional;
    }

    public void setCreateConditional(ConditionalExpression createConditional) {
        this.createConditional = createConditional;
    }

    public ConditionalExpression getHiddenConditional() {
        return hiddenConditional;
    }

    public void setHiddenConditional(ConditionalExpression hiddenConditional) {
        this.hiddenConditional = hiddenConditional;
    }

    // TODO: eventually we'll need real names
    // but for now if it doesn't have a name
    // then return the uuid. It will only have
    // a name if it was imported
    public String getName() {
        if (name == null) {
            return getUuid();
        }

        return name;
    }

    public void setName(String _name) {
        name = _name;
    }

    public boolean isAddPrefixId() {
        if (addPrefixId == null) {
            addPrefixId = Boolean.FALSE;
        }
        return addPrefixId;
    }

    public void setAddPrefixId(boolean addPrefixId) {
        this.addPrefixId = addPrefixId;
    }

    public NodeDefType getNodeDefType() {
        return nodeDefType;
    }

    public void setNodeDefType(NodeDefType nodeDefType) {
        this.nodeDefType = nodeDefType;
    }

    public NodeDef copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (NodeDef) copyOfThis;
            }
        }
        NodeDef copy = new NodeDef();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.setAddPrefixId(this.isAddPrefixId());

        copy.attributeDefs = Sets.newHashSet();
        for (AttributeDef attributeDef : attributeDefs) {
            copy.attributeDefs.add(attributeDef.copy(copies));
        }
        if (this.getCreateConditional() != null) {
            copy.setCreateConditional(this.getCreateConditional().copy(copies));
        }
        if (this.getHiddenConditional() != null) {
            copy.setHiddenConditional(this.getHiddenConditional().copy(copies));
        }
        copy.setHideLabels(this.getHideLabels());
        copy.setInitiallyHidden(this.isInitiallyHidden());
        copy.setName(this.getName());
        copy.setNodeDefType(this.getNodeDefType());
        copy.uuid = this.uuid;
        return copy;
    }

    @Override
    public <T extends ModelObject> NodeDef clone(Map<String, T> fieldMapIn) {
        
        NodeDef myClone = new NodeDef();
        
        super.cloneComponents(myClone);

        myClone.setNodeDefType(getNodeDefType());
        myClone.setName(getName());
        myClone.setInitiallyHidden(isInitiallyHidden());
        myClone.setHideLabels(getHideLabels());
        myClone.setCreateConditional(getCreateConditional());
        myClone.setHiddenConditional(getHiddenConditional());
        myClone.setAddPrefixId(isAddPrefixId());
        myClone.setAttributeDefs(cloneAttributeDefs(fieldMapIn, false));

        return myClone;
    }
    
    public <T extends ModelObject> NodeDef trueCopy(Map<String, T> map) {
        

        NodeDef copy = new NodeDef();
        super.cloneComponents(copy);

        map.put(getUuid(), (T) copy);
        copy.setAddPrefixId(isAddPrefixId());

        copy.attributeDefs = Sets.newHashSet();
        copy.setAttributeDefs(cloneAttributeDefs(map, true));
        if (this.getCreateConditional() != null) {
            copy.setCreateConditional(getCreateConditional().trueCopy(map));
        }
        if (this.getHiddenConditional() != null) {
            copy.setHiddenConditional(getHiddenConditional().trueCopy(map));
        }
        copy.setHideLabels(getHideLabels());
        copy.setInitiallyHidden(isInitiallyHidden());
        copy.setName(getName());
        copy.setNodeDefType(getNodeDefType());
        return copy;
    }
        
    private <T extends ModelObject> Set<AttributeDef> cloneAttributeDefs(Map<String, T> fieldMapIn, boolean isCopy) {
        
        if (null != getAttributeDefs()) {
            
            Set<AttributeDef>  myList = Sets.newHashSet();
            
            for (AttributeDef myItem : getAttributeDefs()) {
                AttributeDef copy = null;
                copy = (AttributeDef) fieldMapIn.get(myItem.getUuid());
                
                if(copy == null){
                    copy = myItem.clone(fieldMapIn, isCopy);
                    fieldMapIn.put(myItem.getUuid(), (T) copy);
                }
                myList.add(copy);
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }

}