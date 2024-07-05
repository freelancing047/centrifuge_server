package csi.server.common.identity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.GroupType;

/*
 * 
 * NB: The resulting DDL causes us some pain in populating--via SQL--the default
 * users.  Any changes to this class, User, or Group need to be reflected
 * in source/resources/CreateUsers.jdbc SQL script! 
 *
 */

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CapcoGroups")
public class CapcoGroup extends Group {
    
    @Enumerated(value = EnumType.STRING)
    protected CapcoSection section;

    protected String portion;
    
    protected boolean reject = false;

    public CapcoGroup() {
        
        super(GroupType.SECURITY);
    }

    public CapcoGroup(String remarksIn) {
        
        super(GroupType.SECURITY, remarksIn);
    }
    
    public void setSection(CapcoSection sectionIn) {
        
        section = sectionIn;
    }
    
    public CapcoSection getSection() {
        
        return section;
    }

    public void setPortion(String portionIn) {

        portion = portionIn;
    }
    
    public String getPortion() {
        
        return portion;
    }

    public void setReject(boolean rejectIn) {
        
        reject = rejectIn;
    }
    
    public boolean isReject() {
        
        return reject;
    }
}
