package csi.server.business.helper.theme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import csi.security.ACL;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.helper.DeepCloner;
import csi.server.business.service.icon.IconActionsService;
import csi.server.common.dto.resource.ImportItem;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.enumerations.LineStyle;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.Resource;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.VisualItemStyle;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 4/3/2019.
 */
public class ThemeHelper extends AbstractThemeHelper {
   private static final Logger LOG = LogManager.getLogger(ThemeHelper.class);

    public static Document createGraphDocument(GraphTheme themeIn, Map<String, String> iconsToExportIn){

        Document myDocument = null;

        if (null != themeIn) {

            Element myRoot = new Element(GRAPH_THEME);
            Element myOptions = new Element(OPTION_SET);
            String myUuid = themeIn.getUuid();
            String myName = themeIn.getName();
            String myRemarks = themeIn.getRemarks();
            String myOwner = themeIn.getOwner();
            List<NodeStyle> myNodeStyleList = themeIn.getNodeStyles();
            List<LinkStyle> myLinkStyleList = themeIn.getLinkStyles();

            myDocument = new Document();
            myDocument.setRootElement(myRoot);
            myOptions.setAttribute(NAME, (null != myName) ? myName : "");
            myOptions.setAttribute(ID, (null != myUuid) ? myUuid : "");
            myOptions.setAttribute(REMARKS, (null != myRemarks) ? myRemarks : "");
            myOptions.setAttribute(OWNER, (null != myOwner) ? myOwner : "");
            myRoot.getChildren().add(myOptions);

            if (null != themeIn.getDefaultShape()) {

                Element myShape = new Element(SHAPE);

                myShape.setText(themeIn.getDefaultShape().name());
                myRoot.getChildren().add(myShape);
            }
            if (null != themeIn.getBundleThreshold()) {

                Element myThreshold = new Element(THRESHOLD);

                myThreshold.setText(themeIn.getBundleThreshold().toString());
                myRoot.getChildren().add(myThreshold);
            }
            if (null != themeIn.getBundleStyle()) {

                Element myThreshold = createNodeStyle(BUNDLE_STYLE, themeIn.getBundleStyle(), iconsToExportIn);

                if (null != myThreshold) {

                    myRoot.getChildren().add(myThreshold);
                }
            }
            if ((null != myNodeStyleList) && !myNodeStyleList.isEmpty()) {

                Element myListElement = new Element(NODE_STYLE_LIST);

                for (NodeStyle myNodeStyle : myNodeStyleList) {

                    Element myNodeStyleElement = createNodeStyle(NODE_STYLE, myNodeStyle, iconsToExportIn);

                    if (null != myNodeStyleElement) {

                        myListElement.getChildren().add(myNodeStyleElement);
                    }
                }
                myRoot.getChildren().add(myListElement);
            }
            if ((null != myLinkStyleList) && !myLinkStyleList.isEmpty()) {

                Element myListElement = new Element(LINK_STYLE_LIST);

                for (LinkStyle myLinkStyle : myLinkStyleList) {

                    Element myLinkStyleElement = createLinkStyle(myLinkStyle);

                    if (null != myLinkStyleElement) {

                        myListElement.getChildren().add(myLinkStyleElement);
                    }
                }
                myRoot.getChildren().add(myListElement);
            }
        }
        return myDocument;
    }

    public static Document createMapDocument(MapTheme themeIn, Map<String, String> iconsToExportIn) {

        Document myDocument = null;

        if (null != themeIn) {

            Element myRoot = new Element(MAP_THEME);
            Element myOptions = new Element(OPTION_SET);
            String myUuid = themeIn.getUuid();
            String myName = themeIn.getName();
            String myRemarks = themeIn.getRemarks();
            String myOwner = themeIn.getOwner();
            List<PlaceStyle> myPlaceStyleList = themeIn.getPlaceStyles();
            List<AssociationStyle> myAssociationStyleList = themeIn.getAssociationStyles();

            myDocument = new Document();
            myDocument.setRootElement(myRoot);
            myOptions.setAttribute(NAME, (null != myName) ? myName : "");
            myOptions.setAttribute(ID, (null != myUuid) ? myUuid : "");
            myOptions.setAttribute(REMARKS, (null != myRemarks) ? myRemarks : "");
            myOptions.setAttribute(OWNER, (null != myOwner) ? myOwner : "");
            myRoot.getChildren().add(myOptions);

            if (null != themeIn.getDefaultShape()) {

                Element myShape = new Element(SHAPE);

                myShape.setText(themeIn.getDefaultShape().name());
                myRoot.getChildren().add(myShape);
            }
            if (null != themeIn.getBundleStyle()) {

                Element myThreshold = createPlaceStyle(BUNDLE_STYLE, themeIn.getBundleStyle(), iconsToExportIn);

                if (null != myThreshold) {

                    myRoot.getChildren().add(myThreshold);
                }
            }
            if ((null != myPlaceStyleList) && !myPlaceStyleList.isEmpty()) {

                Element myListElement = new Element(PLACE_STYLE_LIST);

                for (PlaceStyle myPlaceStyle : myPlaceStyleList) {

                    Element myPlaceStyleElement = createPlaceStyle(PLACE_STYLE, myPlaceStyle, iconsToExportIn);

                    if (null != myPlaceStyleElement) {

                        myListElement.getChildren().add(myPlaceStyleElement);
                    }
                }
                myRoot.getChildren().add(myListElement);
            }
            if ((null != myAssociationStyleList) && !myAssociationStyleList.isEmpty()) {

                Element myListElement = new Element(ASSOCIATION_STYLE_LIST);

                for (AssociationStyle myAssociationStyle : myAssociationStyleList) {

                    Element myAssociationStyleElement = createAssociationStyle(myAssociationStyle);

                    if (null != myAssociationStyleElement) {

                        myListElement.getChildren().add(myAssociationStyleElement);
                    }
                }
                myRoot.getChildren().add(myListElement);
            }
        }
        return myDocument;
    }

    public static Document createIconDocument(IconActionsService serviceIn, Collection<String> iconsToExportIn) {

        Document myDocument = new Document();
        Element myRoot = new Element(ICON_LIST);

        myDocument.setRootElement(myRoot);
        for (String myEntry : iconsToExportIn) {

            try {

                Icon myIcon = serviceIn.getIcon(myEntry);

                if (null != myIcon) {

                    createIconElement(myRoot, myIcon);
                }

            } catch (Exception myException) {

               LOG.error("Caught exception exporting icons", myException);
            }
        }
        return myDocument;
    }

    public static Document createBasemapDocument(Basemap basemapIn) {

        Document myDocument = null;

        if (null != basemapIn) {

            Element myRoot = new Element(BASEMAP);
            Element myOptions = new Element(OPTION_SET);
            String myUuid = basemapIn.getUuid();
            String myName = basemapIn.getName();
            String myRemarks = basemapIn.getRemarks();
            String myOwner = basemapIn.getOwner();
            String myUrl = basemapIn.getUrl();
            String myType = basemapIn.getType();
            String myLayername = basemapIn.getLayername();

            myDocument = new Document();
            myDocument.setRootElement(myRoot);
            myOptions.setAttribute(NAME, (null != myName) ? myName : "");
            myOptions.setAttribute(ID, (null != myUuid) ? myUuid : "");
            myOptions.setAttribute(REMARKS, (null != myRemarks) ? myRemarks : "");
            myOptions.setAttribute(OWNER, (null != myOwner) ? myOwner : "");
            myRoot.getChildren().add(myOptions);
            if (null != myUrl) {

                Element myUrlElement = new Element("Url");
                myUrlElement.setText(myUrl);
                myRoot.getChildren().add(myUrlElement);
            }
            if (null != myType) {

                Element myTypeElement = new Element("Type");
                myTypeElement.setText(myType);
                myRoot.getChildren().add(myTypeElement);
            }
            if (null != myLayername) {

                Element myLayernameElement = new Element("LayerName");
                myLayernameElement.setText(myLayername);
                myRoot.getChildren().add(myLayernameElement);
            }
        }
        return myDocument;
    }

    public static String importTheme(Element elementIn, ImportItem itemIn)  {

        Theme myNewTheme = null;
        String myMessage = null;
        boolean myGraphFlag = false;

        if (null != elementIn) {

            if (GRAPH_THEME.equals(elementIn.getName())) {

                myGraphFlag = true;

            } else if (MAP_THEME.equals(elementIn.getName())) {

                myGraphFlag = false;

            } else {

                myMessage = "Root element not recognized by theme importing software.";
            }

        } else {

            myMessage = "Theme importing software rejecting empty request.";
        }
        if (null == myMessage) {

            try {

                Element myOptions = elementIn.getChild(OPTION_SET);

                if (null != myOptions) {

                    String myName = itemIn.getName();
                    String myUuid = (null != myOptions) ? myOptions.getAttributeValue(ID) : null;
                    String myRemarks = (null != myOptions) ? myOptions.getAttributeValue(REMARKS) : null;
                    String myOwner = CsiSecurityManager.isAdmin() ? itemIn.getOwner() : null;

                    if ((null != myUuid) && (0 < myUuid.length())) {

                        String childText = elementIn.getChildText(SHAPE);
                        ShapeType myShapeType = getShapeType(childText);

                        if (myGraphFlag) {

                            String myThresholdString = elementIn.getChildText(THRESHOLD);
                            Integer myBundleThreshold = (null != myThresholdString) ? Integer.decode(myThresholdString) : null;
                            NodeStyle myBundleStyle = importNodeStyle(elementIn.getChild(BUNDLE_STYLE));
                            Element myNodeElementList = elementIn.getChild(NODE_STYLE_LIST);
                            List<Element> myNodeElements = (null != myNodeElementList) ? myNodeElementList.getChildren(NODE_STYLE) : null;
                            Element myLinkElementList = elementIn.getChild(LINK_STYLE_LIST);
                            List<Element> myLinkElements = (null != myLinkElementList) ? myLinkElementList.getChildren(LINK_STYLE) : null;
                            List<NodeStyle> myNodeStyleList = null;
                            List<LinkStyle> myLinkStyleList = null;

                            myNodeStyleList = new ArrayList<NodeStyle>();
                            if ((null != myNodeElements) && !myNodeElements.isEmpty()) {

                                for (Element myElement : myNodeElements) {

                                    NodeStyle myNodeStyle = importNodeStyle(myElement);

                                    if (null != myNodeStyle) {

                                        myNodeStyleList.add(myNodeStyle);
                                    }
                                }
                            }
                            myLinkStyleList = new ArrayList<LinkStyle>();
                            if ((null != myLinkElements) && !myLinkElements.isEmpty()) {

                                for (Element myElement : myLinkElements) {

                                    LinkStyle myLinkStyle = importLinkStyle(myElement);

                                    if (null != myLinkStyle) {

                                        myLinkStyleList.add(myLinkStyle);
                                    }
                                }
                            }
                            myNewTheme = new GraphTheme(myUuid, myName, myRemarks, myOwner, myShapeType,
                                                        myBundleThreshold, myBundleStyle, myNodeStyleList,
                                                        myLinkStyleList);

                        } else {

                            PlaceStyle myBundleStyle = importPlaceStyle(elementIn.getChild(BUNDLE_STYLE));
                            Element myPlaceElementList = elementIn.getChild(PLACE_STYLE_LIST);
                            List<Element> myPlaceElements = (null != myPlaceElementList) ? myPlaceElementList.getChildren(PLACE_STYLE) : null;
                            Element myAssociationElementList = elementIn.getChild(ASSOCIATION_STYLE_LIST);
                            List<Element> myAssociationElements = (null != myAssociationElementList) ? myAssociationElementList.getChildren(ASSOCIATION_STYLE) : null;
                            List<PlaceStyle> myPlaceStyleList = null;
                            List<AssociationStyle> myAssociationStyleList = null;

                            myPlaceStyleList = new ArrayList<PlaceStyle>();
                            if ((null != myPlaceElements) && !myPlaceElements.isEmpty()) {

                                for (Element myElement : myPlaceElements) {

                                    PlaceStyle myPlaceStyle = importPlaceStyle(myElement);

                                    if (null != myPlaceStyle) {

                                        myPlaceStyleList.add(myPlaceStyle);
                                    }
                                }
                            }
                            myAssociationStyleList = new ArrayList<AssociationStyle>();
                            if ((null != myAssociationElements) && !myAssociationElements.isEmpty()) {

                                for (Element myElement : myAssociationElements) {

                                    AssociationStyle myAssociationStyle = importAssociationStyle(myElement);

                                    if (null != myAssociationStyle) {

                                        myAssociationStyleList.add(myAssociationStyle);
                                    }
                                }
                            }
                            myNewTheme = new MapTheme(myUuid, myName, myRemarks, myOwner, myShapeType,
                                                        myBundleStyle, myPlaceStyleList, myAssociationStyleList);
                        }
                    }
                }

            } catch (Exception myException) {

                myMessage = "Caught exception importing theme " + Format.value(myException) + ".";
            }
        }
        if (null == myMessage) {

            myMessage = processConflictResolution(myNewTheme, itemIn);
        }
        if (null != myMessage) {

           LOG.error(myMessage);
        }
        return myMessage;
    }

   private static ShapeType getShapeType(String attributeValue) {
      return (attributeValue == null) ? null : ShapeType.valueOf(attributeValue);
   }

   public static String importIcons(Element elementIn)  {
      String errorString = null;

      if (elementIn.getName().equals(ICON_LIST)) {
         List<Element> iconElements = elementIn.getChildren();

         if (iconElements != null) {
            for (Element element : iconElements) {
               Icon newIcon = importIcon(element);

               if (newIcon != null) {
                  Icon oldIcon = CsiPersistenceManager.findObjectAvoidingSecurity(Icon.class, newIcon.getUuid());

                  if (oldIcon != null) {
                     boolean changeFlag = false;
                     Set<String> oldTags = oldIcon.getTags();
                     Set<String> newTags = newIcon.getTags();
                     int oldCount = (oldTags != null) ? oldTags.size() : 0;

                     if ((newTags != null) && !newTags.isEmpty()) {
                        oldIcon.getTags().addAll(newTags);
                        changeFlag = (oldCount != oldIcon.getTags().size());
                     }
                     if (changeFlag) {
                        CsiPersistenceManager.merge(oldIcon);
                     }
                  } else {
                     CsiPersistenceManager.persist(newIcon);
                  }
               }
            }
         }
      }
      return errorString;
   }

    private static void mergeThemes(Theme keeperIn, Theme updaterIn) {

        if (keeperIn instanceof  GraphTheme) {

            mergeStyles(((GraphTheme)keeperIn).getNodeStyles(), ((GraphTheme)updaterIn).getNodeStyles());
            mergeStyles(((GraphTheme)keeperIn).getLinkStyles(), ((GraphTheme)updaterIn).getLinkStyles());

        } else if (keeperIn instanceof  MapTheme) {

            mergeStyles(((MapTheme)keeperIn).getPlaceStyles(), ((MapTheme)updaterIn).getPlaceStyles());
            mergeStyles(((MapTheme)keeperIn).getAssociationStyles(), ((MapTheme)updaterIn).getAssociationStyles());
        }
    }

    private static <T extends VisualItemStyle> void mergeStyles(List<T> keeperIn, List<T> updaterIn) {

        Map<String, T> myObjectMap = new TreeMap<>();
        Map<String, T> myStyleMap = new TreeMap<>();

        for (T myStyle : keeperIn) {

            List<String> myList = myStyle.getFieldNames();

            myStyleMap.put(myStyle.genKey(), myStyle);
            for (String myItem : myList) {

                myObjectMap.put(myItem, myStyle);
            }
        }
        for (T myStyle : updaterIn) {

            List<String> myList = myStyle.getFieldNames();

            for (String myItem : myList) {

                if (!myObjectMap.containsKey(myItem)) {

                    String myKey = myStyle.genKey();
                    T myKeeper = myStyleMap.get(myKey);

                    if (null == myKeeper) {

                        myKeeper = (T)myStyle.genEmpty();
                        keeperIn.add(myKeeper);
                        myStyleMap.put(myKey, myKeeper);
                    }
                    myKeeper.addName(myItem);
                    myObjectMap.put(myItem, myKeeper);
                }
            }
        }
    }

    private static Element createNodeStyle(String nameIn, NodeStyle styleIn, Map<String, String> iconsToExportIn) {

        Element myElement = null;

        if ((null != styleIn) && (null != nameIn)) {

            String myName = styleIn.getName();
            String myIconId = styleIn.getIconId();
            Integer myColor = styleIn.getColor();
            ShapeType myShape = styleIn.getShape();
            Double myIconScale = styleIn.getIconScale();
            List<String> myFieldNames = styleIn.getFieldNames();

            myElement = new Element(nameIn);
            if ((null != myIconId) && (0 < myIconId.length())) {

                myElement.setAttribute(ICON, myIconId);
                if (null != iconsToExportIn) {

                    iconsToExportIn.put(myIconId, myIconId);
                }
            }
            if (null != myName) {

                myElement.setAttribute(NAME, myName);
            }
            if (null != myColor) {

                myElement.setAttribute(COLOR, myColor.toString());
            }
            if (null != myShape) {

                myElement.setAttribute(SHAPE, myShape.name());
            }
            if (null != myIconScale) {

                myElement.setAttribute(OVERLAY_SCALE, myIconScale.toString());
            }
            if ((null != myFieldNames) && !myFieldNames.isEmpty()) {

                Element myItemList = new Element(ITEM_LIST);

                myElement.getChildren().add(myItemList);
                for (String myItem : myFieldNames) {

                    Element myItemElement = new Element(ITEM);
                    myItemElement.setAttribute(VALUE, myItem);
                    myItemList.getChildren().add(myItemElement);
                }
            }
        }
        return myElement;
    }

    private static Element createLinkStyle(LinkStyle styleIn) {

        Element myElement = null;

        if (null != styleIn) {

            String myName = styleIn.getName();
            Integer myColor = styleIn.getColor();
            Double myWidth = styleIn.getWidth();
            LineStyle myLineStyle = styleIn.getLineStyle();
            List<String> myFieldNames = styleIn.getFieldNames();

            myElement = new Element(LINK_STYLE);
            if (null != myName) {

                myElement.setAttribute(NAME, myName);
            }
            if (null != myColor) {

                myElement.setAttribute(COLOR, myColor.toString());
            }
            if (null != myWidth) {

                myElement.setAttribute(WIDTH, myWidth.toString());
            }
            if (null != myLineStyle) {

                myElement.setAttribute(STYLE, myLineStyle.name());
            }
            if ((null != myFieldNames) && !myFieldNames.isEmpty()) {

                Element myItemList = new Element(ITEM_LIST);

                myElement.getChildren().add(myItemList);
                for (String myItem : myFieldNames) {

                    Element myItemElement = new Element(ITEM);
                    myItemElement.setAttribute(VALUE, myItem);
                    myItemList.getChildren().add(myItemElement);
                }
            }
        }
        return myElement;
    }

    private static Element createPlaceStyle(String nameIn, PlaceStyle styleIn, Map<String, String> iconsToExportIn) {

        Element myElement = null;

        if ((null != styleIn) && (null != nameIn)) {

            String myName = styleIn.getName();
            String myIconId = styleIn.getIconId();
            Integer myColor = styleIn.getColor();
            ShapeType myShape = styleIn.getShape();
            Double myIconScale = styleIn.getIconScale();
            List<String> myFieldNames = styleIn.getFieldNames();

            myElement = new Element(nameIn);
            if ((null != myIconId) && (0 < myIconId.length())) {

                myElement.setAttribute(ICON, myIconId);
                if (null != iconsToExportIn) {

                    iconsToExportIn.put(myIconId, myIconId);
                }
            }
            if (null != myName) {

                myElement.setAttribute(NAME, myName);
            }
            if (null != myColor) {

                myElement.setAttribute(COLOR, myColor.toString());
            }
            if (null != myShape) {

                myElement.setAttribute(SHAPE, myShape.name());
            }
            if (null != myIconScale) {

                myElement.setAttribute(OVERLAY_SCALE, myIconScale.toString());
            }
            if ((null != myFieldNames) && !myFieldNames.isEmpty()) {

                Element myItemList = new Element(ITEM_LIST);

                myElement.getChildren().add(myItemList);
                for (String myItem : myFieldNames) {

                    Element myItemElement = new Element(ITEM);
                    myItemElement.setAttribute(VALUE, myItem);
                    myItemList.getChildren().add(myItemElement);
                }
            }
        }
        return myElement;
    }

    private static Element createAssociationStyle(AssociationStyle styleIn) {

        Element myElement = null;

        if (null != styleIn) {

            String myName = styleIn.getName();
            Integer myColor = styleIn.getColor();
            Double myWidth = styleIn.getWidth();
            LineStyle myLineStyle = styleIn.getLineStyle();
            List<String> myFieldNames = styleIn.getFieldNames();

            myElement = new Element(LINK_STYLE);
            if (null != myName) {

                myElement.setAttribute(NAME, myName);
            }
            if (null != myColor) {

                myElement.setAttribute(COLOR, myColor.toString());
            }
            if (null != myWidth) {

                myElement.setAttribute(WIDTH, myWidth.toString());
            }
            if (null != myLineStyle) {

                myElement.setAttribute(STYLE, myLineStyle.name());
            }
            if ((null != myFieldNames) && !myFieldNames.isEmpty()) {

                Element myItemList = new Element(ITEM_LIST);

                myElement.getChildren().add(myItemList);
                for (String myItem : myFieldNames) {

                    Element myItemElement = new Element(ITEM);
                    myItemElement.setAttribute(VALUE, myItem);
                    myItemList.getChildren().add(myItemElement);
                }
            }
        }
        return myElement;
    }

    private static void createIconElement(Element rootIn, Icon iconIn) {

        if ((null != iconIn) && (null != rootIn)) {

            String myUuid = iconIn.getUuid();
            String myName = iconIn.getName();
            String myRemarks = iconIn.getRemarks();
            String myOwner = iconIn.getOwner();
            String myImageValue = iconIn.getImage();
            Set<String> myTagList = iconIn.getTags();
            Element myRoot = new Element(ICON);
            Element myOptions = new Element(OPTION_SET);
            Element myImage = new Element(IMAGE);
            Element myTags = new Element(TAG_LIST);

            myOptions.setAttribute(NAME, (null != myName) ? myName : "");
            myOptions.setAttribute(ID, (null != myUuid) ? myUuid : "");
            myOptions.setAttribute(REMARKS, (null != myRemarks) ? myRemarks : "");
            myOptions.setAttribute(OWNER, (null != myOwner) ? myOwner : "");
            myRoot.getChildren().add(myOptions);
            if (null != myImageValue) {

                myImage.setText(myImageValue);
            }
            myRoot.getChildren().add(myImage);
            if (myTagList != null) {

                for (String myTagValue : myTagList) {

                    Element myTag = new Element(TAG);

                    myTag.setText(myTagValue);
                    myTags.getChildren().add(myTag);
                }
            }
            myRoot.getChildren().add(myTags);
            rootIn.getChildren().add(myRoot);
        }
    }

    private static Icon importIcon(Element elementIn)  {

        Icon myIcon = null;

        if ((null != elementIn) && (elementIn.getName().equals(ICON))) {

            String myImage = elementIn.getChildText(IMAGE);
            Element myTagRoot = elementIn.getChild(TAG_LIST);
            Element myOptions = elementIn.getChild(OPTION_SET);

            if ((null != myOptions) && (null != myImage) && (0 < myImage.length())) {

                String myName = myOptions.getAttributeValue(NAME);
                String myUuid =  myOptions.getAttributeValue(ID);

                if ((null != myUuid) && (0 < myUuid.length())) {

                    Set<String> myTagSet = null;

                    if (null != myTagRoot) {

                        List<Element> myElementList = myTagRoot.getChildren(TAG);

                        if ((null != myElementList) && !myElementList.isEmpty()) {

                            myTagSet = new TreeSet<String>();

                            for (Element myElement : myElementList) {

                                String myTag = myElement.getText();

                                if ((null != myTag) && (0 < myTag.length())) {

                                    myTagSet.add(myTag);
                                }
                            }
                        }
                    }
                    myIcon = new Icon(myUuid, myName, myImage, myTagSet);
                }
            }
        }
        return myIcon;
    }

    public static String importBasemap(Element elementIn, ImportItem itemIn) {

        Basemap myNewMap = null;
        String myMessage = null;

        if (null != elementIn) {

            if (!BASEMAP.equals(elementIn.getName())) {

                myMessage = "Root element not recognized by basemap importing software.";
            }

        } else {

            myMessage = "Basemap importing software rejecting empty request.";
        }
        if (null == myMessage) {

            try {

                Element myOptions = elementIn.getChild(OPTION_SET);
                Element myUrlElement = elementIn.getChild("Url");
                Element myTypeElement = elementIn.getChild("Type");
                Element myLayerNameElement = elementIn.getChild("LayerName");
                String myUrl = (null != myUrlElement) ? myUrlElement.getText() : null;
                String myType = (null != myTypeElement) ? myTypeElement.getText() : null;
                String myLayerName = (null != myLayerNameElement) ? myLayerNameElement.getText() : null;
                String myName = itemIn.getName();
                String myUuid = (null != myOptions) ? myOptions.getAttributeValue(ID) : null;
                String myRemarks = (null != myOptions) ? myOptions.getAttributeValue(REMARKS) : null;
                String myOwner = CsiSecurityManager.isAdmin() ? itemIn.getOwner() : null;

                myNewMap = new Basemap(myUuid, myName, myRemarks, myOwner, myUrl, myType, myLayerName);

            } catch (Exception myException) {

                myMessage = "Caught exception importing theme " + Format.value(myException) + ".";
            }
        }
        if (null == myMessage) {

            myMessage = processConflictResolution(myNewMap, itemIn);
        }
        if (null != myMessage) {

           LOG.error(myMessage);
        }
        return myMessage;
    }

    public static String processConflictResolution(Resource resourceIn, ImportItem itemIn) {

        String myMessage = null;

        try {

            AclResourceType myType = resourceIn.getResourceType();
            boolean myThemeFlag = ((AclResourceType.GRAPH_THEME == myType)
                                    || (AclResourceType.MAP_THEME == myType));
            String myUuid = itemIn.getUuid();
            String myOwner = itemIn.getOwner();
            String myName = itemIn.getName();
            String myRemarks = itemIn.getRemarks();
            Resource myNameConflict = myThemeFlag
                                        ? null
                                        : AclRequest.getOwnedResourceByNameAvoidingSecurity(myName, myOwner, myType);
            Resource myUuidConflict = CsiPersistenceManager.findObjectAvoidingSecurity(resourceIn.getClass(), myUuid);
            ACL myAcl = null;

            resourceIn.setUuid(myUuid);
            resourceIn.setOwner(myOwner);
            resourceIn.setName(myName);

            if (null != myRemarks) {

                resourceIn.setRemarks(myRemarks);
            }
            if (null != myUuidConflict) {

                ConflictResolution myControll = itemIn.getControl();
                ConflictResolution myResolution = (null != myControll) ? myControll : ConflictResolution.IMPORT_NEW;

                if ((null != myNameConflict) && (ConflictResolution.IMPORT_NEW != myResolution)
                        && !myNameConflict.getUuid().equals(myUuidConflict.getUuid())) {

                    CsiPersistenceManager.deleteObject(myNameConflict);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                    myNameConflict = null;
                }

                switch (myResolution) {

                    case IMPORT_NEW:

                        // Remove resource with conflicting name if still present
                        if (null != myNameConflict) {

                            // Save ACL for replacement resource
                            myAcl = AclRequest.captureAcl(myNameConflict.getUuid());
                            // Replace resource with conflicting name by grabbing its uuid and deleting it
                            resourceIn.setUuid(CsiUUID.formatId(myNameConflict.getUuid(), resourceIn.getResourceType()));
                            CsiPersistenceManager.deleteObject(myNameConflict);
                            CsiPersistenceManager.commit();
                            CsiPersistenceManager.begin();

                        } else {

                            resourceIn.setUuid(CsiUUID.formatId(CsiUUID.randomUUID(), myType));
                        }
                        // Persist the new resource with a different UUID
                        CsiPersistenceManager.persist(resourceIn);
                        break;

                    case SAVE_CURRENT:

                        // Save ACL for replacement resource
                        ACL myOldAcl = AclRequest.captureAcl(myUuid);
                        myAcl = myOldAcl.clone();
                        // Create a backup of the current resource
                        Resource myBackup = DeepCloner.clone(myUuidConflict, DeepCloner.CloneType.NEW_ID);
                        Date myCreateDate = myBackup.getCreateDate();

                        // Delete the current resource and commit before saving the new resource with the same UUID
                        CsiPersistenceManager.deleteObject(myUuidConflict);
                        CsiPersistenceManager.commit();
                        CsiPersistenceManager.begin();

                        // Persist the backup of the current resource
                        myBackup.setUuid(CsiUUID.formatId(myBackup.getUuid(), myType));
                        myBackup.setName(extendName(myType, myBackup.getName() + " BACKUP", myBackup.getOwner()));
                        CsiPersistenceManager.persist(myBackup);
                        myBackup.setCreateDate(myCreateDate);
                        CsiPersistenceManager.merge(myBackup);
                        AclRequest.replaceAcl(myBackup.getUuid(), myOldAcl);

                        // Persist the new resource as a full replacement
                        CsiPersistenceManager.persist(resourceIn);

                    case MERGE_KEEP:

                        if (myThemeFlag) {

                            mergeThemes((Theme)myUuidConflict, (Theme)resourceIn);
                            CsiPersistenceManager.merge(myUuidConflict);
                        }
                        break;

                    case MERGE_REPLACE:

                        if (myThemeFlag) {

                            mergeThemes((Theme)resourceIn, (Theme)myUuidConflict);
                        }

                    case REPLACE:

                        // Save ACL for replacement resource
                        myAcl = AclRequest.captureAcl(myUuid);
                        // Delete the current resource and commit before saving the new resource with the same UUID
                        CsiPersistenceManager.deleteObject(myUuidConflict);
                        CsiPersistenceManager.commit();
                        CsiPersistenceManager.begin();
                        // Persist the new resource as a full replacement
                        CsiPersistenceManager.persist(resourceIn);
                        break;
                }

            } else {

                if (null != myNameConflict) {

                    // Save ACL for replacement resource
                    myAcl = AclRequest.captureAcl(myNameConflict.getUuid());
                    // Replace resource with conflicting name by grabbing its uuid and deleting it
                    resourceIn.setUuid(CsiUUID.formatId(myNameConflict.getUuid(), resourceIn.getResourceType()));
                    CsiPersistenceManager.deleteObject(myNameConflict);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                }
                CsiPersistenceManager.persist(resourceIn);
            }
            if (null != myAcl) {
                CsiPersistenceManager.commit();
                CsiPersistenceManager.begin();
                myAcl.setOwner(myOwner);
                AclRequest.replaceAcl(resourceIn.getUuid(), myAcl);
            }

        } catch (Exception myException) {

            myMessage = "Caught exception processing imported theme " + Format.value(myException) + ".";
        }
        return myMessage;
    }

    private static String extendName(AclResourceType typeIn, String baseNameIn, String ownerIn) {

        String myName = baseNameIn;
        Integer myCounter = 0;

        while (AclRequest.checkConflictAvoidingSecurity(myName, ownerIn, typeIn)) {

            myName = baseNameIn + " (" + myCounter.toString() + ")";
        }
        return myName;
    }

    private static NodeStyle importNodeStyle(Element elementIn) {

        NodeStyle myNodeStyle = null;

        if (null != elementIn) {

            String myName = elementIn.getAttributeValue(NAME);
            String myIconId = elementIn.getAttributeValue(ICON);
            String myColorString = elementIn.getAttributeValue(COLOR);
            Integer myColor = (null != myColorString) ? Integer.decode(myColorString) : null;
            String attributeValue = elementIn.getAttributeValue(SHAPE);
            ShapeType myShape = getShapeType(attributeValue);
            String myScaleString = elementIn.getAttributeValue(OVERLAY_SCALE);
            Double myIconScale = (null != myScaleString) ? Double.valueOf(myScaleString) : null;
            List<String> myFieldList = null;
            Element myItemElementList = elementIn.getChild(ITEM_LIST);
            List<Element> myItemElements = (null != myItemElementList) ? myItemElementList.getChildren(ITEM) : null;

            if ((null != myItemElements) && !myItemElements.isEmpty()) {

                myFieldList = new ArrayList<String>();
                for (Element myItemElement : myItemElements) {

                    String myItem = myItemElement.getAttributeValue(VALUE);

                    if ((null != myItem) && (0 < myItem.length())) {

                        myFieldList.add(myItem);
                    }
                }
            }
            myNodeStyle = new NodeStyle(myName, myIconId, myColor, myShape, myIconScale, myFieldList);
        }
        return myNodeStyle;
    }

    private static LinkStyle importLinkStyle(Element elementIn) {

        LinkStyle myLinkStyle = null;

        if (null != elementIn) {

            String myName = elementIn.getAttributeValue(NAME);
            String myColorString = elementIn.getAttributeValue(COLOR);
            Integer myColor = (null != myColorString) ? Integer.decode(myColorString) : null;
            String myWidthString = elementIn.getAttributeValue(WIDTH);
            Double myWidth = (null != myWidthString) ? Double.valueOf(myWidthString) : null;
            LineStyle myLineStyle = LineStyle.getLine(elementIn.getAttributeValue(STYLE));
            List<String> myFieldList = null;
            Element myItemElementList = elementIn.getChild(ITEM_LIST);
            List<Element> myItemElements = (null != myItemElementList) ? myItemElementList.getChildren(ITEM) : null;

            if ((null != myItemElements) && !myItemElements.isEmpty()) {

                myFieldList = new ArrayList<String>();
                for (Element myItemElement : myItemElements) {

                    String myItem = myItemElement.getAttributeValue(VALUE);

                    if ((null != myItem) && (0 < myItem.length())) {

                        myFieldList.add(myItem);
                    }
                }
            }
            myLinkStyle = new LinkStyle(myName, myColor, myWidth, myLineStyle, myFieldList);
        }
        return myLinkStyle;
    }

    private static PlaceStyle importPlaceStyle(Element elementIn) {

        PlaceStyle myPlaceStyle = null;

        if (null != elementIn) {

            String myName = elementIn.getAttributeValue(NAME);
            String myIconId = elementIn.getAttributeValue(ICON);
            String myColorString = elementIn.getAttributeValue(COLOR);
            Integer myColor = (null != myColorString) ? Integer.decode(myColorString) : null;
            String attributeValue = elementIn.getAttributeValue(SHAPE);
            ShapeType myShape = getShapeType(attributeValue);
            String myScaleString = elementIn.getAttributeValue(OVERLAY_SCALE);
            Double myIconScale = (null != myScaleString) ? Double.valueOf(myScaleString) : null;
            List<String> myFieldList = null;
            Element myItemElementList = elementIn.getChild(ITEM_LIST);
            List<Element> myItemElements = (null != myItemElementList) ? myItemElementList.getChildren(ITEM) : null;

            if ((null != myItemElements) && !myItemElements.isEmpty()) {

                myFieldList = new ArrayList<String>();
                for (Element myItemElement : myItemElements) {

                    String myItem = myItemElement.getAttributeValue(VALUE);

                    if ((null != myItem) && (0 < myItem.length())) {

                        myFieldList.add(myItem);
                    }
                }
            }
            myPlaceStyle = new PlaceStyle(myName, myIconId, myColor, myShape, myIconScale, myFieldList);
        }
        return myPlaceStyle;
    }

    private static AssociationStyle importAssociationStyle(Element elementIn) {

        AssociationStyle myAssociationStyle = null;

        if (null != elementIn) {

            String myName = elementIn.getAttributeValue(NAME);
            String myColorString = elementIn.getAttributeValue(COLOR);
            Integer myColor = (null != myColorString) ? Integer.decode(myColorString) : null;
            String myWidthString = elementIn.getAttributeValue(WIDTH);
            Double myWidth = (null != myWidthString) ? Double.valueOf(myWidthString) : null;
            LineStyle myLineStyle = LineStyle.getLine(elementIn.getAttributeValue(STYLE));
            List<String> myFieldList = null;
            Element myItemElementList = elementIn.getChild(ITEM_LIST);
            List<Element> myItemElements = (null != myItemElementList) ? myItemElementList.getChildren(ITEM) : null;

            if ((null != myItemElements) && !myItemElements.isEmpty()) {

                myFieldList = new ArrayList<String>();
                for (Element myItemElement : myItemElements) {

                    String myItem = myItemElement.getAttributeValue(VALUE);

                    if ((null != myItem) && (0 < myItem.length())) {

                        myFieldList.add(myItem);
                    }
                }
            }
            myAssociationStyle = new AssociationStyle(myName, myColor, myWidth, myLineStyle, myFieldList);
        }
        return myAssociationStyle;
    }
}
