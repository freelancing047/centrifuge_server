package csi.server.common.model.visualization.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.attribute.AttributeDef;
import csi.shared.gwt.viz.graph.LinkDirection;

@Entity
@DynamicInsert
@DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LinkDef extends ModelObject {

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @Fetch(FetchMode.SELECT)
    private NodeDef nodeDef1;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @Fetch(FetchMode.SELECT)
    private NodeDef nodeDef2;

    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    private ConditionalExpression createConditional;

    @OneToOne(cascade = CascadeType.ALL)
    private ConditionalExpression hiddenConditional;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SELECT)
    private Set<AttributeDef> attributeDefs;

    @OneToOne(cascade = CascadeType.ALL)
    private DirectionDef directionDef;
    private boolean hideLabels;

    public LinkDef() {
        super();
    }

    public Set<AttributeDef> getAttributeDefs() {
        if (attributeDefs == null) {
            attributeDefs = new HashSet<AttributeDef>();
        }

        return attributeDefs;

    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs) {
        this.attributeDefs = attributeDefs;
    }

    public Map<String, FieldDef> getAttributeDefsAsMap() {
        Map<String, FieldDef> map = new HashMap<String, FieldDef>();
        for (AttributeDef adef : this.getAttributeDefs()) {
            map.put(adef.getName(), adef.getFieldDef());
        }

        return map;
    }

    public void addAttributeDef(AttributeDef def) {
        Set<AttributeDef> set = getAttributeDefs();
        if (set.contains(def)) {
            set.remove(def);
        }
        set.add(def);
    }

    public void removeAttributeDef(AttributeDef def) {
        Set<AttributeDef> set = getAttributeDefs();
        if (set.contains(def)) {
            set.remove(def);
        }
    }

    public AttributeDef getAttributeDef(String name) {
        for (AttributeDef def : getAttributeDefs()) {
            if (def.getName().equalsIgnoreCase(name)) {
                return def;
            }
        }

        return null;
    }

    public NodeDef getNodeDef1() {
        return nodeDef1;
    }

    public void setNodeDef1(NodeDef nodeDef1) {
        this.nodeDef1 = nodeDef1;
    }

    public DirectionDef getDirectionDef() {
        return directionDef;
    }

    public void setDirectionDef(DirectionDef directionDef) {
        this.directionDef = directionDef;
    }

    public NodeDef getNodeDef2() {
        return nodeDef2;
    }

    public void setNodeDef2(NodeDef nodeDef2) {
        this.nodeDef2 = nodeDef2;
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

    public void setName(String objectId) {
        name = objectId;
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

    public String getDisplayName() {

        if ((name == null) || (0 == name.trim().length())) {

            StringBuilder myBuffer = new StringBuilder();
            String myNodeOne = nodeDef1.getName();
            String myNodeTwo = nodeDef2.getName();
            FieldDef myField = (directionDef == null) ? null : directionDef.getFieldDef();

            if ((null != myNodeOne) && (0 < myNodeOne.trim().length())) {

                myBuffer.append(myNodeOne.trim());

            } else {

                myBuffer.append("???");
            }
            try {

                if ((null != myField) && (FieldType.STATIC == myField.getFieldType())) {

                    switch (LinkDirection.valueOf(myField.getStaticText())) {

                        case NONE:
                            myBuffer.append(" ---- ");
                            break;

                        case FORWARD:
                            myBuffer.append(" ---> ");
                            break;

                        case REVERSE:
                            myBuffer.append(" <--- ");
                            break;

                        case BOTH:
                            myBuffer.append(" <--> ");
                            break;

                        default:
                            myBuffer.append(" ---- ");
                            break;
                    }

                } else {

                    List<String> myReverseValues = (directionDef == null) ? Collections.emptyList() : directionDef.getReverseValues();
                    List<String> myForwardValues = (directionDef == null) ? Collections.emptyList() : directionDef.getForwardValues();

                    myBuffer.append(((myReverseValues != null) && !myReverseValues.isEmpty()) ? " +" : " -");
                    myBuffer.append("--");
                    myBuffer.append(((myForwardValues != null) && !myForwardValues.isEmpty()) ? "+ " : "- ");
                }

            } catch (Exception ignore) {

                myBuffer.append(" ---- ");
            }
            if ((null != myNodeTwo) && (0 < myNodeTwo.trim().length())) {

                myBuffer.append(myNodeTwo.trim());

            } else {

                myBuffer.append("???");
            }
            return myBuffer.toString();
        }
        return name;
    }

    public LinkDef copy(Map<String,Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (LinkDef) copyOfThis;
            }
        }
        LinkDef copy = new LinkDef();
        copies.put(getUuid(), copy);
        Set<AttributeDef> attributeDefsCopy = new HashSet<AttributeDef>();
        for (AttributeDef attributeDef : this.getAttributeDefs()) {
            attributeDefsCopy.add(attributeDef.copy(copies));
        }
        copy.setAttributeDefs(attributeDefsCopy);
        if (this.getCreateConditional() != null) {
            copy.setCreateConditional(this.getCreateConditional().copy(copies));
        }
        if (this.getDirectionDef() != null) {
            DirectionDef directionDef = this.getDirectionDef().copy(copies);
            copy.setDirectionDef(directionDef);
        }
        if (this.getHiddenConditional() != null) {
            copy.setHiddenConditional(this.getHiddenConditional().copy(copies));
        }
       	copy.setName(name);
        if (this.getNodeDef1() != null) {
            copy.setNodeDef1(this.getNodeDef1().copy(copies));
        }
        if (this.getNodeDef2() != null) {
            copy.setNodeDef2(this.getNodeDef2().copy(copies));
        }
        copy.hideLabels = hideLabels;
        copy.uuid = this.uuid;
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject, S extends ModelObject> LinkDef clone(Map<String, T> fieldMapIn, Map<String, S> nodeMapIn) {

        LinkDef myClone = new LinkDef();

        super.cloneComponents(myClone);

        myClone.setNodeDef1((NodeDef)cloneFromOrToMap(nodeMapIn, (S)getNodeDef1()));
        myClone.setNodeDef2((NodeDef)cloneFromOrToMap(nodeMapIn, (S)getNodeDef2()));
        myClone.setName(name);
        myClone.setCreateConditional(getCreateConditional());
        myClone.setHiddenConditional(getHiddenConditional());
        if (null != getDirectionDef()) {
            myClone.setDirectionDef(getDirectionDef().clone(fieldMapIn));
        }
        myClone.setAttributeDefs(cloneAttributeDefs(fieldMapIn));

        return myClone;
    }

    public <T extends ModelObject, S extends ModelObject> LinkDef trueCopy(Map<String, S> fieldMapIn, Map<String, S> nodeMapIn) {

        LinkDef myClone = new LinkDef();

        super.cloneComponents(myClone);

        myClone.setNodeDef1((NodeDef)cloneFromOrToMap(nodeMapIn, (S)getNodeDef1()));
        myClone.setNodeDef2((NodeDef)cloneFromOrToMap(nodeMapIn, (S)getNodeDef2()));
        myClone.setName(name);
        myClone.setCreateConditional(getCreateConditional());
        myClone.setHiddenConditional(getHiddenConditional());
        if (null != getDirectionDef()) {
            myClone.setDirectionDef(getDirectionDef().clone(fieldMapIn));
        }
        myClone.setAttributeDefs(copyAttributeDefs(nodeMapIn));

        return myClone;
    }

    private <T extends ModelObject> Set<AttributeDef> cloneAttributeDefs(Map<String, T> fieldMapIn) {

        if (null != getAttributeDefs()) {

            Set<AttributeDef>  myList = new HashSet<AttributeDef>();

            for (AttributeDef myItem : getAttributeDefs()) {

                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

	private <T extends ModelObject> Set<AttributeDef> copyAttributeDefs(Map<String, T> fieldMapIn) {

	        if (null != getAttributeDefs()) {

	            Set<AttributeDef>  myList = new HashSet<AttributeDef>();

	            for (AttributeDef myItem : getAttributeDefs()) {

	                myList.add(myItem.clone(fieldMapIn, true));
	            }

	            return myList;

	        } else {

	            return null;
	        }
	    }

    public boolean isHideLabels() {
        return hideLabels;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels = hideLabels;
    }
}
