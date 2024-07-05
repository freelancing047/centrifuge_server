package csi.client.gwt.viz.shared.export;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Centrifuge Systems, Inc.
 */
public class CSVWriterTest {

    @Test
    public void testWriteArray(){
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        String [] array = { "te,st", "data", "to", "write" };
        csvWriter.writeNext(array);

        String csvText = stringWriter.toString();

        assertEquals(csvText, "\"te,st\",\"data\",\"to\",\"write\"\n");
    }

    @Test
    public void testWriteTwoArrays(){
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        String [] array1 = { "test", "data" };
        String [] array2 = { "to", "write" };
        csvWriter.writeNext(array1);
        csvWriter.writeNext(array2);

        String csvText = stringWriter.toString();
        assertEquals(csvText, "\"test\",\"data\"\n\"to\",\"write\"\n");
    }
}
