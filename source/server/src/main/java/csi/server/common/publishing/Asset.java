package csi.server.common.publishing;

import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import csi.security.monitors.AssetACLMonitor;
import csi.server.common.model.extension.ClassificationData;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AssetACLMonitor.class)
public class Asset {

    public static final String ASSET_ID = "assetID";
    public static final String DATA_VIEW_ID = "dataViewId";
    public static final String NAME = "name";

    protected static String QUERY = "from Asset a";

    @Id
    @GeneratedValue
    private Long id;

    private String assetID;

    private String createdBy;

    private String name;

    private Date creationTime;

    @Column(length = 16384)
    private String description;

    private String dataViewName;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SUBSELECT)
    @Sort(type = SortType.NATURAL)
    protected SortedSet<Tag> tags = new TreeSet<Tag>();

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(FetchMode.SUBSELECT)
    @Sort(type = SortType.NATURAL)
    protected SortedSet<Comment> comments = new TreeSet<Comment>();

//    @OneToOne(cascade = { CascadeType.ALL })
//    @PrimaryKeyJoinColumn
//    private ClassificationInfo classificationInfo;
    
    @OneToOne( cascade = { CascadeType.ALL } )
    private ClassificationData classificationData;

    /**
     * Access Control Identifier -- opaque string applied by run-time security
     * checks
     */
    private String aci;

    protected Asset() {
        // Don't instantiate this directly.
    }

    public String getAssetID() {
        return assetID;
    }

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataViewName() {
        return dataViewName;
    }

    public void setDataViewName(String dataViewName) {
        this.dataViewName = dataViewName;
    }

    public String getAci() {
        return aci;
    }

    public void setAci(String aci) {
        this.aci = aci;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public ClassificationData getClassificationData() {
        return classificationData;
    }

    public void setClassificationData(ClassificationData classificationData) {
        this.classificationData = classificationData;
    }

    
    @Transient
    public boolean containsTags(SortedSet<String> matchValues) {
        // No need to search if there are more tags
        // needing to matched than the asset has.
        boolean allFound = matchValues.size() <= tags.size();

        if (allFound) {

            Iterator<String> valIter = matchValues.iterator();
            Iterator<Tag> tagIter = tags.iterator();

            while (valIter.hasNext() && allFound) {

                String tagValue = valIter.next().toLowerCase();

                boolean valueHit = false;
                while (tagIter.hasNext() && !valueHit) {
                    valueHit = tagValue.equals(tagIter.next().lowerValue());
                }

                allFound = valueHit;
            }
        }

        return allFound;
    }


    @Transient
    public String getThumbnailFileName() {
        return getThumbnailFileName(assetID);
    }

    public static String getThumbnailFileName(String assetID) {
        return assetID + ".png";
    }

    public SortedSet<Tag> getTags() {
        if (tags == null) {
            tags = new TreeSet<Tag>();
        }
        return tags;
    }

    public void setTags(SortedSet<Tag> tags) {
        this.tags = (tags == null) ? new TreeSet<Tag>() : tags;
    }

    public void addTag(Tag newTag) {
        getTags().add(newTag);
    }

    @Transient
    public String getAssetListString() {
        StringBuilder sb = new StringBuilder();

        for (Tag tag : tags) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            String tagVal = tag.getValue();
            if (tagVal.contains(" ")) {
                sb.append('"').append(tagVal).append('"');
            } else {
                sb.append(tagVal);
            }
        }

        return sb.toString();
    }

    public SortedSet<Comment> getComments() {
        if (comments == null) {
            comments = new TreeSet<Comment>();
        }

        return comments;
    }

    public void setComments(SortedSet<Comment> comments) {
        this.comments = (comments == null) ? new TreeSet<Comment>() : comments;
    }

    public void addComment(Comment newComment) {
        getComments().add(newComment);
    }
}
