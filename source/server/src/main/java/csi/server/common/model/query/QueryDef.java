package csi.server.common.model.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import csi.server.common.model.DataSourceDef;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.InterceptorType;
import csi.server.common.model.Resource;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QueryDef extends Resource {

    
    //FIXME: Why is this not an Enum?
    //Brian Murray 10/9/2013
    protected String queryType;

    @Column(columnDefinition = "TEXT")
    protected String sql;

    @Column(columnDefinition = "TEXT")
    protected String queryText;

    @Column(columnDefinition = "TEXT")
    protected String linkupText;

    @ElementCollection
    protected List<String> parameters;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<QueryInterceptorDef> interceptors;

    protected int timeout;

    @OneToOne(cascade = CascadeType.ALL)
    protected GenericProperties properties;

    public QueryDef() {
    	super(AclResourceType.QUERY);
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String value) {
        this.queryType = value;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String value) {
        this.queryText = value;
    }

    public String getLinkupText() {
        return linkupText;
    }

    public void setLinkupText(String value) {
        this.linkupText = value;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<String>();
        }
        return this.parameters;
    }

    public List<QueryInterceptorDef> getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<QueryInterceptorDef>();
        }
        return this.interceptors;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<QueryInterceptorDef> getSortedInterceptors(InterceptorType type) {
        List<QueryInterceptorDef> list = new ArrayList<QueryInterceptorDef>();
        for (QueryInterceptorDef i : getInterceptors()) {
            if (type == null || type == i.getType()) {
                list.add(i);
            }
        }
        Collections.sort(list);
        return list;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public void setInterceptors(List<QueryInterceptorDef> interceptors) {
        this.interceptors = interceptors;
    }

    public GenericProperties getProperties() {
        return properties;
    }

    public void setProperties(GenericProperties value) {
        this.properties = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public QueryDef clone() {

        QueryDef myClone = new QueryDef();

        super.cloneComponents(myClone);

        return cloneValues(myClone);
    }

    public QueryDef fullClone() {

        QueryDef myClone = new QueryDef();

        super.fullCloneComponents(myClone);

        return cloneValues(myClone);
    }

    public QueryDef cloneValues(QueryDef cloneIn) {

        cloneIn.setQueryType(getQueryType());
        cloneIn.setSql(getSql());
        cloneIn.setQueryText(getQueryText());
        cloneIn.setLinkupText(getLinkupText());
        cloneIn.setTimeout(getTimeout());
        if (null != getProperties()) {
            cloneIn.setProperties(getProperties().clone());
        }
        cloneIn.setParameters(cloneParameters());
        cloneIn.setInterceptors(cloneInterceptors());

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, queryType, indentIn, "queryType");
        debugObject(bufferIn, sql, indentIn, "sql");
        debugObject(bufferIn, queryText, indentIn, "queryText");
        debugObject(bufferIn, linkupText, indentIn, "linkupText");
        debugObject(bufferIn, timeout, indentIn, "timeout");
        doDebug(properties, bufferIn, indentIn, "properties", "GenericProperties");
//        debugList(bufferIn, parameters, indentIn, "parameters");
        debugList(bufferIn, interceptors, indentIn, "interceptors");
    }
    
    private List<String> cloneParameters() {
        
        if (null != getParameters()) {
            
            List<String>  myList = new ArrayList<String>();
            
            for (String myItem : getParameters()) {
                
                myList.add(myItem);
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    private List<QueryInterceptorDef> cloneInterceptors() {
        
        if (null != getInterceptors()) {
            
            List<QueryInterceptorDef>  myList = new ArrayList<QueryInterceptorDef>();
            
            for (QueryInterceptorDef myItem : getInterceptors()) {
                
                myList.add(myItem.clone());
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
}