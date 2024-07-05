package csi.server.business.visualization.mapchart;

import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/*
 * This class converts an AMMap object into the XML that we need for the control
 * on the client.  Ideally, we would have been able to just have XStream generate
 * this for us from the object.  Unfortunately, XStream seems to have problems
 * with creating attributes rather than sub-elements.  
 * 
 * For example, XStream works fine with -
 * <person>
 *    <name>bob</name>
 * </person
 * 
 * But there seems to bug when trying to use an atribute instead -
 * <person name="bob" />
 * 
 * This was verified with both XStream 1.2 and 1.3
 */

/*
 * Modelled this class after EventConverter with extends JavaBeanConverter
 * rather than Converter.  Not sure why?
 */
@SuppressWarnings( { "all", "unchecked", "deprecation", "JavaDoc", "allJavadoc", "unused", "ForLoopReplaceableByForEach", "WhileLoopReplaceableByForEach",
        "SimplifiableConditionalExpression", "StringEquality", "ThrowableResultOfMethodCallIgnored", "serial", "UnusedAssignment", "RedundantCast", "UnnecessaryLocalVariable",
        "SuspiciousMethodCalls", "UnnecessaryReturnStatement", "UnnecessaryBoxing", "ResultOfMethodCallIgnored" })
public class AMMapConverter extends JavaBeanConverter {

    private static final String MAP_BORDER_COLOR = "#000000";
    private static final String MAP_ICON_WIDTH = "10";
    private static final String MAP_ICON_HEIGHT = "10";

    private AMMap map;

    /*
     * This is the main function - expects to get an AMMap object and serializes
     * it into the xml stream.
     */
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

        map = (AMMap) source;
        writer.addAttribute("map_file", "../resources/mapchart/maps/" + map.map_file);
        addMapCallibration(writer);

        if (map.areas != null)
            writeAreas(writer);
        else
            drawBorders(writer);

        if (map.movies != null)
            writeMovies(writer);

    }

    /*
     * If there are no areas in the map, we still need to draw the border
     * outline on the areas. This is the black line between the areas (states on
     * the usa map). AMMap expects this to be an <area> element so we need to
     * create <areas> just for this one <area>. If the map does have areas, we
     * take care of this in the writeMovies method.
     */
    private void drawBorders(HierarchicalStreamWriter writer) {
        writer.startNode("areas");

        // Add an area for the borders between areas - Make these black
        writer.startNode("area");
        writer.addAttribute("mc_name", "borders");
        writer.addAttribute("title", "borders");
        writer.addAttribute("color", MAP_BORDER_COLOR);
        writer.addAttribute("balloon", "false");
        writer.endNode(); // area
        writer.endNode(); // areas
    }

    private void addMapCallibration(HierarchicalStreamWriter writer) {
        if (map.tl_lat != null)
            writer.addAttribute("tl_lat", map.tl_lat);

        if (map.tl_long != null)
            writer.addAttribute("tl_long", map.tl_long);

        if (map.br_lat != null)
            writer.addAttribute("br_lat", map.br_lat);

        if (map.br_long != null)
            writer.addAttribute("br_long", map.br_long);

        if (map.zoom != null)
            writer.addAttribute("zoom", map.zoom);

        if (map.zoom_x != null)
            writer.addAttribute("zoom_x", map.zoom_x);

        if (map.zoom_y != null)
            writer.addAttribute("zoom_y", map.zoom_y);
    }

    private void writeLabel(HierarchicalStreamWriter writer) {
        // If no heat map, no need for label
        if (map.label == null)
            return;

        writer.startNode("labels");
        writer.startNode("label");
        writer.addAttribute("x", "20%");
        writer.addAttribute("y", "5%");
        writer.addAttribute("text_size", "18");
        writer.addAttribute("color", "#FFFFFF");
        writer.startNode("text");
        writer.setValue(map.label);
        writer.endNode(); // text
        writer.endNode(); // label
        writer.endNode(); // labels
    }

    private void writeAreas(HierarchicalStreamWriter writer) {
        List<AMMapArea> areas = map.areas;
        Iterator iterator = areas.iterator();
        writer.startNode("areas");

        // Add an area for the borders between areas - Make these black
        writer.startNode("area");
        writer.addAttribute("mc_name", "borders");
        writer.addAttribute("title", "borders");
        writer.addAttribute("color", "#000000");
        writer.addAttribute("balloon", "false");
        writer.endNode();

        while (iterator.hasNext()) {
            AMMapArea area = (AMMapArea) iterator.next();
            writer.startNode("area");

            // Need to add 'US_' for mercator map of usa.
            if (map.map_file.startsWith("usa"))
                writer.addAttribute("mc_name", "US_" + area.mcName);
            else
                writer.addAttribute("mc_name", area.mcName);
            writer.addAttribute("title", area.title);
            writer.addAttribute("value", area.value.toString());
            writer.endNode();
        }

        writer.endNode(); // areas
    }

    private void writeMovies(HierarchicalStreamWriter writer) {
        List<AMMapMovie> movies = map.movies;
        Iterator iterator = movies.iterator();
        writer.startNode("movies");

        while (iterator.hasNext()) {
            AMMapMovie movie = (AMMapMovie) iterator.next();
            // Make sure that there are lat and long in this record
            if (movie.lattitude.length() > 0 && movie.longitude.length() > 0) {

                writer.startNode("movie");
                writer.addAttribute("file", movie.file);

                if (movie.title != null) {
                    writer.addAttribute("title", movie.title);
                }
                // TODO: need to calculate the dimensions programmatic or break
                // out into comments

                writer.addAttribute("width", Integer.toString(movie.width));
                writer.addAttribute("height", Integer.toString(movie.height));

                if (movie.fixed_size) {
                    writer.addAttribute("fixed_sized", Boolean.TRUE.toString());
                }

                if (movie.color != null) {
                    writer.addAttribute("color", movie.color);
                }

                if (movie.alpha != null) {
                    writer.addAttribute("alpha", movie.alpha);
                }

                writer.addAttribute("long", movie.longitude);
                writer.addAttribute("lat", movie.lattitude);
                if (movie.description != null && movie.description.trim().length() > 0) {
                    writer.startNode("description");
                    writer.setValue(movie.description);
                    writer.endNode();
                } else {
                    writer.addAttribute("balloon", Boolean.FALSE.toString());
                }
                writer.endNode();
            }
        }

        writer.endNode(); // movies
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(AMMap.class);
    }

    public AMMapConverter(Mapper mapper, String classAttributeIdentifier) {
        super(mapper, classAttributeIdentifier);
    }

}
