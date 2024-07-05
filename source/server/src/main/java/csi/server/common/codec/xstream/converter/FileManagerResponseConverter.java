package csi.server.common.codec.xstream.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import csi.server.common.dto.DirectoryData;
import csi.server.common.dto.FileData;
import csi.server.common.dto.FileManagerResponseData;

public class FileManagerResponseConverter implements Converter {

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        FileManagerResponseData data = (FileManagerResponseData) o;

        for (FileData file : data.getFile()) {
            writer.startNode("file");
            addAttribute(writer, "fname", file.getFname());
            addAttribute(writer, "name", file.getName());
            addAttribute(writer, "path", file.getPath());
            addAttribute(writer, "url", file.getUrl());
            addAttribute(writer, "size", file.getSize());
            addAttribute(writer, "token", file.getToken());
            addAttribute(writer, "urltoken", file.getUrltoken());
            addAttribute(writer, "lastmodified", file.getLastmodified());
            writer.endNode();
        }

        for (DirectoryData dirData : data.getDirectory()) {
            writer.startNode("directory");
            addAttribute(writer, "name", dirData.getName());
            addAttribute(writer, "size", dirData.getSize());
            addAttribute(writer, "path", dirData.getPath());
            addAttribute(writer, "token", dirData.getToken());
            addAttribute(writer, "url", dirData.getUrl());
            addAttribute(writer, "urltoken", dirData.getUrltoken());
            writer.endNode();
        }

    }

    private void addAttribute(HierarchicalStreamWriter writer, String alias, String value) {
        if (value != null) {
            writer.addAttribute(alias, value);
        }
    }

    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        return null;
    }

    public boolean canConvert(Class aClass) {
        return aClass.equals(FileManagerResponseData.class);
    }
}
