package csi.server.business.helper.theme;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.common.base.Throwables;

import liquibase.util.file.FilenameUtils;

import csi.config.Configuration;
import csi.security.CsiSecurityManager;
import csi.server.business.service.icon.IconActionsService;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.resource.ImportStatus;
import csi.server.common.dto.resource.ImportStatusType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.LineStyle;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.Resource;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.dao.CsiPersistenceManager;

public class OldThemeHelper extends AbstractThemeHelper {
   private static final Logger LOG = LogManager.getLogger(OldThemeHelper.class);

   public static Resource importXML(BufferedReader inputReader, String filename, Map<String,String> iconMap)
         throws IOException, JDOMException, CentrifugeException {
      Theme theme;
      StringBuilder sb = new StringBuilder();
      String inline = "";

      while ((inline = inputReader.readLine()) != null) {
         sb.append(inline);
      }
      SAXBuilder builder = new SAXBuilder();

      try (ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes())) {
         Document document = builder.build(bis);
         Element root = document.getRootElement();

         if (root.getName().equals(OPTION_SET_NODE)) {
            Element themeTypeNode = root.getChild(THEME_TYPE_NODE);
            String themeType;

            if (themeTypeNode == null) {
               themeType = GRAPH_TYPE;
            } else {
               themeType = themeTypeNode.getValue();
            }
            if (themeType.equalsIgnoreCase(GRAPH_TYPE)) {
               theme = parseGraph(root, iconMap);
               theme.setResourceType(AclResourceType.GRAPH_THEME);
            } else if (themeType.equalsIgnoreCase(MAP_TYPE)) {
               theme = parseMap(root, iconMap);
               theme.setResourceType(AclResourceType.MAP_THEME);
            } else {
               throw new CentrifugeException("Theme visualization type not recognized: " + themeType);
            }
            if (theme.getName() == null) {
               theme.setName(filename);
            }
            theme.setRemarks(themeType);
         } else {
            throw new CentrifugeException("Root XML element must be OptionSet");
         }
      }
      return theme;
   }

   public static ImportStatus saveTheme(Resource imported, boolean forceSave) throws CentrifugeException {
      ImportStatus importStatus = new ImportStatus();
      importStatus.className = imported.getClass().getName();

      try {
         if (forceSave) {
            Resource saved = CsiPersistenceManager.persist(imported);

            importStatus.status = ImportStatusType.OK;
            importStatus.itemName = imported.getName();
            importStatus.message = String.format("Import of '%s' succeeded.", importStatus.itemName);
            importStatus.uuid = saved.getUuid();
         }
      } catch (Throwable ex) {
         Throwables.propagate(ex);
      }
      return importStatus;
   }

    @SuppressWarnings("rawtypes")
    public static Resource oldUnzipTheme(ZipFile zipFile, String fileName, boolean securityCheck, Map<String, String> iconMap) throws IOException, JDOMException, CentrifugeException {
        boolean iconUploadAccess = hasManagementAccess(securityCheck);
        ZipEntry themeXml = null;
        Resource imported = null;
        String tagName;
        try{
            tagName = fileName.split(REGEX_PERIOD)[0];
            tagName = tagName.split(REGEX_UNDERSCORE)[0];
        } catch(Exception exception){
            //ignore, this is just for convenience
            tagName = null;
        }
        for(Enumeration e = zipFile.entries(); e.hasMoreElements(); ){
            ZipEntry entry = (ZipEntry) e.nextElement();
            if(entry.isDirectory()){
                //ignore
            } else {
                String name = entry.getName();
                String extension = FilenameUtils.getExtension(name);
                extension = extension.toLowerCase();

                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    if(extension.equals("xml")){
                        themeXml = entry;

                    } else if(extension.equals("png") || extension.equals("gif") || extension.equals("jpg")){
                        /*String id = */oldSaveImage(inputStream, extension, name, tagName, iconMap, iconUploadAccess);
                    }
                } catch(Exception exception){
                }
            }
        }


        //We have to do this last to get all the icons in the theme
        try (InputStream inputStream = zipFile.getInputStream(themeXml);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader inputReader = new BufferedReader(inputStreamReader)) {
           if (themeXml != null) {
              String name = themeXml.getName().split(REGEX_PERIOD)[0];
              String[] nameParts = name.split(REGEX_FORWARD_SLASH);
              name = nameParts[nameParts.length - 1];
              imported = OldThemeHelper.importXML(inputReader, name, iconMap);
           }
        } catch(Exception e){
        }
        return imported;
    }

    private static MapTheme parseMap(Element root, Map<String,String> iconMap) {
        MapTheme theme = new MapTheme();
        theme.setName(root.getAttributeValue(NAME));
        theme.setUuid(root.getAttributeValue(ID));

        //optset.bgcolor = reader.getAttribute("RGbgcolor");
        @SuppressWarnings("rawtypes")
        Iterator iterator = root.getChildren().iterator();
        float defaultIconScale = 1.0F;
        String iconRoot = "";
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();

            String nodeName = element.getName();
            if (nodeName.equals(COMMENT2)) {
                theme.setRemarks(element.getValue());
                //goes in client props for now
                //            } else if (nodeName.equals("BundleThreshold")) {
                //                //optset.bundleThreshold = element.getValue();
            } else if(nodeName.equals(ICON_SCALE)){
                defaultIconScale = Float.parseFloat(element.getValue());
            } else if(nodeName.equals(ICON_ROOT)){
                if(element.getValue() != null) {
                  iconRoot = element.getValue();
               }
            } else if(nodeName.equals(PLACE_TYPE)){
                PlaceStyle placeStyle = extractPlaceStyle(element, iconMap, defaultIconScale, iconRoot);
                theme.getPlaceStyles().add(placeStyle);
            } else if(nodeName.equals(ASSOCIATION_TYPE)){
                AssociationStyle linkStyle = extractAssociationStyle(element);
                theme.getAssociationStyles().add(linkStyle);
            } else if(nodeName.equals(SHAPES)){
                ShapeType shape =  extractBaseShape(element);
                theme.setDefaultShape(shape);
            }else {

                CsiMap<String, String> map = theme.getClientProperties();
                if (map == null) {
                    map = new CsiMap<String, String>();
                }
                 map.put(nodeName, element.getValue());
                //map.put(option.key, option);
            }
            //reader.moveUp();
        }

        return theme;
    }

    private static GraphTheme parseGraph(Element root, Map<String,String> iconMap) {
        GraphTheme theme = new GraphTheme();
        theme.setName(root.getAttributeValue(NAME));
        theme.setUuid(root.getAttributeValue(ID));

        //optset.bgcolor = reader.getAttribute("RGbgcolor");
        @SuppressWarnings("rawtypes")
        Iterator iterator = root.getChildren().iterator();
        float defaultIconScale = 1.0F;
        String iconRoot = "";
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();

            String nodeName = element.getName();
            if (nodeName.equals(COMMENT2)) {
                theme.setRemarks(element.getValue());
                //goes in client props for now
                //            } else if (nodeName.equals("BundleThreshold")) {
                //                //optset.bundleThreshold = element.getValue();
            } else if (nodeName.equals("BundleThreshold")) {
                String bundleThreshold = element.getValue();
                try{
                    Integer value = Integer.parseInt(bundleThreshold);
                    theme.setBundleThreshold(value);
                } catch(Exception e){
                   LOG.error("Non-Integer value for bundlethreshold", e);
                }
            }else if(nodeName.equals(ICON_SCALE)){
                defaultIconScale = Float.parseFloat(element.getValue());
            } else if(nodeName.equals(ICON_ROOT)){
                if(element.getValue() != null) {
                  iconRoot = element.getValue();
               }
            } else if(nodeName.equals(BUNDLE_NODE)){
                NodeStyle bundleStyle = extractNodeStyle(element, iconMap, defaultIconScale, iconRoot);
                if(bundleStyle.getName() == null){
                    bundleStyle.setName("Bundled Node Style");
                }
                theme.getNodeStyles().add(bundleStyle);
                theme.setBundleStyle(bundleStyle);
            } else if(nodeName.equals(NODE_TYPE)){
                NodeStyle nodeStyle = extractNodeStyle(element, iconMap, defaultIconScale, iconRoot);
                theme.getNodeStyles().add(nodeStyle);
            } else if(nodeName.equals(LINK_TYPE)){
                LinkStyle linkStyle = extractLinkStyle(element);
                theme.getLinkStyles().add(linkStyle);
            } else if(nodeName.equals(SHAPES)){
                ShapeType shape =  extractBaseShape(element);
                theme.setDefaultShape(shape);
            }else {

                CsiMap<String, String> map = theme.getClientProperties();
                if (map == null) {
                    map = new CsiMap<String, String>();
                }
                if((nodeName != null) && (element != null)){
                    map.put(nodeName, element.getValue());
                }
                //map.put(option.key, option);
            }
            //reader.moveUp();
        }

        return theme;
    }

    private static ShapeType extractBaseShape(Element element) {

        try{
            List<Element> children = element.getChildren();
            if((children != null) && !children.isEmpty()) {
                Element shapeElement = children.get(0);
                String shape = shapeElement.getValue();
                if(shape != null){
                    if(!shape.equals("none") && !shape.isEmpty()) {
                     return ShapeType.getShape(shape);
                  }
                }

            }

        } catch(Exception exception){
           LOG.error("Failed to parse base shape", exception);
        }
        return null;
    }

    private static LinkStyle extractLinkStyle(Element element) {

        LinkStyle linkStyle = new LinkStyle();
        linkStyle.setColor(extractColor(element));
        linkStyle.setLineStyle(extractLineStyle(element));
        linkStyle.setWidth(extractWidth(element));
        linkStyle.setName(extractLinkName(element));
        linkStyle.getFieldNames().addAll(extractLinkFields(element));
        return linkStyle;
    }

    private static AssociationStyle extractAssociationStyle(Element element) {

        AssociationStyle associationStyle = new AssociationStyle();
        associationStyle.setColor(extractColor(element));
        associationStyle.setLineStyle(extractLineStyle(element));
        associationStyle.setWidth(extractWidth(element));
        associationStyle.setName(extractAssociationName(element));
        associationStyle.getFieldNames().addAll(extractAssociationFields(element));
        return associationStyle;
    }

    private static List <String> extractGraphFields(Element element) {
        List<String> fields = new ArrayList<String>();
        String type = element.getAttributeValue(NODETYPE);
        if(type != null) {
         fields.add(type);
      }

        if (!element.getChildren().isEmpty()) {
            @SuppressWarnings("rawtypes")
            Iterator iterator = element.getChildren().iterator();
            while (iterator.hasNext()) {
                Element child = (Element) iterator.next();
                fields.add(child.getText());
            }
        }

        return fields;
    }

    private static List <String> extractMapFields(Element element) {
        List<String> fields = new ArrayList<String>();
        String type = element.getAttributeValue(PLACETYPE);
        if(type != null) {
         fields.add(type);
      }

        if (!element.getChildren().isEmpty()) {
            @SuppressWarnings("rawtypes")
            Iterator iterator = element.getChildren().iterator();
            while (iterator.hasNext()) {
                Element child = (Element) iterator.next();
                fields.add(child.getText());
            }
        }

        return fields;
    }

    private static List <String> extractLinkFields(Element element) {
        List<String> fields = new ArrayList<String>();
        String type = element.getAttributeValue(LINKTYPE);
        if(type != null) {
         fields.add(type);
      }

        if (!element.getChildren().isEmpty()) {
            @SuppressWarnings("rawtypes")
            Iterator iterator = element.getChildren().iterator();
            while (iterator.hasNext()) {
                Element child = (Element) iterator.next();
                fields.add(child.getText());
            }
        }

        return fields;
    }

    private static List <String> extractAssociationFields(Element element) {
        List<String> fields = new ArrayList<String>();
        String type = element.getAttributeValue(ASSOCIATIONTYPE);
        if(type != null) {
         fields.add(type);
      }

        if (!element.getChildren().isEmpty()) {
            @SuppressWarnings("rawtypes")
            Iterator iterator = element.getChildren().iterator();
            while (iterator.hasNext()) {
                Element child = (Element) iterator.next();
                fields.add(child.getText());
            }
        }

        return fields;
    }

    private static double extractWidth(Element element) {
        String width = element.getAttributeValue(WIDTH);
        try{
            if((width != null) && !width.isEmpty()){
                Double doubleWidth = Double.parseDouble(width);
                return doubleWidth;
            }
        } catch(Exception exception){
           LOG.error("Failed to parse width", exception);
        }

        return 1.0;
    }

    private static LineStyle extractLineStyle(Element element) {
        String style = element.getAttributeValue(STYLE);
        try{
            if((style != null) && !style.isEmpty()) {
               return LineStyle.getLine(style);
            }
        } catch(Exception exception){
           LOG.error("Failed to parse style", exception);
        }
        return LineStyle.SOLID;
    }

    private static NodeStyle extractNodeStyle(Element element, Map<String,String> iconMap, float iconScale, String iconRoot) {
        NodeStyle nodeStyle = new NodeStyle();
        nodeStyle.setIconId(extractIconId(element, iconMap, iconRoot));
        nodeStyle.setShape(extractShape(element));
        nodeStyle.setColor(extractColor(element));
        nodeStyle.setIconScale(extractIconScale(element, iconScale));
        nodeStyle.setName(extractName(element));
        nodeStyle.getFieldNames().addAll(extractGraphFields(element));
        return nodeStyle;
    }

    private static PlaceStyle extractPlaceStyle(Element element, Map<String,String> iconMap, float iconScale, String iconRoot) {
        PlaceStyle placeStyle = new PlaceStyle();
        placeStyle.setIconId(extractIconId(element, iconMap, iconRoot));
        placeStyle.setShape(extractShape(element));
        placeStyle.setColor(extractColor(element));
        placeStyle.setIconScale(extractIconScale(element, iconScale));
        placeStyle.setName(extractPlaceName(element));
        placeStyle.getFieldNames().addAll(extractMapFields(element));
        return placeStyle;
    }


    private static String extractName(Element element) {
        String name = element.getAttributeValue(NAME);
        if(name == null){
            name = element.getAttributeValue(NODETYPE);
        }
        return name;
    }

    private static String extractPlaceName(Element element) {
        String name = element.getAttributeValue(NAME);
        if(name == null){
            name = element.getAttributeValue(PLACETYPE);
        }
        return name;
    }

    private static String extractLinkName(Element element) {
        String name = element.getAttributeValue(NAME);
        if(name == null){
            name = element.getAttributeValue(LINKTYPE);
        }
        return name;
    }

    private static String extractAssociationName(Element element) {
        String name = element.getAttributeValue(NAME);
        if(name == null){
            name = element.getAttributeValue(ASSOCIATIONTYPE);
        }
        return name;
    }

    private static String extractIconId(Element element, Map<String, String> iconMap, String iconRoot) {
        String iconPath = element.getAttributeValue(ICON);
        String icon = null;
        if(iconPath != null){
            if((iconRoot == null) || iconRoot.isEmpty()){
                icon = iconMap.get(iconPath);
            } else {
                icon = iconMap.get(iconRoot + ICON_FILE_SEPERATOR + iconPath);
            }
            if(icon == null){
                if(iconPath.isEmpty()){
                    return null;
                } else {
                    return iconPath;
                }
            }
        }

        return icon;
    }

    private static double extractIconScale(Element element, float iconScale) {
        try{
            String scale = element.getAttributeValue(OVERLAY_SCALE);
            if((scale != null) && !scale.isEmpty()) {
               return Double.parseDouble(scale);
            } else{
                return iconScale;
            }
        } catch(Exception exception){
           LOG.error("Failed to parse overlayScale", exception);
        }
        return 1.0;
    }

    private static ShapeType extractShape(Element element) {
        try{
            String shape = element.getAttributeValue(SHAPE);
            if(shape != null){
                if(!shape.equals("none") && !shape.isEmpty()) {
                  return ShapeType.getShape(shape);
               }
            }
        } catch(Exception exception){
           LOG.error("Failed to parse shape", exception);
        }
        return null;
    }

    private static int extractColor(Element element) {
        try{

            if((element.getAttributeValue(COLOR) != null) && !element.getAttributeValue(COLOR).isEmpty()) {
               return Integer.parseInt(element.getAttributeValue(COLOR));
            }
        } catch(Exception exception){
           LOG.error("Failed to parse color", exception);
        }
        return 1;
    }

    private static boolean hasManagementAccess(boolean securityCheck) {

        if(!securityCheck){
            return true;
        }

        String[] iconManagementRoles = null;
        String accessUsers = Configuration.getInstance().getApplicationConfig().getIconManagementAccess();
        String delimiter = Configuration.getInstance().getApplicationConfig().getIconManagementAccessDelimiter();

        if((delimiter != null) && !delimiter.isEmpty()){
            iconManagementRoles = accessUsers.split(delimiter);
        } else {
            iconManagementRoles = new String[1];
            iconManagementRoles[0] = accessUsers;
        }

        if(iconManagementRoles == null) {
            LOG.warn("No icon management users have been configured for this system. Please check your application-config.");
            return false;
        }

        return CsiSecurityManager.hasAnyRole(iconManagementRoles);
    }

    private static String oldSaveImage(InputStream in, String extension, String name, String tagName,
                                       Map<String,String> iconMap, boolean iconUploadAccess) {
       if (name != null) {
          String[] nameParts = name.split("/");

          try {
             String iconName = null;
             String oldIconUuid = null;

             // We need to test if this icon uuid exists already
             try {
                String[] iconInfo = name.split("\\.");
                Icon icon = testName(iconInfo[0]);

                if (icon != null) {
                   iconMap.put(icon.getUuid(), icon.getUuid());
                   return icon.getUuid();
                } else {
                   // Check if it's a UUID from another machine
                   oldIconUuid = iconInfo[0];
                   if (iconInfo.length > 2) {
                      iconName = iconInfo[iconInfo.length - 1];
                   } else {
                      oldIconUuid = null;
                   }
                   if ((iconName != null) && iconName.equals(NULL_STRING)) {
                      iconName = null;
                   }
                   if ((oldIconUuid != null) && (oldIconUuid.isEmpty() || oldIconUuid.equals(NULL_STRING))) {
                      oldIconUuid = null;
                   }
                }
             } catch (Exception e) {
                // No-op continue
             }
             if (!iconUploadAccess) {
                LOG.warn("User not Authorized to upload icons. " + name + " will be skipped.");
                return null;
             }
             BufferedImage image = ImageIO.read(in);
             String result;

             try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, extension, baos);
                // if(baos.toByteArray().length == 0){
                // return null;
                // }
                result = Base64.getEncoder().encodeToString(baos.toByteArray());
             }
             if ((result == null) || (result.length() == 0)) {
                LOG.warn("No image data found for " + name + ", canceling upload");
                return null;
             }
             result = "data:image/" + extension + ";base64," + result;
             Set<String> tags = new HashSet<String>();

             if ((iconName == null) || iconName.isEmpty()) {
                iconName = (nameParts[nameParts.length - 1]);

                if (tagName != null) {
                   tags.add(tagName);
                }
                for (int ii = 0; ii < (nameParts.length - 1); ii++) {
                   tags.add(nameParts[ii]);
                }
             }
             Icon icon = oldSaveIcon(tags, iconName, result);

             // if this is another system's icon we use that in the map
             if (oldIconUuid != null) {
                iconMap.put(oldIconUuid, icon.getUuid());
             } else {
                iconMap.put(name, icon.getUuid());
             }
             return icon.getUuid();
          } catch (IOException e) {
             LOG.error("Failed to create image from zip file");
          }
       }
       return null;
    }

    private static Icon oldSaveIcon(Set<String> tags,String iconName, String result) {
        Icon icon = new Icon();

        icon.setName(iconName);
        icon.setImage(result);
        if ((null != tags) && !tags.isEmpty()) {

            icon.getTags().addAll(tags);
        }
        if((result.length() * IconActionsService.BASE64_SIZE_RATIO) > IconActionsService.MAX_IMAGE_SIZE){
           LOG.error("File (" + icon.getName() + ") is too large to upload, must be less than " + IconActionsService.MAX_IMAGE_SIZE);
        } else {
            CsiPersistenceManager.persist(icon);
        }
        return icon;
    }

    private static Icon testName(String uuid) {
        // TODO Auto-generated method stub
        return CsiPersistenceManager.findObject(Icon.class, uuid);
    }
}
