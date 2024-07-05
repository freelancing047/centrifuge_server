package csi.server.business.visualization.graph.base;

import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.ShapeWheel;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class TypeInfo {
   private static final Logger LOG = LogManager.getLogger(TypeInfo.class);

   public String name;

    public Integer color;

    public ShapeType shape;

    public String iconId;

    public int totalCount;

    public int visible;
    
    public String key;
    
    public boolean dynamic;

    public boolean shapeOverride = false;
    public boolean colorOverride = false;
    public boolean iconOverride =  false;

    public void copyFrom(TypeInfo other) {
        this.name = other.name;
        this.color = other.color;
        this.shape = other.shape;
        this.iconId = other.iconId;
        this.totalCount = other.totalCount;
        this.key = other.key;
        this.shapeOverride = other.shapeOverride;
        this.colorOverride = other.colorOverride;
        this.iconOverride = other.iconOverride;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.toLowerCase().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TypeInfo other = (TypeInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equalsIgnoreCase(other.name))
            return false;
        return key.equals(other.key);
    }

    /**
     * Builds a TypeInfo object. It is appliable for nodes.
     * @param nodeType       the type of the node.
     * @param attributes     map of atributes where the Node has saved its color, shape, or any other atributes.
     * @param dynamicType    tells if the node is a dynamic one.
     * @param dynamicIcon
     * @param theme 
     * @return
     */
    public static TypeInfo initializeTypeInfo(String nodeType, Map<String, Object> attributes, boolean dynamicType, boolean dynamicIcon, GraphTheme theme) {
        TypeInfo typeInfo = new TypeInfo();

        typeInfo.name = nodeType;
        typeInfo.key = nodeType;
        typeInfo.dynamic = dynamicType;
        
        typeInfo.shapeOverride = attributes.get(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE) != null;
        typeInfo.colorOverride = attributes.get(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE) != null;
        typeInfo.iconOverride = attributes.get(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE) != null;

        typeInfo.color = determineColor(dynamicType, attributes, typeInfo.colorOverride);
        typeInfo.shape = determineShape(dynamicType, attributes, typeInfo.shapeOverride, theme);
        
        
        if (!dynamicIcon) {
            typeInfo.iconId = (String) attributes.get(ObjectAttributes.CSI_INTERNAL_ICON);
        }

        return typeInfo;
    }

    private static Integer determineColor(boolean dynamicType, Map<String, Object> attributes, boolean colorOverride) {
        
        Integer colorValue = null;
        
        if(dynamicType && !colorOverride) {
            //colorValue = ColorWheel.next();
            colorValue = getRandomColor();
        } else {
            Object color = attributes.get(ObjectAttributes.CSI_INTERNAL_COLOR);
            if (color != null) {
                if (color instanceof Number) {
                    colorValue = ((Number) color).intValue();
                } else if(color instanceof Color){
                    colorValue = ((Color)color).getIntColor();
                } else {
                    try {
                        colorValue = Integer.parseInt(color.toString());
                    } catch (Throwable ex) {
                       LOG.warn(String.format("Failure parsing value '%s'", color));
                    }
                }
            }
        }
        return colorValue;
    }

    private static ShapeType determineShape(boolean dynamicType, Map<String, Object> attributes, boolean shapeOverride, GraphTheme theme) {
    
        ShapeType shapeValue = null;
        Object shape = attributes.get(ObjectAttributes.CSI_INTERNAL_SHAPE);
        
        if (dynamicType && (!shapeOverride && shape !=null)) {
            if(theme == null || theme.getDefaultShape() == null) {
                shapeValue = ShapeType.getShape(ShapeWheel.next());
            } else {
                shapeValue = theme.getDefaultShape();
            }
        } else {
            
            if(shape instanceof ShapeType){
                shapeValue = (ShapeType)shape;
            } else if(shape instanceof String){
                shapeValue = ShapeType.getShape((String) shape);
            }
        }
        return shapeValue;
    }

    public static TypeInfo createSimpleTypeInfo(String type, String mapKey) {
        TypeInfo info = new TypeInfo();
        info.color = getRandomColor();
        info.shape = ShapeType.getShape(ShapeWheel.next());
        info.totalCount = 0;
        info.visible = 0;
        info.key = mapKey;
        info.name = type;
        info.shapeOverride = false;
        info.iconOverride = false;
        info.colorOverride = false;
        return info;
    }


    private static int getRandomColor() {
        Random r = new Random();
        float satuartion = (float) ((r.nextDouble() / 5F) + .7F);
        float value = (float) ((r.nextDouble() / 4F) + .75F);
        return ClientColorHelper.get().randomHueWheel(satuartion, value).getIntColor();
    }
}
