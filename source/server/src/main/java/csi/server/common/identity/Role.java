package csi.server.common.identity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Roles")
public abstract class Role implements Serializable {

    protected Long id;

    protected String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    protected List<Group> groups;

    @ManyToMany(mappedBy = "members", targetEntity = Group.class)
    public List<Group> getGroups() {
       if (groups == null) {
          groups = new ArrayList<Group>();
       }
       return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", columnDefinition = "VARCHAR(255) NOT NULL UNIQUE")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Role) && ((Role) obj).id.equals(this.id);
    }
}
