package csi.server.common.model;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.AclResourceType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ConnectionDef extends Resource implements IsSerializable, InPlaceUpdate<ConnectionDef> {

    protected String type;

    @Column(length = 4096)
    protected String connectString;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected GenericProperties properties;

    @Column(columnDefinition = "TEXT")
    protected String preSql;

    @Column(columnDefinition = "TEXT")
    protected String postSql;
    
    @Transient
    String username = null;
    
    @Transient
    String password = null;

    public ConnectionDef(String typeIn, String connectStringIn, GenericProperties propertiesIn) {
        super(AclResourceType.CONNECTION);
        type = typeIn;
        connectString = connectStringIn;
        properties = propertiesIn;
    }

    public ConnectionDef() {
        super(AclResourceType.CONNECTION);
    }

    public ConnectionDef redactAll() {

        ConnectionDef myClone = new ConnectionDef();

        super.cloneComponents(myClone);

        myClone.setType(getType());

        return myClone;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String value) {
        this.connectString = value;
    }

    public void setProperties(GenericProperties value) {
        this.properties = value;
    }

    public GenericProperties getProperties() {
        return this.properties;
    }

    public String getPreSql() {
        return preSql;
    }

    public void setPreSql(String preSql) {
        this.preSql = preSql;
    }

    public String getPostSql() {
        return postSql;
    }

    public void setPostSql(String postSql) {
        this.postSql = postSql;
    }

    public void clearRuntimeValues() {
        if (properties != null) {
            Iterator<Property> iterator = properties.getProperties().iterator();
            while (iterator.hasNext()) {
                Property prop = iterator.next();
                if (prop.getName().startsWith("csi.runtime.")) {
                    iterator.remove();
                }
            }
        }
    }

    public void addCredentials(String usernameIn, String passwordIn) {
        
        username = (null != usernameIn) ? usernameIn.trim() : null;
        password = (null != passwordIn) ? passwordIn.trim() : null;
    }
    
    public String getUsername() {
        
        return username;
    }
    
    public String getPassword() {
        
        return password;
    }

    public Set<String> getDataSourceKeySet() {

        Set<String> mySet = new TreeSet<String>();

        if (null != type) {

            mySet.add(type);
        }
        return mySet;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("type", getType()) //
                .add("connectString", getConnectString()) //
                .add("preSql", getPreSql()) //
                .add("postSql", getPostSql()) //
                .toString();

    }

    public void updateInPlace(ConnectionDef sourceIn) {

        super.updateInPlace(sourceIn);
        sourceIn.cloneValues(this);
        getProperties().updateInPlace(sourceIn.getProperties());
    }

    public ConnectionDef cloneValues(ConnectionDef cloneIn) {

        cloneIn.setType(getType());
        cloneIn.setConnectString(getConnectString());
        cloneIn.setPreSql(getPreSql());
        cloneIn.setPostSql(getPostSql());

        return cloneIn;
    }

    public ConnectionDef fullClone() {

        ConnectionDef myClone = new ConnectionDef();

        super.fullCloneComponents(myClone);
        if (null != getProperties()) {
            myClone.setProperties(getProperties().clone());
        }
        return cloneValues(myClone);
    }

    @Override
    public ConnectionDef clone() {
        
        ConnectionDef myClone = new ConnectionDef();

        super.cloneComponents(myClone);
        if (null != getProperties()) {
            myClone.setProperties(getProperties().clone());
        }
        return cloneValues(myClone);
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, type, indentIn, "type");
        debugObject(bufferIn, connectString, indentIn, "connectString");
        debugObject(bufferIn, preSql, indentIn, "preSql");
        debugObject(bufferIn, postSql, indentIn, "postSql");
        doDebug(properties, bufferIn, indentIn, "properties", "GenericProperties");
    }
}
