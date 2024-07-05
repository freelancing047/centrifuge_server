/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.service.export;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.DeepCloner;
import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.theme.ThemeHelper;
import csi.server.business.service.GraphActionsService;
import csi.server.business.service.export.anx.ANXWriterActions;
import csi.server.business.service.export.csv.ChartTableCsvWriter;
import csi.server.business.service.export.csv.CsvWriter;
import csi.server.business.service.export.csv.CsvWriterFactory;
import csi.server.business.service.export.csv.LinksListCsvWriter;
import csi.server.business.service.export.csv.NodeListCsvWriter;
import csi.server.business.service.export.pdf.PdfWriter;
import csi.server.business.service.export.pdf.PdfWriterFactory;
import csi.server.business.service.export.png.PNGImageCreator;
import csi.server.business.service.icon.IconActionsService;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.dto.Response;
import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.resource.ExportImportConstants;
import csi.server.common.dto.resource.ExportImportHelper;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTileLayer;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.DateUtil;
import csi.shared.core.imaging.ImagingRequest;

/**
 * Creates a file on the filesystem and provides a key to the client.
 * Clients can use this key to download the file.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ExportActionsService implements ExportActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(ExportActionsService.class);

   private static final Pattern NON_LETTERS_NUMBERS_PATTERN = Pattern.compile("[^a-zA-Z0-9.-]");
   private static final Pattern UUID_ELEMENT_CONTENTS_PATTERN = Pattern.compile("\\<uuid\\>.*\\</uuid\\>");
   private static final Pattern HEX_POUND_REGEX = Pattern.compile("^(#)([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");
   private static final Pattern HEX_0X_REGEX = Pattern.compile("^(0[x|X])([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");

   private static Map<String,String> colorNameCode = new HashMap<String,String>();

   static {
      colorNameCode.put("aliceblue", "#f0f8ff");
      colorNameCode.put("antiquewhite", "#faebd7");
      colorNameCode.put("aqua", "#00ffff");
      colorNameCode.put("aquamarine", "#7fffd4");
      colorNameCode.put("azure", "#f0ffff");
      colorNameCode.put("beige", "#f5f5dc");
      colorNameCode.put("bisque", "#ffe4c4");
      colorNameCode.put("black", "#000000");
      colorNameCode.put("blanchedalmond", "#ffebcd");
      colorNameCode.put("blue", "#0000ff");
      colorNameCode.put("blueviolet", "#8a2be2");
      colorNameCode.put("brown", "#a52a2a");
      colorNameCode.put("burlywood", "#deb887");
      colorNameCode.put("cadetblue", "#5f9ea0");
      colorNameCode.put("chartreuse", "#7fff00");
      colorNameCode.put("chocolate", "#d2691e");
      colorNameCode.put("coral", "#ff7f50");
      colorNameCode.put("cornflowerblue", "#6495ed");
      colorNameCode.put("cornsilk", "#fff8dc");
      colorNameCode.put("crimson", "#dc143c");
      colorNameCode.put("cyan", "#00ffff");
      colorNameCode.put("darkblue", "#00008b");
      colorNameCode.put("darkcyan", "#008b8b");
      colorNameCode.put("darkgoldenrod", "#b8860b");
      colorNameCode.put("darkgray", "#a9a9a9");
      colorNameCode.put("darkgreen", "#006400");
      colorNameCode.put("darkkhaki", "#bdb76b");
      colorNameCode.put("darkmagenta", "#8b008b");
      colorNameCode.put("darkolivegreen", "#556b2f");
      colorNameCode.put("darkorange", "#ff8c00");
      colorNameCode.put("darkorchid", "#9932cc");
      colorNameCode.put("darkred", "#8b0000");
      colorNameCode.put("darksalmon", "#e9967a");
      colorNameCode.put("darkseagreen", "#8fbc8f");
      colorNameCode.put("darkslateblue", "#483d8b");
      colorNameCode.put("darkslategray", "#2f4f4f");
      colorNameCode.put("darkturquoise", "#00ced1");
      colorNameCode.put("darkviolet", "#9400d3");
      colorNameCode.put("deeppink", "#ff1493");
      colorNameCode.put("deepskyblue", "#00bfff");
      colorNameCode.put("dimgray", "#696969");
      colorNameCode.put("dodgerblue", "#1e90ff");
      colorNameCode.put("firebrick", "#b22222");
      colorNameCode.put("floralwhite", "#fffaf0");
      colorNameCode.put("forestgreen", "#228b22");
      colorNameCode.put("fuchsia", "#ff00ff");
      colorNameCode.put("gainsboro", "#dcdcdc");
      colorNameCode.put("ghostwhite", "#f8f8ff");
      colorNameCode.put("gold", "#ffd700");
      colorNameCode.put("goldenrod", "#daa520");
      colorNameCode.put("gray", "#808080");
      colorNameCode.put("green", "#008000");
      colorNameCode.put("greenyellow", "#adff2f");
      colorNameCode.put("honeydew", "#f0fff0");
      colorNameCode.put("hotpink", "#ff69b4");
      colorNameCode.put("indianred ", "#cd5c5c");
      colorNameCode.put("indigo ", "#4b0082");
      colorNameCode.put("ivory", "#fffff0");
      colorNameCode.put("khaki", "#f0e68c");
      colorNameCode.put("lavender", "#e6e6fa");
      colorNameCode.put("lavenderblush", "#fff0f5");
      colorNameCode.put("lawngreen", "#7cfc00");
      colorNameCode.put("lemonchiffon", "#fffacd");
      colorNameCode.put("lightblue", "#add8e6");
      colorNameCode.put("lightcoral", "#f08080");
      colorNameCode.put("lightcyan", "#e0ffff");
      colorNameCode.put("lightgoldenrodyellow", "#fafad2");
      colorNameCode.put("lightgray", "#d3d3d3");
      colorNameCode.put("lightgreen", "#90ee90");
      colorNameCode.put("lightpink", "#ffb6c1");
      colorNameCode.put("lightsalmon", "#ffa07a");
      colorNameCode.put("lightseagreen", "#20b2aa");
      colorNameCode.put("lightskyblue", "#87cefa");
      colorNameCode.put("lightslategray", "#778899");
      colorNameCode.put("lightsteelblue", "#b0c4de");
      colorNameCode.put("lightyellow", "#ffffe0");
      colorNameCode.put("lime", "#00ff00");
      colorNameCode.put("limegreen", "#32cd32");
      colorNameCode.put("linen", "#faf0e6");
      colorNameCode.put("magenta", "#ff00ff");
      colorNameCode.put("maroon", "#800000");
      colorNameCode.put("mediumaquamarine", "#66cdaa");
      colorNameCode.put("mediumblue", "#0000cd");
      colorNameCode.put("mediumorchid", "#ba55d3");
      colorNameCode.put("mediumpurple", "#9370db");
      colorNameCode.put("mediumseagreen", "#3cb371");
      colorNameCode.put("mediumslateblue", "#7b68ee");
      colorNameCode.put("mediumspringgreen", "#00fa9a");
      colorNameCode.put("mediumturquoise", "#48d1cc");
      colorNameCode.put("mediumvioletred", "#c71585");
      colorNameCode.put("midnightblue", "#191970");
      colorNameCode.put("mintcream", "#f5fffa");
      colorNameCode.put("mistyrose", "#ffe4e1");
      colorNameCode.put("moccasin", "#ffe4b5");
      colorNameCode.put("navajowhite", "#ffdead");
      colorNameCode.put("navy", "#000080");
      colorNameCode.put("oldlace", "#fdf5e6");
      colorNameCode.put("olive", "#808000");
      colorNameCode.put("olivedrab", "#6b8e23");
      colorNameCode.put("orange", "#ffa500");
      colorNameCode.put("orangered", "#ff4500");
      colorNameCode.put("orchid", "#da70d6");
      colorNameCode.put("palegoldenrod", "#eee8aa");
      colorNameCode.put("palegreen", "#98fb98");
      colorNameCode.put("paleturquoise", "#afeeee");
      colorNameCode.put("palevioletred", "#db7093");
      colorNameCode.put("papayawhip", "#ffefd5");
      colorNameCode.put("peachpuff", "#ffdab9");
      colorNameCode.put("peru", "#cd853f");
      colorNameCode.put("pink", "#ffc0cb");
      colorNameCode.put("plum", "#dda0dd");
      colorNameCode.put("powderblue", "#b0e0e6");
      colorNameCode.put("purple", "#800080");
      colorNameCode.put("red", "#ff0000");
      colorNameCode.put("rosybrown", "#bc8f8f");
      colorNameCode.put("royalblue", "#4169e1");
      colorNameCode.put("saddlebrown", "#8b4513");
      colorNameCode.put("salmon", "#fa8072");
      colorNameCode.put("sandybrown", "#f4a460");
      colorNameCode.put("seagreen", "#2e8b57");
      colorNameCode.put("seashell", "#fff5ee");
      colorNameCode.put("sienna", "#a0522d");
      colorNameCode.put("silver", "#c0c0c0");
      colorNameCode.put("skyblue", "#87ceeb");
      colorNameCode.put("slateblue", "#6a5acd");
      colorNameCode.put("slategray", "#708090");
      colorNameCode.put("snow", "#fffafa");
      colorNameCode.put("springgreen", "#00ff7f");
      colorNameCode.put("steelblue", "#4682b4");
      colorNameCode.put("tan", "#d2b48c");
      colorNameCode.put("teal", "#008080");
      colorNameCode.put("thistle", "#d8bfd8");
      colorNameCode.put("tomato", "#ff6347");
      colorNameCode.put("turquoise", "#40e0d0");
      colorNameCode.put("violet", "#ee82ee");
      colorNameCode.put("wheat", "#f5deb3");
      colorNameCode.put("white", "#ffffff");
      colorNameCode.put("whitesmoke", "#f5f5f5");
      colorNameCode.put("yellow", "#ffff00");
      colorNameCode.put("yellowgreen", "#9acd32");
   }

    private class ProcessedResource {

        public AclResourceType type;
        public Resource source;
        public Document document;

        public ProcessedResource(AclResourceType typeIn, Resource sourceIn, Document documentIn) {

            type = typeIn;
            source = sourceIn;
            document = documentIn;
        }
    }

    private static final int BUFFER_SIZE = 65536;

    @Autowired
    GraphActionsService graphActionsService;

    @Autowired
    ServletContext servletContext;

    @Autowired
    IconActionsService iconActionsService;

   @Override
   public String createGraphPNGWithLegend(String vizuuid, String prefixFileName,  ImagingRequest legend,
                                          int viewWdith, int viewHeight, String securityText) {
      String usingPrefixFileName = NON_LETTERS_NUMBERS_PATTERN.matcher(prefixFileName).replaceAll("_");
      SecurityPolicyConfig mySecurityConfig = Configuration.getInstance().getSecurityPolicyConfig();
      PNGImageCreator pngImageCreator = new PNGImageCreator();
      String zipPath = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY) + File.separator + UUID.randomUUID().toString() + ".file";

      try (FileOutputStream fout = new FileOutputStream(zipPath);
           BufferedOutputStream bos = new BufferedOutputStream(fout);
           ZipOutputStream zout = new ZipOutputStream(bos)) {
         BufferedImage image = graphActionsService.getDisplayImage(vizuuid, String.valueOf(viewWdith), String.valueOf(viewHeight));
         BufferedImage composite = new BufferedImage(image.getWidth(), image.getHeight() + 50, BufferedImage.TYPE_INT_RGB);

         if ((securityText != null) && mySecurityConfig.getEnableCapcoLabelProcessing().booleanValue()) {
            BufferedImage tempImage = new BufferedImage(image.getWidth(), 25, BufferedImage.TYPE_INT_RGB);
            Graphics2D securityGraphics = tempImage.createGraphics();
            //This is a map of <String, String> instead of a value pair.
            Map<String, String> colors =  Configuration.getInstance().getSecurityPolicyConfig().getBannerColors();
            String color1 = null;
            String color2 = null;

            for (Map.Entry<String, String> entry : colors.entrySet()) {
               if (securityText.contains(entry.getKey())) {
                  String entryValue = entry.getValue();
                  String[] splitValues = entryValue.split(",");
                  color1 = splitValues[0];
                  color2 = splitValues[1];
               }
            }
            securityGraphics.setColor(stringToColor(color2));
            securityGraphics.fillRect(0,0, image.getWidth(), 25);
            securityGraphics.setColor(stringToColor(color1));
            securityGraphics.setFont(new Font("Arial", Font.PLAIN, 13));
            FontMetrics metrics = securityGraphics.getFontMetrics();

            while (metrics.stringWidth(securityText) >= tempImage.getWidth()) {
               securityGraphics.setFont(new Font(securityGraphics.getFont().getFontName(), Font.PLAIN, securityGraphics.getFont().getSize() - 1));
            }
            securityGraphics.drawString(securityText,  (tempImage.getWidth()/2) - (metrics.stringWidth(securityText)/2), 17);
            securityGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            securityGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            securityGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            composite.getGraphics().drawImage(tempImage, 0, 0, null);
            composite.getGraphics().drawImage(tempImage, 0, image.getHeight() + 25, null);
         }
         if (mySecurityConfig.getEnableCapcoLabelProcessing().booleanValue()) {
            composite.getGraphics().drawImage(image, 0, 25, null);
         } else {
            composite = image;
         }
         File downloadFile = getDownloadFile();
         LOG.info("Exporting graph as a ZIP file.");
         writeImageToFile(composite, downloadFile);

         zout.putNextEntry(new ZipEntry(usingPrefixFileName + "_Graph.png"));
         Files.copy(downloadFile.toPath(), zout);
         zout.closeEntry();

         BufferedImage leg = pngImageCreator.createImage(legend);
         File dFile = getDownloadFile();
         writeImageToFile(leg, dFile);

         zout.putNextEntry(new ZipEntry(usingPrefixFileName + "_Legend.png"));
         Files.copy(dFile.toPath(), zout);
         zout.closeEntry();
      } catch (Exception e){
         // LOG and ignore..
         LOG.error("error creating graph zip " + e.getClass() + " " + e.getMessage());
      }
      return getDownloadToken(new File(zipPath));
   }

   /**
    * Return a Color given a String
    *
    * @param stringArg - one of [ #ffffff, #fff, 0xffffff, 0xfff, a color name ]
    * @return Color
    */
   public Color stringToColor(final String stringArg) {
      Color result = null;
      Matcher matcher = HEX_POUND_REGEX.matcher(stringArg);

      if (matcher.matches()) {
         result = new Color(Integer.parseInt(matcher.group(2), 16));
      } else {
         matcher = HEX_0X_REGEX.matcher(stringArg);

         if (matcher.matches()) {
            result = new Color(Integer.parseInt(matcher.group(2), 16));
         } else {
            String colorCode = colorNameCode.get(stringArg.toLowerCase());

            if (colorCode != null) {
               result = new Color(Integer.parseInt(colorCode.substring(1), 16));
            }
         }
      }
      return result;
   }

    @Override
    public String createGraphPNG(String vizuuid, int viewWdith, int viewHeight) {
        try {
            BufferedImage image = graphActionsService.getDisplayImage(vizuuid, String.valueOf(viewWdith), String.valueOf(viewHeight));
            File downloadFile = getDownloadFile();
            LOG.info("Exporting graph as a PNG file.");
            writeImageToFile(image, downloadFile);
            return getDownloadToken(downloadFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw Throwables.propagate(e);
        }
    }

   @Override
   public String createANX(String vizuuid) {
      File file = getDownloadFile();
      GraphContext gc = GraphServiceUtil.getGraphContext(vizuuid);

      try (FileWriter fileWriter = new FileWriter(file)) {
         String anx = ANXWriterActions.generateANX(gc);

         fileWriter.write(anx);
      } catch (Exception ignored) {
      }
      return getDownloadToken(file);
   }

    @Override
    public String createPNG(ImagingRequest request) {
        PNGImageCreator pngImageCreator = new PNGImageCreator();
        BufferedImage image = pngImageCreator.createImage(request);
        File downloadFile = getDownloadFile();
        LOG.info("Exporting image as a PNG file.");
        writeImageToFile(image, downloadFile);
        return getDownloadToken(downloadFile);
    }


    private void writeImageToFile(BufferedImage image, File downloadFile) {
        try {
            ImageIO.write(image, ExportImportConstants.PNG_EXTENSION, downloadFile);
        } catch (IOException e) {
            LOG.error("Unable to write Image to file " + e.getMessage());
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String createCSV(String dvUuid, VisualizationDef visualizationDef, boolean useSelectionOnly) {
        File file = getDownloadFile();
        CsvWriter writer = CsvWriterFactory.createCsvWriter(dvUuid, visualizationDef, useSelectionOnly);
        LOG.info("Exporting visualization " + Format.value(visualizationDef.getName()) + " as a CSV file.");
        writer.writeCsv(file);
        return getDownloadToken(file);
    }

    @Override
    public String createChartTableCSV(List<String>headers, List<List<String>> data) {
        File file = getDownloadFile();
        ChartTableCsvWriter writer = new ChartTableCsvWriter(headers, data);
        LOG.info("Exporting chart table as a CSV file.");
        writer.writeCsv(file);
        return getDownloadToken(file);
    }

    @Override
    public String createPDF(String dataViewUuid, VisualizationDef visualizationDef) {
        File file = getDownloadFile();
        PdfWriter writer = PdfWriterFactory.createPdfWriter(dataViewUuid, visualizationDef);
        LOG.info("Exporting visualization " + Format.value(visualizationDef.getName()) + " as a PDF file.");
        writer.writePdf(file);
        return getDownloadToken(file);
    }

   public void destroyDownload(String tokenIn) {
      getDownloadFile(tokenIn).delete();
   }

    private String createPrettyXML(Resource resourceIn) {

        return createPrettyXML(resourceIn, false);
    }

   private static String createPrettyXML(Resource resource, boolean stripUuids) {
      String result = null;

      try (StringWriter writer = new StringWriter()) {
         XStream ieCodec = XStreamHelper.getImportExportCodec();
//         String xml = ieCodec.toXML(resource);
         ieCodec.marshal(resource, new PrettyPrintWriter(writer));
//         result = stripUuidsIn ? myWriter.toString().replaceAll("\n[\t ]*\\<uuid\\>.*\n", "\n") : myWriter.toString();
         result = stripUuids ? UUID_ELEMENT_CONTENTS_PATTERN.matcher(writer.toString()).replaceAll("<uuid></uuid>") : writer.toString();
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
      return result;
   }

    private static void validateAuthorization(String dataViewUuid, Resource resource) throws CentrifugeException {

        if (resource == null) {
            throw new CentrifugeException(String.format("No resource found with UUID %s", dataViewUuid));
        }

        if (!CsiSecurityManager.canEditResourceConnections(resource)) {
            throw new CentrifugeException(
                    "Not authorized to export resource.  The data definition contains one or more unauthorized connection types.");
        }
    }

    public Response<String, String> exportTheme(String themeIdIn) {

        try {

            File myFile = exportTheme(CsiPersistenceManager.findObject(Theme.class, themeIdIn));

            if (null != myFile) {

                return new Response<String, String>(themeIdIn, getDownloadToken(myFile));

            } else {

                return new Response<String, String>(themeIdIn, ServerMessage.RESOURCE_EXPORT_ERROR);
            }

        } catch (Exception myException) {

            return new Response<String, String>(themeIdIn, ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));

        }
    }

    public Response<String, String> exportDataView(String uuidIn) {

        File myExportFile = null;
        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, uuidIn);

        if (null != myDataView) {

            try {

                validateAuthorization(uuidIn, myDataView);
                DataView mySpinOff = DeepCloner.clone(myDataView, DeepCloner.CloneType.NEW_ID);
                CsiPersistenceManager.flush();
                if (null != mySpinOff) {

                    DataViewHelper.clearDataReferences(mySpinOff);
                    ModelHelper.resetSecurity(mySpinOff);
                    DataViewHelper.fixupPersistenceLinkage(mySpinOff);
                    mySpinOff.setSpinoff(false);
                    mySpinOff.setVersion(ReleaseInfo.version);
                    mySpinOff.getMeta().setVersion(ReleaseInfo.version);
                    LOG.info("Exporting DataView " + Format.value(myDataView.getName())
                                + ", id = " + myDataView.getUuid() + " as a zip file.");
                    myExportFile = buildExport(mySpinOff);
                }

            } catch (Exception myException) {

                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }
            return (null != myExportFile)
                    ? new Response<String, String>(uuidIn, getDownloadToken(myExportFile))
                    : new Response<String, String>(uuidIn, ServerMessage.RESOURCE_EXPORT_ERROR);
        }
        return new Response<String, String>(ServerMessage.DATAVIEW_LOCATE_ERROR);
    }

    public Response<String, String> exportTemplate(String uuidIn) {

        File myExportFile = null;
        DataViewDef myTemplate = CsiPersistenceManager.findObject(DataViewDef.class, uuidIn);

        if (null != myTemplate) {

            try {

                validateAuthorization(uuidIn, myTemplate);
                DataViewDef mySpinOff = DeepCloner.clone(myTemplate, DeepCloner.CloneType.NEW_ID);
                CsiPersistenceManager.flush();
                if (null != mySpinOff) {

                    ModelHelper.resetSecurity(mySpinOff);
                    DataViewHelper.fixupPersistenceLinkage(mySpinOff);
                    mySpinOff.setVersion(ReleaseInfo.version);
                    LOG.info("Exporting DataView " + Format.value(myTemplate.getName())
                            + ", id = " + myTemplate.getUuid() + " as a zip file.");
                    myExportFile = buildExport(mySpinOff);
                }

            } catch (Exception myException) {

                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }
            return (null != myExportFile)
                        ? new Response<String, String>(uuidIn, getDownloadToken(myExportFile))
                        : new Response<String, String>(uuidIn, ServerMessage.RESOURCE_EXPORT_ERROR);
        }
        return new Response<String, String>(ServerMessage.TEMPLATE_LOCATE_ERROR);
    }

   private File buildExport(Resource resource) throws IOException {
      File exportFile = null;
      DataViewDef metaData = (resource instanceof DataView)
                                ? ((DataView) resource).getMeta()
                                : (resource instanceof DataViewDef) ? (DataViewDef) resource : null;

      if (metaData != null) {
         String resourceXml = createPrettyXML(resource, true);

         if (resourceXml != null) {
            try {
               exportFile = getDownloadFile();

               try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
                  fileOutputStream.write(ExportImportConstants.XML_HEADER);
                  fileOutputStream.write(resourceXml.getBytes(StandardCharsets.UTF_8));
                  fileOutputStream.flush();
               }
            } catch (Exception exception) {
               if ((exportFile != null) && exportFile.exists()) {
                  exportFile.delete();
               }
               throw exception;
            }
         }
      }
      return exportFile;
   }

   private File exportTheme(Theme theme) throws Exception {
      File exportFile = null;
      AclResourceType resourceType =
         (theme instanceof GraphTheme)
            ? AclResourceType.GRAPH_THEME
            : (theme instanceof MapTheme) ? AclResourceType.MAP_THEME : AclResourceType.UNKNOWN;

      if (AclResourceType.UNKNOWN != resourceType) {
         try {
            LOG.info("Exporting Theme " + Format.value(theme.getName()) + " as an XML file.");

            String themeXml = createThemeXml(theme, null);

            if (themeXml != null) {
               exportFile = getDownloadFile();

               try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
                  fileOutputStream.write(themeXml.getBytes());
                  fileOutputStream.flush();
               }
            }
         } catch (Exception exception) {
            LOG.error("Caught exception exporting Theme", exception);

            if ((null != exportFile) && exportFile.exists()) {
               exportFile.delete();
            }
            throw exception;
         }
      }
      return exportFile;
   }

   private File exportTheme(Theme theme, Map<String,String> iconsToExportIn, boolean includeIconsIn)
         throws Exception {
      File exportFile = null;
      AclResourceType resourceType =
         (theme instanceof GraphTheme)
            ? AclResourceType.GRAPH_THEME
            : ((theme instanceof MapTheme) ? AclResourceType.MAP_THEME : AclResourceType.UNKNOWN);

      if (AclResourceType.UNKNOWN != resourceType) {
         boolean exportIcons = includeIconsIn && CsiSecurityManager.isIconAdmin();
         Map<String,String> iconsToExport = ((iconsToExportIn == null) ? new TreeMap<String,String>() : iconsToExportIn);

         try {
            LOG.info("Exporting Theme " + Format.value(theme.getName()) + " as an XML file.");

            String themeXml = createThemeXml(theme, iconsToExport);

            if (themeXml != null) {
               Map<String,String> grabBag = ExportImportHelper.buildFileNameComponent(theme);
               String label = buildFileName(grabBag, Configuration.getInstance().getExportFileNameConfig().getOrder());
               Document contentsDoc = new Document();
               Element xmlRoot = new Element("Contents");

               contentsDoc.setRootElement(xmlRoot);

               Element xmlThemeList = ContentsHelper.createElement(resourceType, Integer.valueOf(1));
               Element themeEntry = ContentsHelper.createElement(theme);

               xmlThemeList.getChildren().add(themeEntry);
               xmlRoot.getChildren().add(xmlThemeList);

               if ((iconsToExportIn == null) && !iconsToExport.isEmpty()) {
                  Element iconEntry = ContentsHelper.createElement(AclResourceType.ICON, Integer.valueOf(iconsToExport.size()));
                  xmlRoot.getChildren().add(iconEntry);
               }
               // Initialize zip stream
               exportFile = getDownloadFile();

               try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
                  byte[] contents = formatXmlDocument(contentsDoc).getBytes();

                  fileOutputStream.write(getSizeArray(contents));
                  fileOutputStream.write(contents);

                  try (ZipOutputStream zipStream = new ZipOutputStream(fileOutputStream)) {
                     // Add contents to zip stream
//                    addZipEntry("Contents.xml", contentsDoc, zipStream);

                     // Add Theme to zip stream
                     zipStream.putNextEntry(new ZipEntry(label));
                     zipStream.write(themeXml.getBytes());
                     zipStream.closeEntry();

                     if (exportIcons && (iconsToExportIn == null) && !iconsToExport.isEmpty()) {
                        // Add Icons to zip stream
                        Document myDocument = ThemeHelper.createIconDocument(iconActionsService, iconsToExport.keySet());

                        try (StringWriter myWriter = createDocumentXml(myDocument)) {
                           grabBag = ExportImportHelper.buildIconListFileName();
                           label = buildFileName(grabBag, Configuration.getInstance().getExportFileNameConfig().getOrder());

                           zipStream.putNextEntry(new ZipEntry(label));
                           zipStream.write(myWriter.toString().getBytes());
                           zipStream.closeEntry();
                        }
                     }
                  }
                  fileOutputStream.flush();
               }
            }
         } catch (Exception exception) {
            LOG.error("Caught exception exporting Theme", exception);

            if ((exportFile != null) && exportFile.exists()) {
               exportFile.delete();
               throw exception;
            }
         }
      }
      return exportFile;
   }

   private static String buildFileName(Map<String,String> grabBag, List<String> order) {
      grabBag.put("date", ZonedDateTime.now().format(DateUtil.YYYY_UNDER_MM_UNDER_DD_FORMATTER));

      List<String> componentsToUse = new ArrayList<>();

      for (String key : order) {
         if (grabBag.containsKey(key)) {
            componentsToUse.add(grabBag.get(key));
         }
      }
      return componentsToUse.stream().collect(Collectors.joining("_"));
   }

    private String createThemeXml(Theme themeIn, Map<String, String> iconsToExportIn) {

        String myXml = null;
        Document myDocument = null;

        if (themeIn instanceof GraphTheme) {

            myDocument = ThemeHelper.createGraphDocument((GraphTheme) themeIn, iconsToExportIn);

        } else if (themeIn instanceof MapTheme) {

            myDocument = ThemeHelper.createMapDocument((MapTheme) themeIn, iconsToExportIn);

        }
        if (null != myDocument) {

            StringWriter myWriter = createDocumentXml(myDocument);

            myXml = myWriter.toString();
            try {

                myWriter.close();

            } catch (Exception IGNORE) {}
        }
        return myXml;
    }

   private static StringWriter createDocumentXml(Document documentIn) {
      StringWriter myWriter = new StringWriter();

      try {
         XMLOutputter myXmlOutput = new XMLOutputter();

         myXmlOutput.setFormat(org.jdom.output.Format.getPrettyFormat());
         myXmlOutput.output(documentIn, myWriter);
      } catch (Exception myException) {
         LOG.error("Caught exception creating XML.", myException);
      }
      return myWriter;
   }

    private void exportIcon(ZipOutputStream zipStreamIn, String iconIdIn) {

        try {

            Icon myIcon = iconActionsService.getIcon(iconIdIn);
            String dataUrl = (null != myIcon) ? myIcon.getImage() : null;

            if ((null != dataUrl) && (0 < dataUrl.length())) {

                String encodingPrefix = "base64,";
                int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
                String base64Image = dataUrl.substring(contentStartIndex);

                if ((null != base64Image) && (0 < base64Image.length())) {

                    String myEntryLabel = "Icon|" + iconIdIn + "|" + myIcon.getName();

                    if ((!myEntryLabel.endsWith(".png") && !myEntryLabel.endsWith(".jpg") && !myEntryLabel.endsWith(".jpeg") && !myEntryLabel.endsWith(".gif"))) {

                        myEntryLabel += ExportImportConstants.PNG_SUFFIX;
                    }
                    zipStreamIn.putNextEntry(new ZipEntry(myEntryLabel));
                    zipStreamIn.write(Base64.getDecoder().decode(base64Image));
                    zipStreamIn.closeEntry();
                }
            }
        }
        catch (Exception myException) {

            LOG.error("Caught exception exporting icon", myException);
        }
    }

   @Override
   public String createExportName(AclResourceType dataType, String resourceId, List<String> order) {
      Resource resource = null;

      if (dataType == AclResourceType.DATAVIEW) {
         resource = CsiPersistenceManager.findObject(DataView.class, resourceId);
      } else if (dataType == AclResourceType.TEMPLATE) {
         resource = CsiPersistenceManager.findObject(DataViewDef.class, resourceId);
      } else if (dataType == AclResourceType.THEME) {
         resource = CsiPersistenceManager.findObject(Theme.class, resourceId);
      }
      return buildFileName(ExportImportHelper.buildFileNameComponents(resource), order);
   }

    @Override
    public Response<String, String> exportSupportingResources(boolean includeThemesIn, boolean includeIconsIn,
                                                              boolean includeMapsIn) {

        boolean myExportIcons = includeIconsIn && CsiSecurityManager.isIconAdmin();

        if (includeThemesIn || myExportIcons || includeMapsIn) {

            try {

                List<Basemap> myAvailableMaps = includeMapsIn ? AclRequest.getBasemapsAvoidingSecurity() : null;
                Map<String, ProcessedResource> myChosenMaps = (null != myAvailableMaps)
                                                                ? new TreeMap<String, ProcessedResource>() : null;
                Document myContentsDoc = new Document();
                Element myXmlRoot = new Element("Contents");
                Map<String, ProcessedResource> myThemeMap = includeThemesIn ? new TreeMap<String, ProcessedResource>() : null;
                List<String> myIconsToExport = null;

                myContentsDoc.setRootElement(myXmlRoot);
                if (includeThemesIn) {

                    List<String> myList = AclRequest.listAuthorizedThemeUuids(new AclControlType[]{AclControlType.READ});

                    for (String myUuid : myList) {

                        Response<String, String> myResponse = processThemeForExport(myUuid, myThemeMap, null);

                        if (null != myResponse) {

                            return myResponse;
                        }
                    }
                }
                if ((null != myAvailableMaps) && !myAvailableMaps.isEmpty()) {

                    for (Basemap myMap : myAvailableMaps) {

                        try {

                            Response<String, String> myResponse = processMapForExport(myMap, myChosenMaps);

                            if (null != myResponse) {

                                return myResponse;
                            }

                        } catch (Exception myException) {

                            return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                                    Format.value(myException));
                        }
                    }
                }
                if (myExportIcons) {

                    myIconsToExport = AclRequest.listAuthorizedIconUuids(new AclControlType[]{AclControlType.READ});
                }
                return finalizeExport(null, myThemeMap, myChosenMaps, myIconsToExport, myContentsDoc);

            } catch (Exception myException) {

                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
            }
        }
        return new Response<String, String>(ServerMessage.BAD_ARGUMENTS);
    }

    @Override
    public Response<String, String> exportThemes(List<String> uuidListIn, boolean includeIconsIn, boolean includeMapsIn) {

        if ((null != uuidListIn) && !uuidListIn.isEmpty()) {

            List<Basemap> myAvailableMaps = includeMapsIn ? AclRequest.getBasemapsAvoidingSecurity() : null;
            Map<String, ProcessedResource> myChosenMaps = (null != myAvailableMaps)
                                                            ? new TreeMap<String, ProcessedResource>() : null;
            Map<String, ProcessedResource> myThemeMap = new TreeMap<String, ProcessedResource>();
            Map<String, String> myIconsToExport = includeIconsIn ? new TreeMap<String, String>() : null;
            Document myContentsDoc = new Document();
            Element myXmlRoot = new Element("Contents");

            myContentsDoc.setRootElement(myXmlRoot);
            for (String myUuid : uuidListIn) {

                Response<String, String> myResponse = processThemeForExport(myUuid, myThemeMap, myIconsToExport);

                if (null != myResponse) {

                    return myResponse;
                }
            }
            if ((null != myAvailableMaps) && !myAvailableMaps.isEmpty()) {

                for (Basemap myMap : myAvailableMaps) {

                    try {

                        Response<String, String> myResponse = processMapForExport(myMap, myChosenMaps);

                        if (null != myResponse) {

                            return myResponse;
                        }

                    } catch (Exception myException) {

                        return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                                Format.value(myException));
                    }
                }
            }
            return finalizeExport(null, myThemeMap, myChosenMaps,
                                    (null != myIconsToExport) ? myIconsToExport.keySet() : null, myContentsDoc);
        }
        return new Response<String, String>(ServerMessage.BAD_ARGUMENTS);
    }

    @Override
    public Response<String, String> exportDataViews(List<String> uuidListIn, boolean includeThemesIn,
                                                    boolean includeIconsIn, boolean includeMapsIn,
                                                    boolean includeDataIn) {

        if ((null != uuidListIn) && !uuidListIn.isEmpty()) {

            Map<String, Basemap> myAvailableMaps = getAvailableMaps(includeMapsIn);
            Map<String, ProcessedResource> myChosenMaps = (null != myAvailableMaps)
                                                            ? new TreeMap<String, ProcessedResource>() : null;
            List<ValuePair<String, String>> myDataList = new ArrayList<ValuePair<String, String>>();
            Map<String, ProcessedResource> myThemeMap = includeThemesIn ? new TreeMap<String, ProcessedResource>() : null;
            Map<String, String> myIconsToExport = includeIconsIn ? new TreeMap<String, String>() : null;
            Document myContentsDoc = new Document();
            Element myXmlRoot = new Element("Contents");
            Element myResourceList = ContentsHelper.createElement(AclResourceType.DATAVIEW, uuidListIn.size());

            myContentsDoc.setRootElement(myXmlRoot);
            myXmlRoot.getChildren().add(myResourceList);
            for (String myUuid : uuidListIn) {

                DataView myDataView = CsiPersistenceManager.findObject(DataView.class, myUuid);

                if (null != myDataView) {

                    try {

                        validateAuthorization(myUuid, myDataView);
                        DataView mySpinOff = DeepCloner.clone(myDataView, DeepCloner.CloneType.NEW_ID);
                        CsiPersistenceManager.flush();
                        if (null != mySpinOff) {

                            DataViewHelper.clearDataReferences(mySpinOff);
                            ModelHelper.resetSecurity(mySpinOff);
                            DataViewHelper.fixupPersistenceLinkage(mySpinOff);
                            mySpinOff.setSpinoff(false);
                            mySpinOff.setVersion(ReleaseInfo.version);
                            mySpinOff.getMeta().setVersion(ReleaseInfo.version);
                            LOG.info("Exporting DataView " + Format.value(myDataView.getName())
                                        + ", id = " + myDataView.getUuid() + " as a zip file.");
                            // Process this DataView any response will indicate an error!
                            Response<String, String> myResponse = exportResource(myUuid, mySpinOff, myDataList,
                                                                                    myThemeMap, myAvailableMaps,
                                                                                    myChosenMaps, myIconsToExport,
                                                                                    myResourceList);

                            if (null != myResponse) {

                                return myResponse;
                            }
                        }

                    } catch (Exception myException) {

                        return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
                    }
                }
            }
            return finalizeExport(myDataList, myThemeMap, myChosenMaps,
                                    (null != myIconsToExport) ? myIconsToExport.keySet() : null, myContentsDoc);
        }
        return new Response<String, String>(ServerMessage.BAD_ARGUMENTS);
    }

    @Override
    public Response<String, String> exportTemplates(List<String> uuidListIn, boolean includeThemesIn,
                                                    boolean includeIconsIn, boolean includeMapsIn) {

        if ((null != uuidListIn) && !uuidListIn.isEmpty()) {

            Map<String, Basemap> myAvailableMaps = getAvailableMaps(includeMapsIn);
            Map<String, ProcessedResource> myChosenMaps = (null != myAvailableMaps)
                    ? new TreeMap<String, ProcessedResource>() : null;
            List<ValuePair<String, String>> myDataList = new ArrayList<ValuePair<String, String>>();
            Map<String, ProcessedResource> myThemeMap = includeThemesIn ? new TreeMap<String, ProcessedResource>() : null;
            Map<String, String> myIconsToExport = includeIconsIn ? new TreeMap<String, String>() : null;
            Document myContentsDoc = new Document();
            Element myXmlRoot = new Element("Contents");
            Element myResourceList = ContentsHelper.createElement(AclResourceType.TEMPLATE, uuidListIn.size());

            myContentsDoc.setRootElement(myXmlRoot);
            myXmlRoot.getChildren().add(myResourceList);
            for (String myUuid : uuidListIn) {

                DataViewDef myTemplate = CsiPersistenceManager.findObject(DataViewDef.class, myUuid);

                if (null != myTemplate) {

                    try {

                        validateAuthorization(myUuid, myTemplate);
                        DataViewDef mySpinOff = DeepCloner.clone(myTemplate, DeepCloner.CloneType.NEW_ID);
                        CsiPersistenceManager.flush();
                        if (null != mySpinOff) {

                            ModelHelper.resetSecurity(mySpinOff);
                            DataViewHelper.fixupPersistenceLinkage(mySpinOff);
                            mySpinOff.setVersion(ReleaseInfo.version);
                            LOG.info("Exporting Template " + Format.value(myTemplate.getName())
                                    + ", id = " + myTemplate.getUuid() + " as a zip file.");
                            // Process this DataView any response will indicate an error!
                            Response<String, String> myResponse = exportResource(myUuid, mySpinOff, myDataList,
                                                                                    myThemeMap, myAvailableMaps,
                                                                                    myChosenMaps, myIconsToExport,
                                                                                    myResourceList);

                            if (null != myResponse) {

                                return myResponse;
                            }
                        }

                    } catch (Exception myException) {

                        return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(myException));
                    }
                }
            }
            return finalizeExport(myDataList, myThemeMap, myChosenMaps,
                                    (null != myIconsToExport) ? myIconsToExport.keySet() : null, myContentsDoc);
        }
        return new Response<String, String>(ServerMessage.BAD_ARGUMENTS);
    }

    private Response<String, String> exportResource(String uuidIn, Resource resourceIn,
                                                    List<ValuePair<String, String>> dataListIn,
                                                    Map<String, ProcessedResource> themeMapIn,
                                                    Map<String, Basemap> availableMapsIn,
                                                    Map<String, ProcessedResource> chosenMapsIn,
                                                    Map<String, String> includeIconsIn, Element resourceListIn) {

        DataViewDef myMetaData = (resourceIn instanceof DataView)
                ? ((DataView) resourceIn).getMeta()
                : (resourceIn instanceof DataViewDef)
                ? (DataViewDef) resourceIn
                : null;

        if (null != myMetaData) {

            String myResourceXml = createPrettyXML(resourceIn, true);

            if (null != myResourceXml) {

                DataModelDef myModel = myMetaData.getModelDef();

                dataListIn.add(new ValuePair(buildFileName(ExportImportHelper.buildFileNameComponent(resourceIn, uuidIn),
                        Configuration.getInstance().getExportFileNameConfig().getOrder()), myResourceXml));
                resourceListIn.getChildren().add(ContentsHelper.createElement(resourceIn, uuidIn));

                if ((null != availableMapsIn) && (null != chosenMapsIn)) {

                    for (VisualizationDef myVisualization : myModel.getVisualizations()) {

                        if (myVisualization instanceof MapViewDef) {

                            MapSettings mySettings = ((MapViewDef)myVisualization).getMapSettings();
                            List<MapTileLayer> myLayerList = (null != mySettings) ? mySettings.getTileLayers() : null;

                            if ((null != myLayerList) && !myLayerList.isEmpty()) {

                                for (MapTileLayer myLayer : myLayerList) {

                                    String myID = myLayer.getLayerId();
                                    Basemap myBasemap = (null != myID) ? availableMapsIn.get(myID) : null;

                                    if ((null != myBasemap) && !(chosenMapsIn.containsKey(myID))) {

                                        try {

                                            Response<String, String> myResponse = processMapForExport(myID, chosenMapsIn);

                                            if (null != myResponse) {

                                                return myResponse;
                                            }

                                        } catch (Exception myException) {

                                            return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                                                    Format.value(myException));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (null != themeMapIn) {

                        // Identify exported themes
                    for (VisualizationDef myVisualization : myModel.getVisualizations()) {

                        String myThemeId = myVisualization.getThemeUuid();

                        if (null != myThemeId) {

                            try {

                                if (!themeMapIn.containsKey(myThemeId)) {

                                    Response<String, String> myResponse = processThemeForExport(myThemeId, themeMapIn,
                                                                                                includeIconsIn);

                                    if (null != myResponse) {

                                        return myResponse;
                                    }
                                }

                            } catch (Exception myException) {

                                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                                                                    Format.value(myException));
                            }
                        }
                        if (myVisualization instanceof MapViewDef) {

                            MapSettings mySettings = ((MapViewDef)myVisualization).getMapSettings();

                            myThemeId = mySettings.getThemeUuid();

                            if (null != myThemeId) {

                                try {

                                    if (!themeMapIn.containsKey(myThemeId)) {

                                        Response<String, String> myResponse = processThemeForExport(myThemeId, themeMapIn,
                                                includeIconsIn);

                                        if (null != myResponse) {

                                            return myResponse;
                                        }
                                    }

                                } catch (Exception myException) {

                                    return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                                            Format.value(myException));
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Response<String, String> processThemeForExport(String uuidIn, Map<String, ProcessedResource> themeMapIn,
                                                           Map<String, String> includedIconsIn) {

        Theme myTheme = CsiPersistenceManager.findObject(Theme.class, uuidIn);

        if (null != myTheme) {

            AclResourceType myThemeType = (myTheme instanceof GraphTheme)
                    ? AclResourceType.GRAPH_THEME
                    : (myTheme instanceof MapTheme)
                    ? AclResourceType.MAP_THEME
                    : AclResourceType.THEME;

            try {

                Document myDocument = null;

                if (AclResourceType.GRAPH_THEME == myThemeType) {

                    myDocument = ThemeHelper.createGraphDocument((GraphTheme)myTheme, includedIconsIn);

                } else if (AclResourceType.MAP_THEME == myThemeType) {

                    myDocument = ThemeHelper.createMapDocument((MapTheme)myTheme, includedIconsIn);
                }
                if (null != myDocument) {

                    themeMapIn.put(uuidIn, new ProcessedResource(myThemeType, myTheme,
                            myDocument));
                }

            } catch (Exception myException) {

                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                        Format.value(myException));
            }
        }
        return null;
    }

    private Response<String, String> processMapForExport(String uuidIn, Map<String, ProcessedResource> chosenMapsIn) {

        return processMapForExport(CsiPersistenceManager.findObject(Basemap.class, uuidIn), chosenMapsIn);
    }

    private Response<String, String> processMapForExport(Basemap basemapIn, Map<String, ProcessedResource> chosenMapsIn) {

        if (null != basemapIn) {

            AclResourceType myType = AclResourceType.MAP_BASEMAP;

            try {

                Document myDocument = ThemeHelper.createBasemapDocument(basemapIn);

                if (null != myDocument) {

                    chosenMapsIn.put(basemapIn.getUuid(), new ProcessedResource(myType, basemapIn, myDocument));
                }

            } catch (Exception myException) {

                return new Response<String, String>(ServerMessage.CAUGHT_EXCEPTION,
                        Format.value(myException));
            }
        }
        return null;
    }

    private Response<String,String> finalizeExport(List<ValuePair<String,String>> dataListIn,
                                                   Map<String,ProcessedResource> themeMapIn,
                                                   Map<String,ProcessedResource> chosenMapsIn,
                                                   Collection<String> exportedIconsIn, Document documentIn) {
       Response<String,String> result = null;
       File exportFile = null;

       try {
          // Finalize and write the table of contens to the file as XML
          Element myXmlRoot = documentIn.getRootElement();

          // Add the Theme portion of the table of contents
          if ((null != themeMapIn) && !themeMapIn.isEmpty()) {
             Element myXmlThemeList = ContentsHelper.createElement(AclResourceType.THEME, themeMapIn.size());

             for (ProcessedResource myProcessedTheme : themeMapIn.values()) {
                myXmlThemeList.getChildren().add(ContentsHelper.createElement(myProcessedTheme.source));
             }
             myXmlRoot.getChildren().add(myXmlThemeList);
          }
          // Add the Basemap portion of the table of contents
          if ((null != chosenMapsIn) && !chosenMapsIn.isEmpty()) {
             Element myXmlMapList = ContentsHelper.createElement(AclResourceType.MAP_BASEMAP, chosenMapsIn.size());

             for (ProcessedResource myProcessedMap : chosenMapsIn.values()) {
                myXmlMapList.getChildren().add(ContentsHelper.createElement(myProcessedMap.source));
             }
             myXmlRoot.getChildren().add(myXmlMapList);
          }
          if ((null != exportedIconsIn) && !exportedIconsIn.isEmpty()) {
             Element myIconEntry = ContentsHelper.createElement(AclResourceType.ICON, exportedIconsIn.size());

             myXmlRoot.getChildren().add(myIconEntry);
          }
          exportFile = getDownloadFile();
          boolean exceptionDecteced = false;

          try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
             byte[] contents = formatXmlDocument(documentIn).getBytes();
             fileOutputStream.write(getSizeArray(contents));
             fileOutputStream.write(contents);

             // Create and write zipped exports to the file
             try (ZipOutputStream zipStream = new ZipOutputStream(fileOutputStream)) {
                // Add DataView or Template exports
                if ((dataListIn != null) && !dataListIn.isEmpty()) {
                   for (ValuePair<String,String> myResource : dataListIn) {
                      addZipEntry(myResource.getValue1(), myResource.getValue2(), zipStream);
                   }
                }
                // Add Graph Theme and Map Theme exports
                if ((null != themeMapIn) && !themeMapIn.isEmpty()) {
                   // Add exported themes
                   for (ProcessedResource myProcessedTheme : themeMapIn.values()) {
                      Resource myTheme = myProcessedTheme.source;
                      Document myDocument = myProcessedTheme.document;
//                      AclResourceType myThemeType =
//                         (myTheme instanceof GraphTheme)
//                            ? AclResourceType.GRAPH_THEME
//                            : (myTheme instanceof MapTheme) ? AclResourceType.MAP_THEME : AclResourceType.THEME;
                      Map<String,String> grabBag = ExportImportHelper.buildFileNameComponent(myTheme);
                      String myLabel = buildFileName(grabBag, Configuration.getInstance().getExportFileNameConfig().getOrder());

                      try {
                         addZipEntry(myLabel, myDocument, zipStream);
                      } catch (Exception exception) {
                         exceptionDecteced = true;
                         result = new Response<String,String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
                      }
                   }
                }
                // Add Basemap exports
                if (!exceptionDecteced && (chosenMapsIn != null) && !chosenMapsIn.isEmpty()) {
                   // Add esported themes
                   for (ProcessedResource myProcessedMap : chosenMapsIn.values()) {
                      Resource myBasemap = myProcessedMap.source;
                      Document myDocument = myProcessedMap.document;
//                      AclResourceType myType = AclResourceType.MAP_BASEMAP;
                      Map<String,String> grabBag = ExportImportHelper.buildFileNameComponent(myBasemap);
                      String myLabel = buildFileName(grabBag, Configuration.getInstance().getExportFileNameConfig().getOrder());

                      try {
                         addZipEntry(myLabel, myDocument, zipStream);
                      } catch (Exception exception) {
                         exceptionDecteced = true;
                         result = new Response<String,String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
                      }
                   }
                }
                // Add Icon exports
                if (!exceptionDecteced && (exportedIconsIn != null) && !exportedIconsIn.isEmpty()) {
                   // Add Icons to zip stream
                   Map<String,String> grabBag = ExportImportHelper.buildIconListFileName();
                   String label = buildFileName(grabBag, Configuration.getInstance().getExportFileNameConfig().getOrder());
                   Document document = ThemeHelper.createIconDocument(iconActionsService, exportedIconsIn);

                   try (StringWriter writer = createDocumentXml(document)) {
                      zipStream.putNextEntry(new ZipEntry(label));
                      zipStream.write(writer.toString().getBytes());
                      zipStream.closeEntry();
                   }
                }
                zipStream.flush();
             }
             fileOutputStream.flush();
          }
          if (!exceptionDecteced) {
             String fileToken = getDownloadToken(exportFile);
             result = new Response<String,String>(fileToken);
          }
       } catch (Exception exception) {
          if ((exportFile != null) && exportFile.exists()) {
             exportFile.delete();
          }
          result = new Response<String,String>(ServerMessage.CAUGHT_EXCEPTION, Format.value(exception));
       }
       return result;
    }

   private static String formatXmlDocument(Document documentIn) throws IOException {
      String result = null;
      XMLOutputter xmlOutput = new XMLOutputter();

      try (StringWriter writer = new StringWriter()) {
         xmlOutput.setFormat(org.jdom.output.Format.getPrettyFormat());
         xmlOutput.output(documentIn, writer);
         result = writer.toString();
      }
      return result;
   }

   private static void addZipEntry(String labelIn, Document documentIn, ZipOutputStream zipStreamIn)
         throws IOException {
      try {
         String xmlString;
         XMLOutputter xmlOutput = new XMLOutputter();

         try (StringWriter myWriter = new StringWriter()) {
            xmlOutput.setFormat(org.jdom.output.Format.getPrettyFormat());
            xmlOutput.output(documentIn, myWriter);
            xmlString = myWriter.toString();
         }
         zipStreamIn.putNextEntry(new ZipEntry(labelIn));
         zipStreamIn.write(xmlString.getBytes());
         zipStreamIn.closeEntry();
      } finally {
         zipStreamIn.closeEntry();
         zipStreamIn.flush();
      }
   }

   private static void addZipEntry(String labelIn, String documentIn, ZipOutputStream zipStreamIn)
         throws IOException {
      try {
         zipStreamIn.putNextEntry(new ZipEntry(labelIn));
         zipStreamIn.write(ExportImportConstants.XML_HEADER);
         zipStreamIn.write(documentIn.getBytes(StandardCharsets.UTF_8));
         zipStreamIn.closeEntry();
      } finally {
         zipStreamIn.closeEntry();
         zipStreamIn.flush();
      }
   }

    private void addZipEntry(String labelIn, InputStream inputStreamIn, ZipOutputStream zipStreamIn)
            throws IOException {

        try {

            byte[] myBuffer = new byte[BUFFER_SIZE];

            zipStreamIn.putNextEntry(new ZipEntry(labelIn));
            for (int myCount = inputStreamIn.read(myBuffer);
                 -1 != myCount;
                 myCount = inputStreamIn.read(myBuffer)) {

                if (0 < myCount) {

                    zipStreamIn.write(myBuffer);
                }
            }

        } finally {

            zipStreamIn.closeEntry();
            zipStreamIn.flush();
            if (null != inputStreamIn) {

                inputStreamIn.close();
            }
        }
    }

    /**
     * @return File that can be written to for download. The file is associated with a token that can be sent to
     * cause a full download from the browser. The token can be obtained by calling getDownloadToken()
     */
    private File getDownloadFile() {
        String path = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY);
        path += File.separator + UUID.randomUUID().toString() + DownloadServlet.TEMP_FILE_EXT;
        return new File(path);
    }

    private File getDownloadFile(String tokenIn) {
        String path = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY);
        path += File.separator + tokenIn + DownloadServlet.TEMP_FILE_EXT;
        return new File(path);
    }

    /**
     * @param file File reference
     * @return Token for the given file.
     */
    private String getDownloadToken(File file) {
        return file.getName().substring(0, file.getName().length() - DownloadServlet.TEMP_FILE_EXT.length());
    }


    @Override
    public String exportNodesList(List<NodeListDTO> data, List<String> visibleColumns) {
        LOG.info("Exporting Nodes List with size: " + data.size());
        File file = getDownloadFile();
        NodeListCsvWriter writer = new NodeListCsvWriter();
        writer.writeCsv(file, data, visibleColumns);

        return getDownloadToken(file);
    }

    @Override
    public String exportLinksList(List<EdgeListDTO> data, List<String> visibleColumns){
        LOG.info("Exporting Links List with size: " + data.size());
        File file = getDownloadFile();
        LinksListCsvWriter writer = new LinksListCsvWriter();

        writer.writeCsv(file, data, visibleColumns);

        return getDownloadToken(file);

    }

   @Override
   public String exportZipPNG(List<ImagingRequest> req) {
      PNGImageCreator pngImageCreator = new PNGImageCreator();
      String zipPath = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY) + File.separator + UUID.randomUUID().toString() + ".file";

      try (FileOutputStream fout = new FileOutputStream(zipPath);
           BufferedOutputStream bos = new BufferedOutputStream(fout);
           ZipOutputStream zout = new ZipOutputStream(bos)) {
         for (ImagingRequest imReq : req) {
            String myName = (imReq.getName() != null) ? imReq.getName() : "unnamed";
            BufferedImage image = pngImageCreator.createImage(imReq);
            File downloadFile = getDownloadFile();

            writeImageToFile(image, downloadFile);
            zout.putNextEntry(new ZipEntry(ExportImportHelper.formatEscapeString(myName) + ExportImportConstants.PNG_SUFFIX));
            Files.copy(downloadFile.toPath(), zout);
            zout.closeEntry();
            downloadFile.delete();
         }
      } catch (Exception e) {
         LOG.error("Error during Zip creation " + e.getClass() + " " + e.getMessage());
         throw Throwables.propagate(e);
      }
      return getDownloadToken(new File(zipPath));
   }

    private Map<String, Basemap> getAvailableMaps(boolean includeMapsIn) {

        Map<String, Basemap> myAvailableMaps = null;

        if (includeMapsIn) {

            List<Basemap> myBaseMapList = AclRequest.getBasemapsAvoidingSecurity();

            myAvailableMaps = new TreeMap<String, Basemap>();
            for (Basemap myMap : myBaseMapList) {

                myAvailableMaps.put(myMap.getUuid(), myMap);
            }
        }
        return myAvailableMaps;
    }

    private byte[] getSizeArray(byte[] dataIn) {

        int myCount = dataIn.length;

        return new byte[] {(byte)((myCount >> 8) & 0xff), (byte)(myCount & 0xff)};
    }
}
