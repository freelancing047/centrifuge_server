package csi.server.common.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;

import csi.server.common.dto.CsiMap;

@Entity(name = "ModelProperty")
public class Property extends ModelObject implements InPlaceUpdate<Property> {

    int ordinal;

    private String name;

    @Column(length = 4096)
    private String value;

    public Property() {
        super();
    }

    public Property(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.toLowerCase().hashCode());
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
        final Property other = (Property) obj;
        if (name == null) {
            if (other.name != null) {
               return false;
            }
        } else if (!name.equalsIgnoreCase(other.name)) {
         return false;
      }
        return true;
    }

    public Property copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String, Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (Property) copyOfThis;
            }
        }
        Property copy = new Property();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.value = this.value;
        copy.uuid = this.uuid;
        return copy;
    }

    public Property copy() {
        Property copy = new Property();
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.value = this.value;
        //copy.uuid = this.uuid;
        return copy;
    }

    @Override
    public void updateInPlace(Property sourceIn) {

        sourceIn.cloneValues(this);
    }

    @Override
    public Property clone() {

        Property myClone = new Property();

        super.cloneComponents(myClone);

        cloneValues(myClone);

        return myClone;
    }

    public void cloneValues(Property cloneIn) {

        cloneIn.setName(getName());
        cloneIn.setValue(getValue());
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, value, indentIn, "value");
    }
}
