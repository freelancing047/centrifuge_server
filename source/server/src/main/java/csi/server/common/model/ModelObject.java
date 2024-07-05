package csi.server.common.model;

import java.util.List;
import java.util.Map;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.business.cachedb.json.JsonBinaryType;
import csi.server.business.cachedb.json.JsonStringType;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.util.Format;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.JOINED)
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public abstract class ModelObject implements IsSerializable {

    @EmbeddedId
    protected CsiUUID uuid;

    @Lob
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    protected CsiMap<String, String> clientProperties;

    @Transient
    boolean _entered = false;

    public ModelObject() {
        this(new CsiUUID());
    }

    public ModelObject(CsiUUID uuid) {
        this.uuid = uuid;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ModelObject> T cloneFromOrToMap(Map<String, T> mapIn, T itemIn) {

        T myClone = null;

        if (null != itemIn) {

            if (null != mapIn) {

                myClone = mapIn.get(itemIn.getUuid());

                if (null == myClone) {

                    myClone = (T) itemIn.clone();
                    mapIn.put(itemIn.getUuid(), myClone);
                }

            } else {

                myClone = (T) itemIn.clone();
            }
        }

        return myClone;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ModelObject, S extends ModelObject> T cloneFromOrToMap(Map<String, T> mapIn, T itemIn, Map<String, S> mapParmIn) {

        T myClone = null;

        if (null != itemIn) {

            if (null != mapIn) {

                myClone = mapIn.get(itemIn.getUuid());

                if (null == myClone) {

                    myClone = (T) itemIn.clone(mapParmIn);
                    mapIn.put(itemIn.getUuid(), myClone);
                }

            } else {

                myClone = (T) itemIn.clone(mapParmIn);
            }
        }

        return myClone;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ModelObject, S extends ModelObject, R extends ModelObject> T cloneFromOrToMap(Map<String, T> mapIn, T itemIn, Map<String, S> mapParmIn_1, Map<String, R> mapParmIn_2) {

        T myClone = null;

        if (null != itemIn) {

            if (null != mapIn) {

                myClone = mapIn.get(itemIn.getUuid());

                if (null == myClone) {

                    myClone = (T) itemIn.clone(mapParmIn_1, mapParmIn_2);
                    mapIn.put(itemIn.getUuid(), myClone);
                }

            } else {

                myClone = (T) itemIn.clone(mapParmIn_1, mapParmIn_2);
            }
        }

        return myClone;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ModelObject, S extends ModelObject, R extends ModelObject, Q extends ModelObject> T cloneFromOrToMap(Map<String, T> mapIn, T itemIn, Map<String, S> mapParmIn_1, Map<String, R> mapParmIn_2, Map<String, Q> mapParmIn_3) {

        T myClone = null;

        if (null != itemIn) {

            if (null != mapIn) {

                myClone = mapIn.get(itemIn.getUuid());

                if (null == myClone) {

                    myClone = (T) itemIn.clone(mapParmIn_1, mapParmIn_2, mapParmIn_3);
                    mapIn.put(itemIn.getUuid(), myClone);
                }

            } else {

                myClone = (T) itemIn.clone(mapParmIn_1, mapParmIn_2, mapParmIn_3);
            }
        }

        return myClone;
    }

    public void resetTransients() {

        _entered = false;
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#getUuid()
     */
    public String getUuid() {
        if (uuid == null) {
            uuid = new CsiUUID();
        }
        return uuid.getUuid();
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#setUuid(java.lang.String)
     */
    public void setUuid(String uuid) {
        if ((uuid == null) || uuid.isEmpty()) {
            this.uuid = null;
        } else {
            this.uuid = new CsiUUID(uuid);
        }
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#regenerateUuid()
     */
    public void regenerateUuid() {
        this.uuid = new CsiUUID();
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#OnPersist()
     */
    @PreUpdate
    @PrePersist
    public void OnPersist() {
        if ((uuid == null) || (uuid.getUuid() == null)) {
            uuid = new CsiUUID();
        }
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#getClientProperties()
     */
    public CsiMap<String, String> getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new CsiMap<String, String>();
        }
        return clientProperties;
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#setClientProperties(csi.server.common.dto.CsiMap)
     */
    public void setClientProperties(CsiMap<String, String> clientProperties) {
        if (clientProperties == null) {
            this.clientProperties = new CsiMap<String, String>();
        }
        this.clientProperties = clientProperties;
    }

    public void copyClientProperties(CsiMap<String, String> clientPropertiesIn) {
        if (null != clientPropertiesIn) {
            this.clientProperties = clientPropertiesIn.clone();
        } else {
            this.clientProperties = new CsiMap<String, String>();
        }
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see csi.server.common.model.ModelObjectInt#equals(java.lang.Object)
     */
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
        final ModelObject other = (ModelObject) obj;
        if (uuid == null) {
            return other.uuid == null;
        } else {
         return uuid.equals(other.uuid);
      }
    }

    public String debug(final String indentIn) {

        StringBuilder myBuffer = new StringBuilder();

        debug(myBuffer, indentIn);

        return myBuffer.toString();
    }

    public String string(final String indentIn, final String tagIn) {

        StringBuilder myBuffer = new StringBuilder();

        debug(myBuffer, indentIn, tagIn);

        return myBuffer.toString();
    }

    public String string(final String indentIn, final String tagIn, final String typeIn) {

        StringBuilder myBuffer = new StringBuilder();

        debug(myBuffer, indentIn, tagIn, typeIn);

        return myBuffer.toString();
    }

    public void debug(StringBuilder bufferIn, final String indentIn) {

        if (null != bufferIn) {

            logDebug(bufferIn, indentIn, ("my" + this.getClass().getSimpleName()), this.getClass().getName());
        }
    }

    public void debug(StringBuilder bufferIn, final String indentIn, final String tagIn) {

        if (null != bufferIn) {

            logDebug(bufferIn, indentIn, tagIn, this.getClass().getSimpleName());
        }
    }

    public void debug(StringBuilder bufferIn, final String indentIn, final String tagIn, final String typeIn) {

        if (null != bufferIn) {

            logDebug(bufferIn, indentIn, tagIn, typeIn);
        }
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public ModelObject fullClone() {

        return null;
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public <T extends ModelObject> ModelObject fullClone(Map<String, T> mapIn) {

        return null;
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public ModelObject clone() {

        return null;
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public <T extends ModelObject> ModelObject clone(Map<String, T> mapIn) {

        return null;
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public <T extends ModelObject, S extends ModelObject> ModelObject clone(Map<String, T> mapIn_1, Map<String, S> mapIn_2) {

        return null;
    }

    public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        return null;
    }

    //
    // SHOULD NEVER BE CALLED !!!!!!
    //
    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> ModelObject clone(Map<String, T> mapIn_1, Map<String, S> mapIn_2, Map<String, R> mapIn_3) {

        return null;
    }

    protected void cloneComponents(ModelObject cloneIn) {

        cloneIn.copyClientProperties(getClientProperties());
    }

    protected void copyComponents(ModelObject cloneIn) {

        cloneIn.copyClientProperties(getClientProperties());
    }

    protected void fullCloneComponents(ModelObject cloneIn) {

        cloneIn.setUuid(uuid.toString());
        cloneIn.setClientProperties(clientProperties);
    }

    protected <T extends ModelObject> void doDebug(T objectIn, StringBuilder bufferIn, final String indentIn) {

        if (null != bufferIn) {

            if (null != objectIn) {

                objectIn.debug(bufferIn, indentIn);

            } else {

                bufferIn.append(indentIn + "-- ????:<null>\n");
            }
        }
    }

    protected <T extends ModelObject> void doDebug(T objectIn, StringBuilder bufferIn, final String indentIn, final String tagIn) {

        if (null != bufferIn) {

            if (null != objectIn) {

                objectIn.debug(bufferIn, indentIn, tagIn);

            } else {

                bufferIn.append(indentIn + "-- " + tagIn + ":<null>\n");
            }
        }
    }

    protected <T extends ModelObject> void doDebug(T objectIn, StringBuilder bufferIn, final String indentIn, final String tagIn, final String typeIn) {

        if (null != bufferIn) {

            if (null != objectIn) {

                objectIn.debug(bufferIn, indentIn, tagIn, typeIn);

            } else {

                bufferIn.append(indentIn + "-- " + tagIn + ":<null>(" + typeIn + ")\n");
            }
        }
    }

    protected void logDebug(StringBuilder bufferIn, final String indentIn, final String tagIn, final String typeIn) {

        if (null != bufferIn) {

            bufferIn.append(indentIn + "-- " + tagIn + ":" + this.getUuid() + "(" + typeIn + ")\n");

            if (!_entered) {

                _entered = true;
                debugContents(bufferIn, nextIndent(indentIn));
                _entered = false;
            }
        }
    }

    protected <T extends ModelObject> void debugList(StringBuilder bufferIn, final List<T> listIn, final String indentIn, final String nameIn) {

        if (null != bufferIn) {

            if (null == listIn) {

                bufferIn.append(indentIn + "-- " + nameIn + ":<null>\n");

            } else if (!listIn.isEmpty()) {

                String myIndent = indentIn + "   ";
                int howMany = listIn.size();

                bufferIn.append(indentIn + "-- " + nameIn + ":non-empty list\n");

                for (int i = 0; i < howMany; i++) {

                    T myItem = listIn.get(i);

                    myItem.debug(bufferIn, myIndent, Format.value(i), myItem.getClass().getSimpleName());
                }

            } else {

                bufferIn.append(indentIn + "-- " + nameIn + ":empty list\n");
            }
        }
    }

    protected void debugObject(StringBuilder bufferIn, final List<Object> listIn, final String indentIn, final String nameIn) {

        if (null != bufferIn) {

            if (null == listIn) {

                bufferIn.append(indentIn + "-- " + nameIn + ":<null>\n");

            } else if (listIn instanceof List<?>) {

                if (!listIn.isEmpty()) {

                    String myIndent = indentIn + "   ";
                    int howMany = listIn.size();

                    bufferIn.append(indentIn + "-- " + nameIn + ":non-empty list\n");

                    for (int i = 0; i < howMany; i++) {

                        Object myItem = listIn.get(i);

                        debugObject(bufferIn, myItem, myIndent, Format.value(i));
                    }

                } else {

                    bufferIn.append(indentIn + "-- " + nameIn + ":empty list\n");
                }
            }
        }
    }

    protected void debugObject(StringBuilder bufferIn, final Object objectIn, final String indentIn, final String nameIn) {

        if (null != bufferIn) {

            if (objectIn instanceof ModelObject) {

            } else if (null != objectIn) {

                bufferIn.append(indentIn + "-- " + nameIn + ":" + Format.value(objectIn) + "\n");

            } else {

                bufferIn.append(indentIn + "-- " + nameIn + ":<null>\n");
            }
        }
    }

    protected String nextIndent(String indentIn) {

        return indentIn + "   ";
    }

    // Force error in all children without function in themselves or a super class
    //protected abstract void debugContents(StringBuilder bufferIn, String indentIn);
    // Provide a default for subclasses not being exploded
    protected void debugContents(StringBuilder bufferIn, String indentIn) {
    }
}
