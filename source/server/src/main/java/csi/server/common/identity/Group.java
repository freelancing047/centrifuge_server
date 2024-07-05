package csi.server.common.identity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
@Table(name = "Groups")
public class Group extends Role {

    protected Set<Role> members;

    protected GroupType type;

    protected String display;

    protected String remark;

    protected boolean external;

    @JoinTable(name = "Group_Members", joinColumns = { @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID") }, inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID"))
    @ManyToMany
    public Set<Role> getMembers() {
        return members;
    }

    public void setMembers(Set<Role> set) {
        members = set;
    }

    public Group() {

        type = GroupType.SHARING;
    }

    public Group(String remarkIn) {

        type = GroupType.SHARING;
        remark = remarkIn;
    }

    public Group(GroupType typeIn) {

        type = typeIn;
    }

    public Group(GroupType typeIn, String remarkIn) {

        type = typeIn;
        remark = remarkIn;
    }

    public GroupType getType() {

        return type;
    }

    public void setType(GroupType typeIn) {

        type = typeIn;
    }

    public String getRemark() {

        return remark;
    }

    public void setRemark(String remarkIn) {

        remark = remarkIn;
    }

    public boolean getExternal() {

        return external;
    }

    public void setExternal(boolean externalIn) {

        external = externalIn;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String displayIn) {
        this.display = displayIn;
    }

   public void addMember(Role memberIn) {
      if (members == null) {
         members = new HashSet<Role>();
      }
      members.add(memberIn);
   }

   public void removeMember(Role memberIn) {
      if (members != null) {
         members.remove(memberIn);

         if (members.isEmpty()) {
            members = null;
         }
      }
   }
}
