package csi.server.common.model.functions;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.business.cachedb.script.IDataRow;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.DeepCopiable;
import csi.server.common.model.FieldDef;
import csi.server.common.model.InPlaceUpdate;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ScriptFunction extends ModelObject implements DeepCopiable<ScriptFunction>, InPlaceUpdate<ScriptFunction> {

    protected String name;
    protected int ordinal;

    public ScriptFunction() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String generateScript(FieldListAccess modelIn) {
        return null;
    }

    public Object execute(IDataRow rowSet, FieldDef resultField) {
        return null;
    }

    @Override
    public <T extends ModelObject> ScriptFunction clone(Map<String, T> fieldMapIn){
        if (fieldMapIn == null) {
           fieldMapIn = new HashMap<String,T>();
        }
        {
            ScriptFunction copyOfThis = (ScriptFunction) fieldMapIn.get(this.getUuid());
            if (copyOfThis != null) {
                return copyOfThis;
            }
        }
        ScriptFunction copy = new ScriptFunction();
        fieldMapIn.put(getUuid(), (T) copy);
        super.copyComponents(copy);
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        return copy;
    }

    @Override
    public ScriptFunction copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (ScriptFunction) copyOfThis;
            }
        }
        ScriptFunction copy = new ScriptFunction();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ScriptFunction fullClone() {

        ScriptFunction myClone = new ScriptFunction();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ScriptFunction clone() {

        ScriptFunction myClone = new ScriptFunction();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((ScriptFunction) cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((ScriptFunction) cloneIn);
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, ordinal, indentIn, "ordinal");
    }

    private void cloneContents(ScriptFunction cloneIn) {

        cloneIn.setName(getName());
    }

    public void updateInPlace(ScriptFunction sourceIn) {

        setName(sourceIn.getName());
    }
}
