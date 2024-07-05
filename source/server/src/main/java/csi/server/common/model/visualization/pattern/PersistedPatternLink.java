package csi.server.common.model.visualization.pattern;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "patternlinks")
public class PersistedPatternLink implements Serializable {
	@Id @Column(name="patternlink_uuid")
    private String uuid;
    private String name;
    @Column(name = "show_in_results")
    private boolean showInResults;
    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(
            name="patternlinks_patternnodes",
            joinColumns = @JoinColumn( name="patternlink_uuid"),
            inverseJoinColumns = @JoinColumn( name="patternnode_uuid")
    )
	private List<PersistedPatternNode> patternNodes;
	@ElementCollection
	@CollectionTable(name="patternlinks_criteria", joinColumns=@JoinColumn(name="patternlink_uuid"))
	@Column(name="criterion")
	private List<String> criteria;

    public PersistedPatternLink() {
        patternNodes = Lists.newArrayList();
        criteria = Lists.newArrayList();
    }

    public List<PersistedPatternNode> getPatternNodes() {
        return patternNodes;
    }

    public void setPatternNodes(List<PersistedPatternNode> patternNodes) {
        this.patternNodes = patternNodes;
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

	public PersistedPatternNode getNode1() {
		return patternNodes.get(0);
    }

	public void setNode1(PersistedPatternNode node1) {
		patternNodes.add(0,node1);
    }

	public PersistedPatternNode getNode2() {
		return patternNodes.get(1);
    }

	public void setNode2(PersistedPatternNode node2) {
		patternNodes.add(1,node2);
    }

	public List<String> getCriteria() {
        return criteria;
    }

	public void setCriteria(List<String> criteria) {
		this.criteria = criteria;
    }

    public boolean isShowInResults() {
        return showInResults;
    }

    public void setShowInResults(boolean showInResults) {
        this.showInResults = showInResults;
    }

}
