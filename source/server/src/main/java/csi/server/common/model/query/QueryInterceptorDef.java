package csi.server.common.model.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.GenericProperties;
import csi.server.common.model.InterceptorType;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QueryInterceptorDef extends ModelObject implements Comparable<QueryInterceptorDef> {

    protected String name;

    @Enumerated(value = EnumType.STRING)
    protected InterceptorType type;

    protected int priority;

    protected boolean runForEach;

    protected boolean runAlways;

    @Column(length = 4096)
    protected String queryText;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(nullable = true)
    protected List<QueryParameterDef> inParameters;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(nullable = true)
    protected List<QueryParameterDef> outParameters;

    @OneToOne(cascade = CascadeType.ALL)
    protected GenericProperties properties;

    public QueryInterceptorDef() {
    	super();
    }

    public List<QueryParameterDef> getInParameters() {
        if (inParameters == null) {
            inParameters = new ArrayList<QueryParameterDef>();
        }
        return inParameters;
    }

    public void setInParameters(List<QueryParameterDef> inParameters) {
        this.inParameters = inParameters;
    }

    public List<QueryParameterDef> getOutParameters() {
        if (outParameters == null) {
            outParameters = new ArrayList<QueryParameterDef>();
        }
        return outParameters;
    }

    public void setOutParameters(List<QueryParameterDef> outParameters) {
        this.outParameters = outParameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isRunForEach() {
        return runForEach;
    }

    public void setRunForEach(boolean runForEach) {
        this.runForEach = runForEach;
    }

    public boolean isRunAlways() {
        return runAlways;
    }

    public void setRunAlways(boolean runAlways) {
        this.runAlways = runAlways;
    }

    public InterceptorType getType() {
        return type;
    }

    public void setType(InterceptorType type) {
        this.type = type;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String value) {
        this.queryText = value;
    }

    public Map<String, String> getInputParameterValueMap() {
        Map<String, String> paramMap = new HashMap<String, String>();

        for (QueryParameterDef p : getInParameters()) {
            paramMap.put(p.getName().toLowerCase(), p.getValue());
        }

        return paramMap;
    }

    public Map<String, QueryParameterDef> getInputParametersByName() {
        Map<String, QueryParameterDef> paramMap = new HashMap<String, QueryParameterDef>();

        for (QueryParameterDef p : getInParameters()) {
            if (p.getName() != null) {
                paramMap.put(p.getName().toLowerCase(), p);
            }
        }

        return paramMap;
    }

    @Override
    public int compareTo(QueryInterceptorDef o) {
        return Integer.valueOf(this.getPriority()).compareTo(o.getPriority());
    }

    public GenericProperties getProperties() {
        return properties;
    }

    public void setProperties(GenericProperties value) {
        this.properties = value;
    }
   
    @Override
    public QueryInterceptorDef clone() {
        
        QueryInterceptorDef myClone = new QueryInterceptorDef();
        
        super.cloneComponents(myClone);

        myClone.setName(getName());
        myClone.setType(getType());
        myClone.setPriority(getPriority());
        myClone.setRunForEach(isRunForEach());
        myClone.setRunAlways(isRunAlways());
        myClone.setQueryText(getQueryText());
        if (null != getProperties()) {
            myClone.setProperties(getProperties().clone());
        }
        myClone.setInParameters(cloneQueryParameterList(getInParameters()));
        myClone.setOutParameters(cloneQueryParameterList(getOutParameters()));
        
        return myClone;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, type, indentIn, "type");
        debugObject(bufferIn, priority, indentIn, "priority");
        debugObject(bufferIn, runForEach, indentIn, "runForEach");
        debugObject(bufferIn, runAlways, indentIn, "runAlways");
        debugObject(bufferIn, queryText, indentIn, "queryText");
        doDebug(properties, bufferIn, indentIn, "properties", "GenericProperties");
        debugList(bufferIn, inParameters, indentIn, "inParameters");
        debugList(bufferIn, outParameters, indentIn, "outParameters");
    }
    
    private List<QueryParameterDef> cloneQueryParameterList(List<QueryParameterDef> listIn) {
        
        if (null != listIn) {
            
            List<QueryParameterDef>  myList = new ArrayList<QueryParameterDef>();
            
            for (QueryParameterDef myItem : listIn) {
                
                myList.add(myItem.clone());
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
}
