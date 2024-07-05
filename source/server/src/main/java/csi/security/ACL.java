package csi.security;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ACL
{

    @Id
    @GeneratedValue
    protected Long id;
    protected String uuid;
    protected String owner;
    protected boolean locked = false;
    protected boolean useCapcoDefault = false;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>();
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name="ACL_sourceEntries")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<SourceAclEntry> sourceEntries = new ArrayList<SourceAclEntry>();
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name="ACL_linkupEntries")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<SourceAclEntry> linkupEntries = new ArrayList<SourceAclEntry>();
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name="ACL_capcoTags")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<CapcoSecurityTag> capcoTags = new ArrayList<CapcoSecurityTag>();
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name="ACL_genericTags")
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<GenericSecurityTag> genericTags = new ArrayList<GenericSecurityTag>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "parentId")
    protected List<DistributionTag> distributionTags = new ArrayList<DistributionTag>();

    public ACL() { }

    public ACL(String ownerIn) {
        owner = ownerIn;
    }

    public ACL(String ownerIn, String uuidIn) {
        owner = ownerIn;
        uuid = uuidIn;
    }

    public boolean isLocked() {

        return locked;
    }

    public void setLocked(boolean lockedIn) {

        locked = lockedIn;
    }

    public boolean getUseCapcoDefault() {

        return useCapcoDefault;
    }

    public void setUseCapcoDefault(boolean useCapcoDefaultIn) {

        useCapcoDefault = useCapcoDefaultIn;
    }

    public List<AccessControlEntry> getEntries() {
        return entries;
    }

    public List<SourceAclEntry> getSourceEntries() {
        return sourceEntries;
    }

    public List<SourceAclEntry> getLinkupEntries() {
        return linkupEntries;
    }

    public List<CapcoSecurityTag> getCapcoTags() {
        return capcoTags;
    }

    public List<GenericSecurityTag> getGenericTags() {
        return genericTags;
    }

    public List<DistributionTag> getDistributionTags() {
        return distributionTags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ACL clone() {

        ACL myNewACL = new ACL();
        List<AccessControlEntry> myEntries = myNewACL.getEntries();
        List<SourceAclEntry> mySourceEntries = myNewACL.getSourceEntries();
        List<SourceAclEntry> myLinkupEntries = myNewACL.getLinkupEntries();
        List<CapcoSecurityTag> myCapcoTags = myNewACL.getCapcoTags();
        List<GenericSecurityTag> myGenericTags = myNewACL.getGenericTags();
        List<DistributionTag> myDistributionTags = myNewACL.getDistributionTags();

        myNewACL.setOwner(owner);
        myNewACL.setUseCapcoDefault(useCapcoDefault);
        for (AccessControlEntry myEntry : entries) {

            myEntries.add(myEntry.clone());
        }
        for (SourceAclEntry myEntry : sourceEntries) {

            mySourceEntries.add(myEntry);
        }
        for (SourceAclEntry myEntry : linkupEntries) {

            myLinkupEntries.add(myEntry);
        }
        for (CapcoSecurityTag myEntry : capcoTags) {

            myCapcoTags.add(myEntry);
        }
        for (GenericSecurityTag myEntry : genericTags) {

            myGenericTags.add(myEntry);
        }
        for (DistributionTag myEntry : distributionTags) {

            myDistributionTags.add(myEntry.clone());
        }
        return myNewACL;
    }
}
