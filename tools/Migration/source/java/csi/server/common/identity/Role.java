package csi.server.common.identity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;


/*
 * 
 * NB: The resulting DDL causes us some pain in populating--via SQL--the default
 * users.  Any changes to this class, User, or Group need to be reflected
 * in source/resources/CreateUsers.jdbc SQL script! 
 *
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance (strategy = InheritanceType.JOINED )
@Table (name = "Roles" )
public abstract class Role implements Comparable
{
    protected Long id;

    protected String name;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY )
    public Long getId()
    {
        return id;
    }

    protected List<Group> groups;
     
    @ManyToMany(mappedBy = "members", targetEntity = Group.class, cascade = CascadeType.ALL)
    public List<Group> getGroups() 
    {
        return groups;
    }
    
    public void setGroups(List<Group> groups)
    {
        this.groups = groups;
    }
    
    public void setId( Long id )
    {
        this.id = id;
    }

   @Column(name="name", columnDefinition="VARCHAR(255) NOT NULL UNIQUE")
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final Role other = (Role)obj;
        if( name == null ) {
            if( other.name != null )
                return false;
        } else if( !name.equals( other.name ) )
            return false;
        return true;
    }
    
//    @Override
//    public int hashCode()
//    {
//        return (id == null) ? 0 : id.hashCode();
//    }
//    
//    @Override
//    public  boolean equals(Object obj)
//    {
//        return obj != null && obj instanceof Role && ((Role)obj).id.equals( this.id );
//    }
//
    @Override
    public int compareTo( Object o )
    {
        if (o == null) {
            return 1;
        }
        
        Role other = (Role) o;
        if (this.name == null && other.getName() == null) {
            return 0;
        }
        
        if (this.name != null && other.getName() == null) {
            return 1;
        }
        
        if (this.name == null && other.getName() != null) {
            return -1;
        }
        
        return this.name.compareTo(other.getName());
    }
    
    

}
