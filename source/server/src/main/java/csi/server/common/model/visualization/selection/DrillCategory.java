package csi.server.common.model.visualization.selection;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DrillCategory extends ModelObject implements Serializable {

    /**
     * A category may be a simple category (in which case the list has one element)
     * or a 'drill' category with parent categories (e.g. State -> City).
     * The containing category will be first in the list.
     */
    @Column(length = 2147483647)
    @Lob
    private ArrayList<String> categories = new ArrayList<String>();

    public DrillCategory() {
        super();
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String convertCategoriesToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            sb.append(categories.get(i));

            if (i + 1 < categories.size()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
