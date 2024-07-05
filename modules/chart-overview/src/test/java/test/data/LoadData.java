package test.data;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoadData
{
    public LoadData()  
    {
        
    }
    
    
    public Wrapper load(String file) throws FileNotFoundException {
        FileReader reader = new FileReader(file);
        
        GsonBuilder builder = new GsonBuilder();
        
        Gson codec = builder.create();
        
        Wrapper wrapper = codec.fromJson(reader, Wrapper.class);
        
        return wrapper;
    }

}
