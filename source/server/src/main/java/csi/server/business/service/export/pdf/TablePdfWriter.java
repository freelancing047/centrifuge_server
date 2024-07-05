package csi.server.business.service.export.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.common.dto.TypeNames;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TablePdfWriter implements PdfWriter {

    private static final float MAX_COL_WIDTH = 216f;
    private static final float TABLE_PDF_HEIGHT = 492f;
    private static final float MARGIN = 36f;
    private static final float COLUMN_GAP = 18F;

    private final DataView dataView;
    private final TableViewDef visualizationDef;
    private final List<Float> maxWidths = new ArrayList<Float>();

    private Document tableDocument = null;

    public TablePdfWriter(String dvUuid, TableViewDef visualizationDef) {
        dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        this.visualizationDef = visualizationDef;
    }

    @Override
    public void writePdf(File fileToWrite) {
        List<FieldDef> visibleFieldDefs = visualizationDef.getTableViewSettings().getVisibleFieldDefs(dataView.getMeta().getModelDef());
        int columnCount = visibleFieldDefs.size();

        PdfPTable table = initPdfTable(columnCount);
        setPdfTitle(table, dataView.getName(), columnCount);

        writeHeader(table, visibleFieldDefs);

        try {
            writeRows(table);
            setTableWidths(table);
            tableDocument = createDocument(table, fileToWrite);
            tableDocument.add(table);
        } catch (Exception e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
        finally {
            tableDocument.close();
        }

    }

    private void setTableWidths(PdfPTable table) throws Exception{
        float [] widths = createMaxWidthsFloatArray();
        table.setWidths(widths);

        float totalWidth = 0F;
        for (float width : maxWidths) {
            totalWidth += width;
        }

        totalWidth += (maxWidths.size() - 1F) * COLUMN_GAP;

        table.setTotalWidth(totalWidth);
        table.setLockedWidth(true);
        table.setWidthPercentage(100F);
    }

    private float[] createMaxWidthsFloatArray() {
        float [] arr = new float[maxWidths.size()];
        for(int i = 0; i < maxWidths.size(); i++){
            arr[i] = maxWidths.get(i);
        }
        return arr;
    }

    private Document createDocument(PdfPTable table, File fileToWrite) throws Exception{
        float contentWidth = table.getTotalWidth() + (2 * MARGIN);

        Document document = new Document(new Rectangle(contentWidth, TABLE_PDF_HEIGHT));
        document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(fileToWrite));
        document.open();
        return document;
    }

    private void writeHeader(PdfPTable table, List<FieldDef> visibleFieldDefs) {
        for(FieldDef fieldDef : visibleFieldDefs){
            float cellWidth = addCell(table, fieldDef.getFieldName(), Element.ALIGN_CENTER, 1, null);
            maxWidths.add(cellWidth);
        }
    }

    private float addCell(PdfPTable table, String contents, int alignment, int cellSpan, Color color) {
        Chunk chunk = new Chunk((contents == null) ? "" : contents.trim());
        Paragraph cellParagraphs = new Paragraph(chunk);
        cellParagraphs.setAlignment(alignment);

        PdfPCell cell = new PdfPCell();
        cell.addElement(cellParagraphs);
        cell.setColspan(cellSpan);
        if(color != null) {
         cell.setBackgroundColor(color);
      }

        table.addCell(cell);

        float width = chunk.getWidthPoint();
        float padding = cell.getEffectivePaddingLeft() + cell.getEffectivePaddingRight();
        return Math.min(width + padding, MAX_COL_WIDTH * cellSpan);
    }

    private void writeRows(PdfPTable table) throws Exception {
       DataCacheHelper cacheHelper = new DataCacheHelper();

       try (Connection connection = CsiPersistenceManager.getCacheConnection();
            ResultSet resultSet = cacheHelper.getTableViewData(connection, dataView, visualizationDef)) {
          writeEachRow(table, resultSet);
       }
    }

    private void writeEachRow(PdfPTable table, ResultSet resultSet) throws Exception{
        List<FieldDef> visibleFieldDefs = visualizationDef.getTableViewSettings().getVisibleFieldDefs(dataView.getMeta().getModelDef());
        CacheRowSet rowSet = new CacheRowSet(visibleFieldDefs, resultSet);

        while (rowSet.nextRow()) {
            writeRow(table, visibleFieldDefs, rowSet);
        }
    }

    private void writeRow(PdfPTable table, List<FieldDef> visibleFieldDefs, CacheRowSet rowSet) {
        int i = 0;
        for(FieldDef fieldDef : visibleFieldDefs){
            int alignment = TypeNames.isNumeric(fieldDef.getValueType()) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT;
            String cellValue = rowSet.getString(fieldDef);
            float cellWidth = addCell(table, cellValue, alignment, 1, null);
            maxWidths.set(i,Math.max(cellWidth, maxWidths.get(i)));
            i++;
        }
    }

    private PdfPTable initPdfTable(int columnCount) {
        PdfPTable table = new PdfPTable(columnCount);
        table.setSplitRows(false);
        table.setHeaderRows(2);
        return table;
    }

    private void setPdfTitle(PdfPTable table, String dataViewName, int columnCount) {
        Paragraph header1Text = new Paragraph(dataViewName);
        header1Text.setAlignment(Element.ALIGN_CENTER);
        PdfPCell header1 = new PdfPCell();
        header1.addElement(header1Text);
        header1.setColspan(columnCount);
        table.addCell(header1);
    }
}
