//package csi.server.common.codec.flexjson;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.Reader;
//import java.io.UnsupportedEncodingException;
//import java.io.Writer;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeSet;
//
//import csi.server.common.codec.Codec;
//import csi.server.common.codec.CodecType;
//import csi.server.common.dto.CsiMap;
//import csi.server.common.dto.graph.EdgeListing;
//import csi.server.common.dto.graph.NodeListing;
//import flexjson.JSONDeserializer;
//import flexjson.JSONSerializer;
//import flexjson.ObjectBinder;
//import flexjson.ObjectFactory;
//
//public class FlexJsonCodec implements Codec {
//
//    protected CodecType type;
//    protected JSONSerializer serializer;
//
//    @SuppressWarnings("rawtypes")
//    private JSONDeserializer deserializer;
//
//    @SuppressWarnings("rawtypes")
//    public FlexJsonCodec(CodecType type) {
//        this.type = type;
//        initSerializer();
//        initDeserializer();
//    }
//
//    private void initDeserializer() {
//        this.deserializer = new JSONDeserializer();
//        this.deserializer.use(CsiMap.class, new ObjectFactory() {
//
//            public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
//                if (targetType != null) {
//                    if (targetType instanceof ParameterizedType) {
//                        ParameterizedType ptype = (ParameterizedType) targetType;
//                        return context.bindIntoMap((Map) value, new CsiMap<Object, Object>(), ptype.getActualTypeArguments()[0], ptype.getActualTypeArguments()[1]);
//                    }
//                }
//                return context.bindIntoMap((Map) value, new CsiMap<Object, Object>(), null, null);
//            }
//        });
//       this.deserializer.use(TreeSet.class, new ObjectFactory() {
//            @SuppressWarnings({ "rawtypes", "unchecked" })
//            public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
//                if( value instanceof Set) {
//                    return context.bindIntoCollection((Set)value, new TreeSet(), targetType);
//                } else if( value instanceof ArrayList) {
//                    TreeSet<Object> set = new TreeSet<Object>();
//                    ArrayList al = (ArrayList) value;
//                    set.addAll(al);
//                    return set;
//                } else {
//                    TreeSet<Object> set = new TreeSet<Object>();
//                    set.add( context.bind(value) );
//                    return set;
//                }
//
//            }
//            
//        });
//    }
//
//    private void initSerializer() {
//        this.serializer = new JSONSerializer().prettyPrint(true).transform(new GraphItemListTransformer(), NodeListing.class, EdgeListing.class);
//    }
//
//    @Override
//    public CodecType getType() {
//        return this.type;
//    }
//
//    @Override
//    public String getContentType() {
//        return type.getContentType();
//    }
//
//    @Override
//    public String marshal(Object obj) {
//        if (type == CodecType.JSON_SHALLOW) {
//            return serializer.serialize(obj);
//        } else {
//            return serializer.deepSerialize(obj);
//        }
//    }
//
//    @Override
//    public void marshal(Object obj, OutputStream stream) {
//        Writer writer = null;
//        try {
//            writer = new OutputStreamWriter(stream, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        this.marshal(obj, writer);
//    }
//
//    @Override
//    public void marshal(Object obj, Writer writer) {
//
//        try {
//            if (type == CodecType.JSON_SHALLOW) {
//                serializer.serialize(obj, writer);
//            } else {
//                serializer.deepSerialize(obj, writer);
//            }
//            writer.flush();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public Object unmarshal(String data) {
//        return deserializer.deserialize(data);
//    }
//
//    @Override
//    public Object unmarshal(InputStream stream) {
//        Reader reader = null;
//        try {
//            reader = new InputStreamReader(stream, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        return this.unmarshal(reader);
//    }
//
//    @Override
//    public Object unmarshal(Reader reader) {
//        return deserializer.deserialize(reader);
//    }
//
//}
