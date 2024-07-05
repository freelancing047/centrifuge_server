package csi.server.common.model.visualization.pattern;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Sets;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "graphpatterns")
public class PersistedPattern implements Serializable {
    @Column(name = "require_distinct_nodes")
    boolean requireDistinctNodes = false;
    private String owner;
    @Id
    @Column(name = "pattern_uuid")
    private String uuid;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(
            name = "graphpatterns_patternnodes",
            joinColumns = @JoinColumn(name = "pattern_uuid"),
            inverseJoinColumns = @JoinColumn(name = "patternnode_uuid")
    )
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Set<PersistedPatternNode> patternNodes = Sets.newHashSet();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinTable(
            name = "graphpatterns_patternlinks",
            joinColumns = @JoinColumn(name = "pattern_uuid"),
            inverseJoinColumns = @JoinColumn(name = "patternlink_uuid")
    )
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Set<PersistedPatternLink> patternLinks = Sets.newHashSet();
    @Column(name = "require_distinct_links")
    private boolean requireDistinctLinks;

    public PersistedPattern() {
    }

    public boolean isRequireDistinctNodes() {
        return requireDistinctNodes;
    }

    public void setRequireDistinctNodes(boolean requireDistinctNodes) {
        this.requireDistinctNodes = requireDistinctNodes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PersistedPatternNode> getPatternNodes() {
        return patternNodes;
    }

    public void setPatternNodes(Set<PersistedPatternNode> patternNodes) {
        this.patternNodes = patternNodes;
    }

    public Set<PersistedPatternLink> getPatternLinks() {
        return patternLinks;
    }

    public void setPatternLinks(Set<PersistedPatternLink> patternLinks) {
        this.patternLinks = patternLinks;
    }

    public boolean isRequireDistinctLinks() {
        return requireDistinctLinks;
    }

    public void setRequireDistinctLinks(boolean requireDistinctLinks) {
        this.requireDistinctLinks = requireDistinctLinks;
    }
}
