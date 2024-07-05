package csi.server.business.publishing.pdf;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.codec.PngImage;

import prefuse.data.Table;
import prefuse.data.Tuple;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.common.dto.TypeNames;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SortOrder;
import csi.server.common.model.chart.ChartDimension;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.extension.ClassificationData;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.publishing.Asset;
import csi.server.common.publishing.pdf.PdfAsset;
import csi.server.dao.CsiPersistenceManager;
import csi.server.dao.jpa.PublishedAssetsBean;
import csi.server.util.SqlUtil;

/**
 * Our use of the iText library appears to be in violation of the terms of the GPL.
 * Issue was  reported to management in September 2013.
 * @author bmurray
 */
public class PdfAssetWriter {
   private static final Logger LOG = LogManager.getLogger(PdfAssetWriter.class);

    public static final int MAX_PAGE_DIM = 14400;

    private static final int CELL_LIMIT = 50000;
    private static final float COLUMN_GAP = 18F;
    private static final int INIT_HEADER_HEIGHT = 75;
    private static final float MARGIN = 36F; // 36
                                             // points
                                             // is
                                             // half
                                             // an
                                             // inch.
    private static final float MAX_COL_WIDTH = 216F;
    private static final float TABLE_PDF_HEIGHT = 492F;

    public Chunk buildClassificationInfoChunk(ClassificationData classData) {
        Font base = new Font(Font.ITALIC, 14.0F);

        String banner = classData.getBanner();
        Color bgColor;
        Color fontColor;

        int i;
        i = classData.getFontColor();
        fontColor = (i > 0) ? new Color(i) : Color.BLACK;

        i = classData.getBackgroundColor();
        bgColor = (i > 0) ? new Color(i) : Color.WHITE;

        base.setColor(fontColor);
        Chunk chunk = new Chunk(banner, base);
        chunk.setBackground(bgColor);

        return chunk;

    }

    private int computeSlot(String olapVal, Number minimum, double range) {
        if ((olapVal == null) || (olapVal.length() == 0)) {
            return -1;
        }
        double val = Double.parseDouble(olapVal);
        double min = minimum.doubleValue();
        double slice = Math.floor(range / 4);

        if (val < (min + slice)) {
            return 0;
        } else if (val < (min + (2 * slice))) {
            return 1;
        } else if (val < (min + (3 * slice))) {
            return 2;
        } else {
            return 3;
        }
    }

    public void createPdfTable(File assetDir, DataView view, TableViewDef tableDef, Asset asset) throws SQLException,
            FileNotFoundException, DocumentException, CentrifugeException {

        List<FieldDef> visibleFieldDefs = tableDef.getTableViewSettings().getVisibleFieldDefs(view.getMeta().getModelDef());
        int colCount = getPdfColumnCount(visibleFieldDefs);

        float[] maxWidths = new float[colCount];

        PdfPTable table = new PdfPTable(colCount);
        table.setSplitRows(false);
        table.setHeaderRows(2);

        Paragraph header1Text = new Paragraph(asset.getDataViewName());
        header1Text.setAlignment(Element.ALIGN_CENTER);
        PdfPCell header1 = new PdfPCell();
        header1.addElement(header1Text);
        header1.setColspan(colCount);
        table.addCell(header1);

        // NB: In the cached table data, the column names
        // are the UUIDs of the corresponding field defs,
        // except that the dashes are replaced with underbars
        // so that SQL does not gak.
        String[] cacheColName = new String[colCount];
        FieldDef[] pdfCols = new FieldDef[colCount];

        int colInx = 0;
        for (FieldDef meta : visibleFieldDefs) {

            if (!isPdfField(meta)) {
                continue;
            }

            float cellWidth = addCell(table, meta.getFieldName(), Element.ALIGN_CENTER, 1);
            maxWidths[colInx] = Math.max(cellWidth, maxWidths[colInx]);
            cacheColName[colInx] = meta.getUuid().replace('-', '_');
            pdfCols[colInx] = meta;

            colInx++;
        }

        java.sql.Connection conn = null;
        ResultSet rows = null;
        int rowCount = 0;
        Document tableDoc = null;
        try {
            conn = CsiPersistenceManager.getCacheConnection();
            DataCacheHelper cacheHelper = new DataCacheHelper();

            rows = cacheHelper.getTableViewData(conn, view, tableDef);
            CacheRowSet rowSet = new CacheRowSet(visibleFieldDefs, rows);
            int cellCount = 0;
            while (rowSet.nextRow()) {
                for (int jj = 0; jj < colCount; jj++) {
                    int alignment = TypeNames.isNumeric(pdfCols[jj].getValueType()) ? Element.ALIGN_RIGHT
                            : Element.ALIGN_LEFT;
                    String cellValue = rowSet.getString(pdfCols[jj]);
                    float cellWidth = addCell(table, cellValue, alignment, 1);
                    cellCount++;
                    if (tableDoc == null) {
                        maxWidths[jj] = Math.max(cellWidth, maxWidths[jj]);
                    }
                }

                if (cellCount > CELL_LIMIT) {
                   LOG.info(String.format("Cell limit of %d exceeded, flush PDF table", CELL_LIMIT));
                    if (tableDoc == null) {
                        setTableWidths(maxWidths, table);
                        tableDoc = createAssetDocument(assetDir, asset, table.getTotalWidth(), TABLE_PDF_HEIGHT);
                    }

                    tableDoc.add(table);
                    table.deleteBodyRows();
                    table.setSkipFirstHeader(true);
                    cellCount = 0;
                }

                rowCount++;
            }

            if (cellCount > 0) {
                // add remaining cells to doc.
                if (tableDoc == null) {
                    setTableWidths(maxWidths, table);
                    tableDoc = createAssetDocument(assetDir, asset, table.getTotalWidth(), TABLE_PDF_HEIGHT);
                }

                tableDoc.add(table);

            } else if (tableDoc == null) {
                // No rows in table; create a doc with an empty table.
                table.setHeaderRows(0);
                setTableWidths(maxWidths, table);
                tableDoc = createAssetDocument(assetDir, asset, table.getTotalWidth(), TABLE_PDF_HEIGHT);
                tableDoc.add(table);
            }
            //
            // NB: do NOT add the empty table if we have already added cells,
            // but no un-added cells remaining.
            // E.g., the page boundary coincided with the end of the data.

            LOG.info(String.format("%d rows written to PDF", rowCount));

        } finally {
            SqlUtil.quietCloseResulSet(rows);
            SqlUtil.quietCloseConnection(conn);
        }

        tableDoc.close();

        return;
    }

    private String getValueKey(int rowInx, int colInx, ArrayList<LinkedHashSet<Object>> sets) {
        StringBuilder sb = new StringBuilder();
        for (int jj = 0; jj < sets.size(); jj++) {
            if ((jj % 2) == 0) {
                String colLabel = getColLabel(colInx, jj, sets);
                appendKey(sb, colLabel);
            } else {
                String rowLabel = getRowLabel(rowInx, jj / 2, sets);
                appendKey(sb, rowLabel);
            }
        }

        return sb.toString();
    }

   @SuppressWarnings("unchecked")
   private HashMap<String, String> getValueMap(Table chartResults) throws CentrifugeException {
      HashMap<String, String> vals = new HashMap<String, String>();
      int colCount = chartResults.getColumnCount();
      Iterator<Tuple> rowIter = chartResults.tuples();
      StringBuilder sb = new StringBuilder();

      while (rowIter.hasNext()) {
         Tuple row = rowIter.next();

         sb.setLength(0);

         for (int dimInx = 0; dimInx < (colCount - 1); dimInx++) {
            appendKey(sb, row.get(dimToTablePos(dimInx, colCount)).toString());
         }
         vals.put(sb.toString(), row.get(colCount - 1).toString());
      }
      return vals;
   }

    private StringBuilder appendKey(StringBuilder sb, String value) {
        return sb.append("([").append(value).append("])");
    }

    private String getColLabel(int colInx, int dimInx, ArrayList<LinkedHashSet<Object>> dimValues) {
        int valOffset = colInx;
        for (int jj = dimInx + 2; jj < dimValues.size(); jj += 2) {
            valOffset /= dimValues.get(jj).size();
        }

        LinkedHashSet<Object> colValues = dimValues.get(dimInx);
        Object[] labels = colValues.toArray();

        Object labelValue = labels[valOffset % colValues.size()];

        return labelValue.toString();
    }

    private String getRowLabel(int rowInx, int lblInx, ArrayList<LinkedHashSet<Object>> dimValues) {
        int valOffset = rowInx;
        for (int jj = (2 * (lblInx + 1)) + 1; jj < dimValues.size(); jj += 2) {
            valOffset /= dimValues.get(jj).size();
        }

        LinkedHashSet<Object> rowValues = dimValues.get((lblInx * 2) + 1);
        Object[] labels = rowValues.toArray();

        Object labelValue = labels[valOffset % rowValues.size()];

        return labelValue.toString();
    }

    private int getOlapColSpan(int colInx, int[] valCounts) {
        int span = 1;

        for (int ii = (colInx + 1) * 2; ii < valCounts.length; ii += 2) {
            span *= valCounts[ii];
        }

        return span;
    }

    private int getOlapColCount(List<LinkedHashSet<Object>> sets) {
        int colCount = 1;

        for (int ii = 0; ii < sets.size(); ii += 2) {

            colCount *= sets.get(ii).size();

        }

        return colCount;
    }

    private int getOlapRowCount(List<LinkedHashSet<Object>> sets) {
        int rowCount = 1;

        for (int ii = 1; ii < sets.size(); ii += 2) {

            rowCount *= sets.get(ii).size();

        }

        return rowCount;
    }

    private String getAttributeNameString(List<ChartDimension> dims) {
        StringBuilder sb = new StringBuilder();

        for (ChartDimension dim : dims) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(dim.getDimName());
        }

        return sb.toString();
    }

    // Map from dimension order to table column order.
    //
    // The Table returned by the chart query has all row dimensions followed by
    // all all column dimensions. The 1st, 3rd, and 5th dimensions are row
    // dimensions,
    // while the 2nd and 4th dimensions are column dimensions. The aggregate
    // result
    // column is the last column.
    //
    // FIXME: we need to specify the number of row and column dimensions in the
    // metadata,
    // instead this horribly complex and inflexible interleaving. This is
    // fall-out
    // from Harris's simplification of the chart interface back in version 1.1.
    private int dimToTablePos(int dimPos, int colCount) throws CentrifugeException {
        int tablePos;

        if (dimPos >= colCount) {

            throw new CentrifugeException(String.format("Inconsistent dimPos, %d, and colCount, %d, in dimToTablePos",
                    dimPos, colCount));

        } else if (dimPos == (colCount - 1)) {

            tablePos = dimPos; // aggregate (result) column.

        } else if ((dimPos % 2) == 0) {

            tablePos = dimPos / 2; // a column-header dimension.

        } else {

            tablePos = (dimPos / 2) + (colCount / 2); // a row-label dimension.
        }

        return tablePos;
    }

    @SuppressWarnings("unchecked")
    /**
     * Return an array where each entry is the distinct value count for
     * a dimension.
     */
    private int[] distinctValueCounts(List<ChartDimension> dims, Table tab) throws CentrifugeException {
        ArrayList<LinkedHashSet<Object>> sets = getDimensionValues(dims, tab);

        // NB - the result table includes a column for the aggregate
        // as well as a column for each dimension.
        int[] colCounts = new int[tab.getColumnCount() - 1];

        for (int jj = 0; jj < colCounts.length; jj++) {
            colCounts[jj] = sets.get(jj).size();
        }

        return colCounts;
    }

    private int[] distinctValueCounts(ArrayList<LinkedHashSet<Object>> sets) throws CentrifugeException {

        // NB - the result table includes a column for the aggregate
        // as well as a column for each dimension.
        int[] colCounts = new int[sets.size()];

        for (int jj = 0; jj < colCounts.length; jj++) {
            colCounts[jj] = sets.get(jj).size();
        }

        return colCounts;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<LinkedHashSet<Object>> getDimensionValues(final List<ChartDimension> dims, Table tab)
            throws CentrifugeException {
        int dimCount = tab.getColumnCount() - 1;

        ArrayList<ArrayList<Object>> lists = new ArrayList<ArrayList<Object>>();
        for (int jj = 0; jj < dimCount; jj++) {
            lists.add(new ArrayList<Object>());
        }

        Iterator<Tuple> rowIter = tab.tuples();

        while (rowIter.hasNext()) {
            Tuple row = rowIter.next();

            for (int jj = 0; jj < dimCount; jj++) {
                int tblPos = dimToTablePos(jj, tab.getColumnCount());
                lists.get(jj).add(row.get(tblPos).toString());
            }
        }

        for (int jj = 0; jj < dimCount; jj++) {
            final int dimInx = jj;
            Collections.sort(lists.get(jj), new Comparator() {

                @Override
                public int compare(Object arg0, Object arg1) {
                    int cmpRtn;
                    ChartDimension chartDim = dims.get(dimInx);

                    String s0 = arg0.toString();
                    String s1 = arg1.toString();

                    if (chartDim.isNumeric()) {
                        try {
                            Double d0 = Double.valueOf(s0);
                            Double d1 = Double.valueOf(s1);
                            cmpRtn = (SortOrder.ASC == chartDim.getSortOrder()) ? d0.compareTo(d1) : d1
                                    .compareTo(d0);
                        } catch (NumberFormatException nfe) {
                            cmpRtn = (SortOrder.ASC == chartDim.getSortOrder()) ? s0.compareTo(s1) : s1
                                    .compareTo(s0);
                        }
                    } else {
                        cmpRtn = SortOrder.ASC.equals(chartDim.getSortOrder()) ? s0.compareTo(s1) : s1.compareTo(s0);
                    }
                    // TODO Auto-generated method stub
                    return cmpRtn;
                }

            });
        }

        ArrayList<LinkedHashSet<Object>> sets = new ArrayList<LinkedHashSet<Object>>();
        for (int jj = 0; jj < dimCount; jj++) {
            LinkedHashSet<Object> attrSet = new LinkedHashSet<Object>();
            attrSet.addAll(lists.get(jj));
            sets.add(attrSet);
        }

        return sets;
    }

    private boolean isPdfField(FieldDef def) {
        return (FieldType.STATIC != def.getFieldType());
    }

    private int getPdfColumnCount(List<FieldDef> fieldDefs) {
        int pdfColumnCount = 0;
        for (FieldDef def : fieldDefs) {
            if (isPdfField(def)) {
                pdfColumnCount++;
            }
        }

        return pdfColumnCount;
    }

    private void setTableWidths(float[] maxWidths, PdfPTable table) throws DocumentException {
        table.setWidths(maxWidths);
        float totalWidth = 0F;
        for (float width : maxWidths) {
            totalWidth += width;
        }

        totalWidth += (maxWidths.length - 1F) * COLUMN_GAP;

        table.setTotalWidth(totalWidth);
        table.setLockedWidth(true);
        table.setWidthPercentage(100F);
    }

    private float addCell(PdfPTable table, String contents, int alignment, int cellSpan) {
        return addCell(table, contents, alignment, cellSpan, -1);
    }

    private float addCell(PdfPTable table, String contents, int alignment, int cellSpan, int slot) {
        Chunk chunk = new Chunk((contents == null) ? "" : contents.trim());
        Paragraph cellPP = new Paragraph(chunk);
        cellPP.setAlignment(alignment);
        PdfPCell cell = new PdfPCell();
        cell.addElement(cellPP);
        cell.setColspan(cellSpan);

        if (slot != -1) {
            cell.setBackgroundColor(getSlotColor(slot));
        }

        table.addCell(cell);

        float width = chunk.getWidthPoint();
        float padding = cell.getEffectivePaddingLeft() + cell.getEffectivePaddingRight();

        return Math.min(width + padding, MAX_COL_WIDTH * cellSpan);
    }

    private Color getSlotColor(int slot) {
        if (slot == 0) {
            return new Color(0x1188ff);
        } else if (slot == 1) {
            return new Color(0xc184ff);
        } else if (slot == 2) {
            return new Color(0xff6666);
        } else if (slot >= 3) {
            return new Color(0xff972f);
        } else {
            return Color.WHITE;
        }
    }

    // TODO: update to create hierarchy based on UUID of
    // the asset -- avoid large # of files in single directory.
    public void createPDF(File rootDir, String importID, InputStream pngStream) throws IOException, DocumentException {
        PublishedAssetsBean dao = new PublishedAssetsBean();

        PdfAsset asset = (PdfAsset) dao.findByAssetID(importID);
        if (asset == null) {
            throw new RuntimeException("Asset '" + importID + "' not found.");
        }

        Image png = PngImage.getImage(pngStream);
        pngStream.close();

        float docWidth = asset.getWidth().floatValue() + (2F * MARGIN);
        float docHeight = asset.getHeight().floatValue() + (2F * MARGIN) + INIT_HEADER_HEIGHT;

        Document document = createAssetDocument(rootDir, asset, docWidth, docHeight);

        png.setAbsolutePosition(MARGIN, MARGIN);
        document.add(png);

        document.close();
    }

    private Document createAssetDocument(File rootDir, Asset asset, float contentWidth, float docHeight)
            throws DocumentException, FileNotFoundException {
        float docWidth = contentWidth + (2 * MARGIN);

        Document document = new Document(new Rectangle(docWidth, docHeight));
        document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
        File pdfFile = new File(rootDir, asset.getAssetID() + ".pdf");
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

        // Note: since we're adding the classification as header/footer. this
        // operation needs to be
        // performed prior to opening the document. This way all pages created
        // get these settings.
        addClassificationIfPresent(asset, document);

        document.open();
        writeAssetTitle(asset, document);

        return document;
    }

    private void writeAssetTitle(Asset asset, Document document) throws DocumentException {
        // HeaderFooter header = new HeaderFooter(new
        // Phrase("TS//TK/SI/NOFORN"), false);
        // HeaderFooter footer = new HeaderFooter(new
        // Phrase("TS//TK/SI/NOFORN"), true);
        // document.setHeader(header);
        // document.setFooter(footer);

        Paragraph title = new Paragraph(asset.getName(), new Font(Font.BOLD, 14.0F));
        title.setSpacingAfter(2.0F);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

//        DataView dataView = CsiPersistenceManager.findObject(DataView.class, asset.getAssetID());
        addClassificationIfPresent(asset, document);

        document.add(normalLine("Created by", asset.getCreatedBy(), 0F));
        document.add(normalLine("Created on", asset.getCreationTime().toString(), 6F));
        document.addCreationDate();
        document.addTitle(asset.getName());
        document.addHeader("comments", asset.getDescription());

    }

    private void addClassificationIfPresent(Asset asset, Document document) throws DocumentException {

        // ClassificationInfo classInfo = asset.getClassificationInfo();
        ClassificationData cd = asset.getClassificationData();
        if (cd == null) {
            return;
        }

        Chunk classChunk = buildClassificationInfoChunk(cd);
        Phrase phrase = new Phrase(classChunk);

        HeaderFooter header = new HeaderFooter(phrase, false);
        header.setAlignment(Element.ALIGN_CENTER);

        HeaderFooter footer = new HeaderFooter(phrase, true);
        footer.setAlignment(Element.ALIGN_CENTER);

        document.setHeader(header);

        // iText doesn't treat footers like an add-on! All of the measurements
        // must be pre-calculated
        // to account for our header info + pictures. This ripples throughout
        // our construction since
        // we're dealing with a patchwork of images. Holding off on including
        // the class banner in
        // the footer until we can resolve the best way to change this.
        // document.setFooter(footer);

    }

    private ClassificationData getClassificationInfo(DataView dataView) {
        List<ExtensionData> extensionData = dataView.getMeta().getExtensionData();
        for (ExtensionData d : extensionData) {
            if (d instanceof ClassificationData) {
                ClassificationData cd = (ClassificationData) d;
                return cd;
            }
        }

        return null;
    }

    /**
     * Add 2nd, 3rd, etc. tile to image, if it requires more than a single tile.
     *
     * @param assetId
     *            UUID of asset that is being published.
     * @param tileNumber
     *            zero-based tile number. First we get is the 2nd tile,
     *            tileNumber is 1.
     * @param tileWidth
     *            standard tile width
     * @param tileHeight
     *            standard tile height
     * @param pngStream
     *            stream of image to embed
     * @throws IOException
     * @throws DocumentException
     */
    public void addImageTile(File root, String assetId, int tileNumber, int tileWidth, int tileHeight,
            InputStream pngStream) throws IOException, DocumentException {
        if (!root.exists()) {
            String eMsg = String.format("Asset directory '%s' not found", root.getPath());
            LOG.error("addImageTile: " + eMsg);
            throw new RuntimeException(eMsg);
        }

        PublishedAssetsBean dao = new PublishedAssetsBean();

        PdfAsset asset = (PdfAsset) dao.findByAssetID(assetId);
        if (asset == null) {
            String eMsg = String.format("Asset '%s' not found", assetId);
            LOG.error("updatePDF: " + eMsg);
            throw new RuntimeException(eMsg);
        }

        File pdfFile = new File(root, assetId + ".pdf");
        if (!pdfFile.exists()) {
            String eMsg = "PDF file to update, '" + pdfFile.toString() + "', not found";
            LOG.warn("addImageTile: " + eMsg);
            throw new RuntimeException(eMsg);
        }

        PdfReader reader = new PdfReader(new FileInputStream(pdfFile));

        ByteArrayOutputStream updateStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, updateStream);
        Image png = PngImage.getImage(pngStream);

        setImageXY(png, tileNumber, tileWidth, tileHeight, asset);
        PdfContentByte cb = stamper.getOverContent(1);
        cb.addImage(png);

        stamper.close();

        reader.close();

        FileOutputStream pdfOut = new FileOutputStream(pdfFile);

        IOUtils.copy(new ByteArrayInputStream(updateStream.toByteArray()), pdfOut);

        pdfOut.close();

        return;
    }

    /**
     * Set the <x,y> coordinates for an image tile in the PDF.
     *
     * For an image in a PDF, the position is the lower-left corner. The origin
     * (i.e., position <0, 0>) of the doc is its lower-left corner.
     *
     * Our app numbers tiles as follows:
     *
     * etc., etc. 16 17 18 19 12 13 14 15 8 9 10 11 4 5 6 7 0 1 2 3
     *
     * All tiles are the same size, except for the right-most column and the top
     * row. If standard size tiles do not fit exactly into the doc, the smaller
     * tiles are on the right-most column and the top row.
     *
     * @param png
     *            PNG image of the tile to be inserted in a PDF.
     * @param tileNumber
     *            zero-based number of the tile, starting at the lower-left-hand
     *            corner and numbering row by row, from right to left.
     * @param tileWidth
     *            width of a "standard" (i.e., not on left-hand border) tile, in
     *            points.
     * @param tileHeight
     *            height of a "standard" (i.e., not on upper border) tile, in
     *            points.
     * @param asset
     *            meta data describing the stored image. We get image width from
     *            this object.
     */
    private void setImageXY(Image png, int tileNumber, int tileWidth, int tileHeight, PdfAsset asset) {
        int tilesPerRow = ((asset.getWidth() + tileWidth) - 1) / tileWidth;
        int row = tileNumber / tilesPerRow;
        int col = tileNumber - (row * tilesPerRow);

        float imageX = (col * tileWidth) + MARGIN; // Assume left &
                                                           // bottom margins
        float imageY = (row * tileHeight) + MARGIN; // are the same.

        png.setAbsolutePosition(imageX, imageY);

        return;

    }

    private Paragraph normalLine(String phrase, String info, Float spaceAfter) throws DocumentException {
        Paragraph creator = new Paragraph(phrase + " " + info, new Font(Font.NORMAL, 10.F));
        creator.setSpacingAfter(spaceAfter);
        creator.setAlignment(Element.ALIGN_LEFT);

        return creator;
    }

}
