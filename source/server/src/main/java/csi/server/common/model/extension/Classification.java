package csi.server.common.model.extension;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/*
 * Note this configuration could be a more 
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Classification
    extends SimpleExtension
{
    public final static String NAME = "classification";
    
    public Classification() {
    	super();
        this.name = NAME;
        this.description = "Configuration of classification labels for a Dataview";
    }

    protected String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
