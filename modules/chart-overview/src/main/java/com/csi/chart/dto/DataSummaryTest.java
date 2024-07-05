package com.csi.chart.dto;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;

//import org.junit.Test;

public class DataSummaryTest
{

//    @Test
    public void testXML() throws JAXBException
    {
        ChartSummary dataSummary = new ChartSummary();
        DimensionSummary axis = new DimensionSummary();
        
        axis.name = "foo";
        axis.type = "other";
        
        dataSummary.dimensions.add(axis);
        
        CategoryRangeData crd = new CategoryRangeData();
        crd.categories.add("bob");
        crd.categories.add("jones");
        
//        axis.range = crd;
        
        
        JAXBContext jaxb = JAXBContext.newInstance(ChartSummary.class);
        Marshaller marshaller = jaxb.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(dataSummary, writer);
        
        
        System.out.println(writer.toString());
    }

}
