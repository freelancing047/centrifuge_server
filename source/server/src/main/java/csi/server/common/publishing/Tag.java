package csi.server.common.publishing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.security.Authorization;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Tags")
public class Tag implements Comparable<Tag> {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    protected String value;

    @Column(nullable = false)
    protected String creator;

    @Transient
    protected String lowerTag;

    /**
     * Default c'tor used by Hibernate.
     */
    public Tag() {

    }

    public Tag(String value, Authorization creator) {
        this.value = value;
        this.creator = creator.getName();
    }

    public String getValue() {
        return value;
    }

    public Long getId() {
        return id;
    }

    public String getCreator() {
        return creator;
    }

  public boolean tagMatches(String valueStr) {
     return (value != null) && lowerValue().equals(valueStr);
  }

    public String lowerValue() {
        if (lowerTag == null) {
            lowerTag = this.value.toLowerCase();
        }

        return lowerTag;
    }

    @Override
    public int compareTo(Tag other) {
        return (other == null) ? 1 : this.value.compareTo(other.value);
    }

}
