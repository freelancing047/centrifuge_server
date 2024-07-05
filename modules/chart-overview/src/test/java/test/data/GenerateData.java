package test.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GenerateData
{

    @SuppressWarnings("rawtypes")
    public static void main( String[] args ) throws IOException
    {

        int maxValue = 100;
        int nCategories = 50;
        int nSeries = 1;
        
        Set<String> names = new HashSet<String>();
        for( int i=0; i < nCategories; i++ ) {
            String name = UUID.randomUUID().toString();
            names.add(name);
        }
        
        Multimap<String, String> matrix = HashMultimap.create();
        for( int i=0; i < nSeries; i++ ) {
            String series = UUID.randomUUID().toString();
            matrix.putAll(series, names);
        }
        
        
        Wrapper wrapper = new Wrapper();
        wrapper.data = new Tuple[nSeries*nCategories];
        int index=0;
        for(String series : matrix.keySet()) {
            Collection<String> categories = matrix.get(series);
            for(String cat : categories) {
                Tuple t = new Tuple();
                t.categories = new ArrayList<Object>();
                t.categories.add(series);
                t.categories.add(cat);
                t.value = (int)(Math.random()*maxValue);
                wrapper.data[index++] = t;
            }
        }

        Gson codec = new GsonBuilder().setPrettyPrinting().create();
        String json = codec.toJson(wrapper);
        FileWriter writer = new FileWriter("data.json");

        writer.write(json);
        writer.flush();
        writer.close();
    }

}
