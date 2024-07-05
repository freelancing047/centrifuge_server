package csi.server.common.model.visualization.pattern;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "patternnodes")
public class PersistedPatternNode implements Serializable {
	@Id @Column(name ="patternnode_uuid")
    private String uuid;
    private String name;
    @Column(name = "show_in_results")
    private boolean showInResults;
    @ElementCollection
	@CollectionTable(name="patternnodes_criteria", joinColumns=@JoinColumn(name="patternnode_uuid"))
	@Column(name="criterion")
    private List<String> criteria = Lists.newArrayList();
    private double drawY;
    private double drawX;

    public PersistedPatternNode() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCriteria() {
        return this.criteria;
    }

	public void setCriteria(List<String> criteria) {
		this.criteria = criteria;
    }

    public double getDrawY() {
        return drawY;
    }

    public void setDrawY(double drawY) {
        this.drawY = drawY;
    }

    public double getDrawX() {
        return drawX;
    }

    public void setDrawX(double drawX) {
        this.drawX = drawX;
    }

    public boolean isShowInResults() {
        return showInResults;
    }

    public void setShowInResults(boolean showInResults) {
        this.showInResults = showInResults;
    }

}
