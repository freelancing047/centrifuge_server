package csi.server.common.publishing.pdf;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.publishing.Asset;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PdfAsset extends Asset {

    public Integer width;

    public Integer height;

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

}
