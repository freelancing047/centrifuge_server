package csi.server.common.model.icons;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.collect.Sets;

import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.Resource;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@EntityListeners(ResourceACLMonitor.class)
public class Icon extends Resource implements Serializable {

    @Column(length = 2147483647)
    @Lob
    private String image;

//    @OneToMany(cascade = CascadeType.ALL)
//    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    private  Set<String> tags = Sets.newHashSet();


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="icon_tags", joinColumns=@JoinColumn(name="icon_uuid"))
    @Column(name="tag")
    @Fetch(FetchMode.SUBSELECT)
    private  Set<String> tags = Sets.newTreeSet();

    //private List<Integer> tags;

//    private byte[] image;
//
//    public byte[] getImage() {
//        return image;
//    }
//
//    public void setImage(byte[] image) {
//        this.image = image;
//    }

    public Icon() {

        super(AclResourceType.ICON);
    }

    public Icon(String uuidIn, String nameIn, String imageIn, Set<String> tagsIn){

        super(AclResourceType.ICON);

        setName(nameIn);
        setImage(imageIn);
        if ((null != tagsIn) && !tagsIn.isEmpty()) {

            getTags().addAll(tagsIn);
        }
        setUuid(uuidIn);
    }

    public Icon(String nameIn, String imageIn, Set<String> tagsIn){

        super(AclResourceType.ICON);

        setName(nameIn);
        setImage(imageIn);
        if ((null != tagsIn) && !tagsIn.isEmpty()) {

            getTags().addAll(tagsIn);
        }
        setUuid(CsiUUID.getImageIconId(imageIn));
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public Icon clone() {

        Icon myClone = new Icon();

        super.cloneComponents(myClone);
        return cloneValues(myClone);
    }

    private Icon cloneValues(Icon cloneIn) {

        cloneIn.setImage(image);
        cloneIn.getTags().addAll(tags);
        return cloneIn;
    }


//    public List<Integer> getTags() {
//        return tags;
//    }
//
//    public void setTags(List<Integer> tags) {
//        this.tags = tags;
//    }
}
