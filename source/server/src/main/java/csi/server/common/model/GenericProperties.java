package csi.server.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;
import csi.server.common.util.Update;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GenericProperties extends ModelObject implements IsSerializable, InPlaceUpdate<GenericProperties> {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private List<Property> properties;

    @Transient
    Map<String, String> _propertyMap = null;

    public GenericProperties() {
        super();
    }

    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<Property>();
        }
        return properties;
    }

    public void add(String nameIn, String valueIn) {

        getProperties().add(new Property(nameIn, valueIn));
        if (null != _propertyMap) {

            _propertyMap.put(nameIn, valueIn);
        }
    }

    public String get(String nameIn) {

        return ensureMap().get(nameIn);
    }

    public void setProperties(List<Property> connDefProperties) {
        this.properties = connDefProperties;
    }

    //TODO: would like to make this method just { retrun Maps.fromProperties(properties);}
    //TODO: Need to update downstream to expect immutableMap
    public Map<String, String> getPropertiesMap() {

        _propertyMap = null;
        return ensureMap();
   }

    //This is an odd way of flushing values. we should try not to use it.
    public void refreshProperties() {

        getProperties();
        if (null != _propertyMap) {

            List<Property> myDiscards = new ArrayList<Property>();

            for (Property myProperty : properties) {

                String myValue = _propertyMap.get(myProperty.getName());

                if (null != myValue) {

                    myProperty.setValue(myValue);
                    _propertyMap.remove(myProperty.getName()) ;

                } else {

                    myDiscards.add(myProperty);
                }
            }
            for (Map.Entry<String, String> myPair : _propertyMap.entrySet()) {

                properties.add(new Property(myPair.getKey(), myPair.getValue()));
            }
            for (Property myDiscard : myDiscards) {

                properties.remove(myDiscard);
            }

        } else {

            properties.clear();
        }
    }

    public GenericProperties copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String, Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (GenericProperties) copyOfThis;
            }
        }

        GenericProperties copy = new GenericProperties();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        List<Property> popertiesCopy = new ArrayList<Property>();
        for (Property property : this.getProperties()) {
            popertiesCopy.add(property.copy(copies));
        }
        copy.setProperties(popertiesCopy);

        copy.uuid = this.uuid;
        return copy;
    }

    @Override
    public void updateInPlace(GenericProperties sourceIn) {

        setProperties(Update.createOrUpdateList(getProperties(), sourceIn.getProperties(), null));
        _propertyMap = null;
    }

    @Override
    public GenericProperties clone() {

        GenericProperties myClone = new GenericProperties();

        super.cloneComponents(myClone);

        myClone.setProperties(cloneProperties());

        return myClone;
    }

    public GenericProperties copy() {

        GenericProperties myCopy = new GenericProperties();

        super.copyComponents(myCopy);

        myCopy.setProperties(copyProperties());

        return myCopy;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugList(bufferIn, properties, indentIn, "properties");
    }

    private List<Property> cloneProperties() {

        if (null != properties) {

            List<Property>  myList = new ArrayList<Property>();

            for (Property myItem : properties) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<Property> copyProperties() {

        if (null != properties) {

            List<Property>  myList = new ArrayList<Property>();

            for (Property myItem : properties) {

                myList.add(myItem.copy());
            }

            return myList;

        } else {

            return null;
        }
    }

    private Map<String, String> ensureMap() {

        if (null == _propertyMap) {

            _propertyMap = Maps.newHashMapWithExpectedSize(getProperties().size());
            //NOTE:issues with concurrent modification if I do not make a defensive copy
            for (Property myProperty : new ArrayList<Property>(getProperties())) {
                _propertyMap.put(myProperty.getName(), myProperty.getValue());
            }
        }
        return _propertyMap;
    }
}
