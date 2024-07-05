package csi.server.common.model.extension;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ClassificationData
    extends ExtensionData
{
    protected String banner;
    protected int backgroundColor;
    protected int fontColor;
    
    public ClassificationData() {
    	super();
        this.name = Classification.NAME;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }
    
    public ClassificationData copy() {
        ClassificationData other = new ClassificationData();
        other.setUuid(this.getUuid());
        other.banner = this.banner;
        other.backgroundColor = this.backgroundColor;
        other.fontColor = this.fontColor;
        return other;
    }
    
    

}
