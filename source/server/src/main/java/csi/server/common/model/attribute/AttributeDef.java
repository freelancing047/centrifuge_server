package csi.server.common.model.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import csi.client.gwt.viz.graph.node.settings.tooltip.AnchorLinkType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.DeepCopiable;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.listener.AttributeDefListener;

@Entity
@EntityListeners(AttributeDefListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AttributeDef extends ModelObject implements DeepCopiable<AttributeDef>, Comparable{

    public static List<String> nonToolTips = new ArrayList<String>();
    static {
        nonToolTips.add("csi.postProcess");
        nonToolTips.add("csi.createIf");
        nonToolTips.add("csi.initiallyHiddenIf");
    }

    protected String name;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @Fetch(FetchMode.SELECT)
    protected FieldDef fieldDef;

    // track the referenced attribute
    protected String referenceName;
    protected AttributeKind kind;
    protected boolean bySize = false;
    protected boolean byStatic = true;
    protected AttributeAggregateType aggregateFunction;
    protected Boolean includeInTooltip;
    protected Boolean hideEmptyInTooltip = Boolean.FALSE;
    // FIXME: I want to be unboxed. but I don't have time
    protected Integer tooltipOrdinal = 0;

    private AnchorLinkType tooltipLinkType;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @Fetch(FetchMode.SELECT)
    private FieldDef tooltipLinkFeildDef;

    private String tooltipLinkText;
    private String fieldDefId = null;
    private String tooltipLinkFieldDefId = null;

    public int getTooltipOrdinal() {
        if (tooltipOrdinal == null) {
         tooltipOrdinal = Integer.valueOf(0);
      }
        return tooltipOrdinal;
    }

    public void setTooltipOrdinal(int tooltipOrdinal) {
        this.tooltipOrdinal = tooltipOrdinal;
    }

    public AttributeDef() {
        super();
    }

    public AttributeDef(String name, FieldDef fieldDef) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("AttributeDef cannot have null name");
        }

        if (fieldDef == null) {
            throw new IllegalArgumentException("AttributeDef cannot have null field definition");
        }
        this.name = name;
        this.fieldDef = fieldDef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public AttributeKind getKind() {
        return kind;
    }

    public void setKind(AttributeKind kind) {
        this.kind = kind;
    }

    public AttributeAggregateType getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(AttributeAggregateType aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    public boolean isIncludeInTooltip() {
        if (nonToolTips.contains(name)) {
            return false;
        }

        return (includeInTooltip == null) ? getDefaultIncludeInTooltip() : includeInTooltip;
    }

    public void setIncludeInTooltip(boolean value) {
        this.includeInTooltip = Boolean.valueOf(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : getUuid().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
         return true;
      }
        if (obj == null) {
         return false;
      }
        if (getClass() != obj.getClass()) {
         return false;
      }
        final AttributeDef other = (AttributeDef) obj;
        if (getUuid() == null) {
            if (other.getUuid() != null){
                return false;
            }
        } else if (!getUuid().equalsIgnoreCase(other.getUuid())){
            return false;
        }

//        if (name == null) {
//            if (other.name != null)
//                return false;
//        } else if (!name.equalsIgnoreCase(other.name))
//            return false;
//        else if (kind != other.kind)
//            return false;
//        else if (referenceName == null && other.referenceName != null) {
//            return false;
//        } else if (referenceName != null && other.referenceName == null) {
//            return false;
//        } else if (isIncludeInTooltip() != other.isIncludeInTooltip()) {
//            return false;
//        }
        return true;
    }

    @PrePersist
    @PreUpdate
    protected void validateData() {
        validateIncludeInTooltip();
    }

    private void validateIncludeInTooltip() {
        if (nonToolTips.contains(name)) {
            includeInTooltip = Boolean.FALSE;
        }

        if (includeInTooltip == null) {
            includeInTooltip = getDefaultIncludeInTooltip();
        }
    }

    private Boolean getDefaultIncludeInTooltip() {
        return Boolean.TRUE;
    }

    public boolean isHideEmptyInTooltip() {
        if (hideEmptyInTooltip == null) {
            hideEmptyInTooltip = getDefaultIncludeInTooltip();
        }
        return hideEmptyInTooltip;
    }

    public void setHideEmptyInTooltip(boolean hideEmptyInTooltip) {
        this.hideEmptyInTooltip = hideEmptyInTooltip;
    }

    public boolean getBySize() {
        return bySize;
    }

    public void setBySize(boolean bySize) {
        this.bySize = bySize;
    }

    public boolean getByStatic() {
        return byStatic;
    }

    public void setByStatic(boolean byStatic) {
        this.byStatic = byStatic;
    }

    public void setTooltipLinkType(AnchorLinkType linkType) {
        tooltipLinkType = linkType;
    }

    public void setTooltipLinkFeildDef(FieldDef linkFeildDef) {
        this.tooltipLinkFeildDef = linkFeildDef;
    }

    public void setTooltipLinkText(String linkText) {
        this.tooltipLinkText = linkText;
    }

    public FieldDef getTooltipLinkFeildDef() {
        return tooltipLinkFeildDef;
    }

    public String getTooltipLinkText() {
        return tooltipLinkText;
    }

    public AnchorLinkType getTooltipLinkType() {
        return tooltipLinkType;
    }

    public AttributeDef copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String, Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (AttributeDef) copyOfThis;
            }
        }
        AttributeDef copy = new AttributeDef();
        copies.put(getUuid(), copy);
        copy.setAggregateFunction(this.getAggregateFunction());
        copy.setBySize(this.getBySize());
        copy.setByStatic(this.getByStatic());
        copy.getClientProperties().putAll(this.getClientProperties());
        if (this.getFieldDef() != null) {
            if((getFieldDef().getFieldType() != FieldType.COLUMN_REF)
                    && (getFieldDef().getFieldType() != FieldType.LINKUP_REF)
                    && (getFieldDef().getFieldType() != FieldType.SCRIPTED)
                    && (getFieldDef().getFieldType() != FieldType.DERIVED)) {
               copy.setFieldDef(this.getFieldDef().copy(copies));
            } else {
               copy.setFieldDef(getFieldDef());
            }
        }
        copy.setHideEmptyInTooltip(this.isHideEmptyInTooltip());
        copy.setIncludeInTooltip(this.isIncludeInTooltip());
        copy.setKind(this.getKind());
        copy.setName(this.getName());
        copy.setReferenceName(this.getReferenceName());
        if (this.getTooltipLinkFeildDef() != null) {
            copy.setTooltipLinkFeildDef(this.getTooltipLinkFeildDef().copy(copies));
        }
        copy.setTooltipLinkText(this.getTooltipLinkText());
        copy.setTooltipLinkType(this.getTooltipLinkType());
        copy.setTooltipOrdinal(this.getTooltipOrdinal());
        copy.uuid = this.uuid;
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject> AttributeDef clone(Map<String, T> fieldMapIn) {

        AttributeDef myClone = new AttributeDef();

        super.cloneComponents(myClone);

        myClone.setName(getName());
        myClone.setReferenceName(getReferenceName());
        myClone.setKind(getKind());
        myClone.setBySize(getBySize());
        myClone.setByStatic(getByStatic());
        myClone.setAggregateFunction(getAggregateFunction());
        myClone.setIncludeInTooltip(isIncludeInTooltip());
        myClone.setHideEmptyInTooltip(isHideEmptyInTooltip());
        myClone.setTooltipOrdinal(getTooltipOrdinal());
        myClone.setTooltipLinkType(getTooltipLinkType());
        myClone.setTooltipLinkText(getTooltipLinkText());
        myClone.setFieldDef((FieldDef)cloneFromOrToMap(fieldMapIn, (T)getFieldDef(), fieldMapIn));
        myClone.setTooltipLinkFeildDef((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getTooltipLinkFeildDef(), fieldMapIn));

        return myClone;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelObject> AttributeDef clone(Map<String, T> fieldMapIn, boolean isCopy) {

        AttributeDef myClone = new AttributeDef();

        super.cloneComponents(myClone);

        myClone.setName(getName());
        myClone.setReferenceName(getReferenceName());
        myClone.setKind(getKind());
        myClone.setBySize(getBySize());
        myClone.setByStatic(getByStatic());
        myClone.setAggregateFunction(getAggregateFunction());
        myClone.setIncludeInTooltip(isIncludeInTooltip());
        myClone.setHideEmptyInTooltip(isHideEmptyInTooltip());
        myClone.setTooltipOrdinal(getTooltipOrdinal());
        myClone.setTooltipLinkType(getTooltipLinkType());
        myClone.setTooltipLinkText(getTooltipLinkText());

        if(isCopy){
            if((getFieldDef() != null) && (getFieldDef().getFieldType() != FieldType.COLUMN_REF)
                    && (getFieldDef().getFieldType() != FieldType.LINKUP_REF)
                    && (getFieldDef().getFieldType() != FieldType.SCRIPTED)
                    && (getFieldDef().getFieldType() != FieldType.DERIVED)){
                //Name being null means synthetic fielddef, make new
                myClone.setFieldDef(getFieldDef().clone(fieldMapIn));
            } else {
                myClone.setFieldDef(getFieldDef());
            }

            if((myClone.getTooltipLinkFeildDef() != null) && (getTooltipLinkFeildDef().getFieldType() != FieldType.COLUMN_REF)
                    && (getTooltipLinkFeildDef().getFieldType() != FieldType.LINKUP_REF)
                    && (getTooltipLinkFeildDef().getFieldType() != FieldType.SCRIPTED)
                    && (getTooltipLinkFeildDef().getFieldType() != FieldType.DERIVED)){

                myClone.setTooltipLinkFeildDef(getTooltipLinkFeildDef().clone(fieldMapIn));
            } else {
                myClone.setTooltipLinkFeildDef(getTooltipLinkFeildDef());
            }
        } else {

        	myClone.setFieldDef((FieldDef)cloneFromOrToMap(fieldMapIn, (T)getFieldDef(), fieldMapIn));
            myClone.setTooltipLinkFeildDef((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getTooltipLinkFeildDef(), fieldMapIn));
        }



        return myClone;
    }

    public void setFieldDefId(String uuidIn) {

        fieldDefId = uuidIn;
    }

    public String getFieldDefId() {

        return fieldDefId;
    }

    public void setTooltipLinkFieldDefId(String uuidIn) {

        tooltipLinkFieldDefId = uuidIn;
    }

    public String getTooltipLinkFieldDefId() {

        return tooltipLinkFieldDefId;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof AttributeDef){
            AttributeDef def = (AttributeDef) o;
            return def.getUuid().compareTo(getUuid());
        }
        return -1;
    }
}
