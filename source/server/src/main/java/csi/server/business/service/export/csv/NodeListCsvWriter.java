package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import csi.server.common.dto.graph.gwt.NodeListDTO;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes nodelist to csv, note: default writeCSV isn't used here, because we need more params.
 */
public class NodeListCsvWriter implements CsvWriter{
    @Override
    public void writeCsv(File fileToWrite) {
        // default, not used.
    }


    protected static class NodeFieldHelper {
        private static String[] headerText = new String[]{"Label",
                    "Bundle Name", "Betweenness", "Closeness", "Eigenvector", "Degrees", "Visible Neighbors", "Component", "Type", "Size","Opacity", "Bundled", "Selected", "Anchored", "Hide Labels", "Hidden", "Visualized", "Comment"};

        public static String getHeaderText(String lbl) {
            if(lbl.equals("bundleNodeLabel")){
                return headerText[1];
            }

            if(lbl.equals("transparency")){
                return headerText[10];
            }

            if(lbl.equals("isBundle")){
                return "Bundle";
            }
            if(lbl.equals("annotation")){
                return "Comment";
            }

            for (String s : headerText) {
                if (s.replace(" ", "").equalsIgnoreCase(lbl)) {
                    return s;
                }
            }
            return null;
        }

    }



    private String[] getHeader(List<String> visibleCols){
        String[] header = new String[visibleCols.size()];

        for(int i = 0; i<header.length; i++){
            String s = NodeFieldHelper.getHeaderText(visibleCols.get(i));
            if(s != null) {
                header[i] = NodeFieldHelper.getHeaderText(visibleCols.get(i));
            }
        }
        return header;

    }



    public void writeCsv(File file, List<NodeListDTO> list, List<String> visibleCols) {
        try(CSVWriter  csvWriter = new CSVWriter(new FileWriter(file))) {
            visibleCols.size();

            csvWriter.writeNext(getHeader(visibleCols));

            for (NodeListDTO node : list) {
                String[] row = new String[visibleCols.size()];
                int currIndex = 0;
                for(String s : visibleCols){
                    switch (s) {
                        case "size":
                            row[currIndex] = String.valueOf(node.getSize());
                            break;
                        case "visibleNeighbors":
                            row[currIndex] = String.valueOf(node.getVisibleNeighbors());
                            break;
                        case "betweenness":
                            row[currIndex] = String.valueOf(node.getBetweenness());
                            break;
                        case "selected":
                            row[currIndex] = String.valueOf(node.isSelected());
                            break;
                        case "annotation":
                            row[currIndex] = String.valueOf(node.hasAnnotation());
                            break;
                        case "bundled":
                            row[currIndex] = String.valueOf(node.isBundled());
                            break;
                        case "hidden":
                            row[currIndex] = String.valueOf(node.isHidden());
                            break;
                        case "visualized":
                            row[currIndex] = String.valueOf(node.getVisualized());
                            break;
                        case "closeness":
                            row[currIndex] = String.valueOf(node.getCloseness());
                            break;
                        case "eigenvector":
                            row[currIndex] = String.valueOf(node.getEigenvector());
                            break;
                        case "degrees":
                            row[currIndex] = String.valueOf(node.getDegrees());
                            break;
                        case "component":
                            row[currIndex] = String.valueOf(node.getComponent());
                            break;
                        case "bundleNodeLabel":
                            row[currIndex] = String.valueOf(node.getBundleNodeLabel());
                            break;
                        case "hideLabels":
                            row[currIndex] = String.valueOf(node.getHideLabels());
                            break;
                        case "anchored":
                            row[currIndex] = String.valueOf(node.isAnchored());
                            break;
                        case "label":
                            row[currIndex] = String.valueOf(node.getLabel());
                            break;
                        case "type":
                            row[currIndex] = String.valueOf(node.getType());
                            break;
                        case "transparency":
                            row[currIndex] = String.valueOf(node.getTransparency());
                            break;
                        case "isBundle":
                            row[currIndex] = String.valueOf(node.isBundle());
                            break;
                        default:
                            row[currIndex] = "ERROR";
                    }
                    currIndex++;
                }

                csvWriter.writeNext(row);
            }

            csvWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}