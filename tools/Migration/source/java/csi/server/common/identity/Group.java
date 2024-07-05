package csi.server.common.identity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


/*
 * 
 * NB: The resulting DDL causes us some pain in populating--via SQL--the default
 * users.  Any changes to this class, User, or Group need to be reflected
 * in source/resources/CreateUsers.jdbc SQL script! 
 *
 */


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table (name = "Groups" )
public class Group
        extends Role
{
    protected Set<Role> members;

    @JoinTable (name = "Group_Members", joinColumns = {
        @JoinColumn (name = "GROUP_ID", referencedColumnName = "ID" )
    }, inverseJoinColumns = @JoinColumn (name = "ROLE_ID", referencedColumnName = "ID" ) )
    @ManyToMany(cascade=CascadeType.ALL)
    public Set<Role> getMembers()
    {
        return members;
    }

    public void setMembers( Set<Role> set )
    {
        members = set;
    }

}