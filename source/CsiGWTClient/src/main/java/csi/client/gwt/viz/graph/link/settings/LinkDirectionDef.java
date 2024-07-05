package csi.client.gwt.viz.graph.link.settings;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.shared.gwt.viz.graph.LinkDirection;

public class LinkDirectionDef {

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public ArrayList<String> getForwardValues() {
        return forwardValues;
    }

    public void setForwardValues(ArrayList<String> forwardValues) {
        this.forwardValues = forwardValues;
    }

    public ArrayList<String> getReverseValues() {
        return reverseValues;
    }

    public void setReverseValues(ArrayList<String> reverseValues) {
        this.reverseValues = reverseValues;
    }

    public LinkDirection getLinkDirection() {
        return linkDirection;
    }

    public void setLinkDirection(LinkDirection linkDirection) {
        this.linkDirection = linkDirection;
    }

    private FieldDef fieldDef;
    private ArrayList<String> forwardValues = Lists.newArrayList();
    private ArrayList<String> reverseValues = Lists.newArrayList();
    private LinkDirection linkDirection = LinkDirection.NONE;
    private String node1Name;
    private String node2Name;

    protected LinkDirectionDef(LinkProxy linkProxy) {
        node1Name = linkProxy.getNode1().getName();
        node2Name = linkProxy.getNode2().getName();
        DirectionDef def = linkProxy.getDirectionDef();
        if (def == null) {
            return;

        }
        fieldDef = def.getFieldDef();
        if (fieldDef == null) {
            return;
        }
        if (fieldDef.getFieldType() == FieldType.STATIC) {
            String staticText = fieldDef.getStaticText();
            linkDirection = LinkDirection.valueOf(staticText);
        } else {
            linkDirection = LinkDirection.DYNAMIC;
            forwardValues.addAll(def.getForwardValues());
            reverseValues.addAll(def.getReverseValues());
        }
    }

    public void persist(LinkProxy linkProxy) {
        DirectionDef directionDef = linkProxy.getDirectionDef();
        if (directionDef == null) {
            directionDef = new DirectionDef();
            linkProxy.setDirectionDef(directionDef);
        }
        switch (linkDirection) {
            case DYNAMIC:
                directionDef.setForwardValues(forwardValues);
                directionDef.setReverseValues(reverseValues);
                break;
            case FORWARD:
                fieldDef = new FieldDef();
                fieldDef.setFieldType(FieldType.STATIC);
                fieldDef.setStaticText(LinkDirection.FORWARD.toString());
                break;
            case REVERSE:
                fieldDef = new FieldDef();
                fieldDef.setFieldType(FieldType.STATIC);
                fieldDef.setStaticText(LinkDirection.REVERSE.toString());
                break;
            default:
                linkProxy.setDirectionDef(null);
                return;
        }
        directionDef.setFieldDef(fieldDef);
    }

    public String getNode1Name() {
        return node1Name;
    }

    public String getNode2Name() {
        return node2Name;
    }
}
