package csi.client.gwt.viz.graph.node.settings;


import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;

public class NodeIdentity {

    private FieldDef fieldDef;
    private String text;
    private boolean fixed;

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void persist(NodeProxy nodeProxy) {
        AttributeDef identityAttributeDef = nodeProxy.getIdentityAttributeDef();
        FieldDef f;
        if(isFixed()){
            f = new FieldDef();
            f.setFieldType(FieldType.STATIC);
            f.setStaticText(getText());
            f.setFieldName(null);
        }
        else {
            f = getFieldDef();
        }
        identityAttributeDef.setFieldDef(f);
    }

    public NodeIdentity(final NodeProxy nodeProxy) {
        AttributeDef identityAttributeDef = nodeProxy.getIdentityAttributeDef();
        FieldDef _fieldDef = identityAttributeDef.getFieldDef();
        if(_fieldDef!=null){
            setFixed(FieldType.STATIC.equals(_fieldDef.getFieldType()));
            setText(_fieldDef.getStaticText());
            setFieldDef(_fieldDef);
        }
    }
}
