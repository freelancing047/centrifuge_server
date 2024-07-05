package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import csi.server.common.dto.graph.gwt.EdgeListDTO;

import au.com.bytecode.opencsv.CSVWriter;

public class LinksListCsvWriter  implements CsvWriter{
    @Override
    public void writeCsv(File fileToWrite) {
        //
    }

    private static class LinksFieldHelper{
        private final static String[] headerText = new String[]{"Source", "Target", "Type", "Label", "Hidden", "Selected", "Comment", "Size", "Opacity"};

        public static String getHeaderText(String lbl) {
//            log.info(lbl);
            //weird ones that annoy me
            if(lbl.equals("types") ){
                return headerText[2];
            }
            if(lbl.equals("annotation")){
                return headerText[6];
            }
            if(lbl.equals("width")){
                return headerText[7];
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
            String s = LinksFieldHelper.getHeaderText(visibleCols.get(i));
            if(s != null) {
                header[i] = LinksFieldHelper.getHeaderText(visibleCols.get(i));
            }
        }
        return header;

    }


    // could clean this up a bit
    public void writeCsv(File file, List<EdgeListDTO> data, List<String> visibleCols){
        try(CSVWriter  csvWriter = new CSVWriter(new FileWriter(file))) {
            csvWriter.writeNext(getHeader(visibleCols));

            for(EdgeListDTO edge : data){
                String[] row = new String[visibleCols.size()];
                int currIndex = 0;
                for(String s : visibleCols) {
                    switch (s) {
                        case "source":
                            row[currIndex] = String.valueOf(edge.getSource());
                            break;
                        case "target":
                            row[currIndex] = String.valueOf(edge.getTarget());
                            break;
                        case "types":
                            row[currIndex] = String.valueOf(edge.getAllTypesAsString());
                            break;
                        case "label":
                            row[currIndex] = String.valueOf(edge.getLabel());
                            break;
                        case "hidden":
                            row[currIndex] = String.valueOf(edge.isHidden());
                            break;
                        case "selected":
                            row[currIndex] =  String.valueOf(edge.isSelected());
                            break;
                        case "annotation":
                            row[currIndex] = String.valueOf(edge.hasAnnotation());
                            break;
                        case "width":
                            row[currIndex] = String.valueOf(edge.getWidth());
                            break;
                        case "opacity":
                            row[currIndex] = String.valueOf(edge.getOpacity());
                            break;
                    }
                    currIndex++;
                }

                csvWriter.writeNext(row);
            }

            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
