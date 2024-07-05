package csi.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Created by centrifuge on 2/6/2017.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DistributionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected Long parentId;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="DistributionTag_roleList")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<GenericSecurityTag> roleList = new ArrayList<GenericSecurityTag>();

    public DistributionTag() {
    }

    public DistributionTag(Collection<GenericSecurityTag> roleNameListIn) {

        setRoleNameList(roleNameListIn);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentIdIn) {
        parentId = parentIdIn;
    }

    public List<GenericSecurityTag> getRoleList() {

        return roleList;
    }

    public void setRoleList(List<GenericSecurityTag> roleNameListIn) {

        roleList = roleNameListIn;
    }

    public List<GenericSecurityTag> getRoleNameList() {

        return new ArrayList<GenericSecurityTag>(roleList);
    }

    public void setRoleNameList(Collection<GenericSecurityTag> roleNameListIn) {

        roleList = new ArrayList<GenericSecurityTag>(new TreeSet<GenericSecurityTag>(roleNameListIn));
    }

    public DistributionTag clone() {

        DistributionTag myNewTag = new DistributionTag();
        List<GenericSecurityTag> myRoleList = myNewTag.getRoleList();

        for (GenericSecurityTag myEntry : roleList) {

            myRoleList.add(myEntry);
        }
        return myNewTag;
    }
}
