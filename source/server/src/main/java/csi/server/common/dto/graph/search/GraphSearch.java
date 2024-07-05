package csi.server.common.dto.graph.search;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GraphSearch implements IsSerializable {

    private boolean visibleGraphSearch;

    private SearchType searchType;

    private List<LinkSearchCriterion> linkCriteria;

    private List<NodeSearchCriterion> nodeCriteria;

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public List<LinkSearchCriterion> getLinkCriteria() {
        if (linkCriteria == null) {
            linkCriteria = new ArrayList<LinkSearchCriterion>();
        }
        return linkCriteria;
    }

    public void setLinkCriteria(List<LinkSearchCriterion> linkCriteria) {
        this.linkCriteria = linkCriteria;
    }

    public List<NodeSearchCriterion> getNodeCriteria() {
        if (nodeCriteria == null) {
            nodeCriteria = new ArrayList<NodeSearchCriterion>();
        }
        return nodeCriteria;
    }

    public void setNodeCriteria(List<NodeSearchCriterion> nodeCriteria) {
        this.nodeCriteria = nodeCriteria;
    }

    public boolean isVisibleGraphSearch() {
        return visibleGraphSearch;
    }

    public void setVisibleGraphSearch(boolean visibleGraphSearch) {
        this.visibleGraphSearch = visibleGraphSearch;
    }

}
