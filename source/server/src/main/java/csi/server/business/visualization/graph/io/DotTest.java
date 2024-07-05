package csi.server.business.visualization.graph.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

public class DotTest {

    static String edgeInput = "digraph EY {\n" + "Brian -> Bob;\n" + "Bob -> Acct1;\n" + "Debbie -> Mary;\n" + "Mary -> Acct1;\n" + "Debbie -> Sally;\n" + "Sally -> Acct2;\n"
            + "Brian -> Ed;\n" + "Ed -> Acct2;\n" + "Brian -> Casey;\n" + "Casey -> Acct2;\n" + "}";

    static String results = "digraph EY {\n" + "node [label=\"\\N\"];\n" + "graph [bb=\"0,0,368,180\"];\n" + "Brian [pos=\"255,162\", width=\"0.86111\", height=\"0.5\"];\n"
            + "Bob [pos=\"183,90\", width=\"0.75\", height=\"0.5\"];\n" + "Acct1 [pos=\"101,18\", width=\"0.91667\", height=\"0.5\"];\n"
            + "Debbie [pos=\"70,162\", width=\"1.0278\", height=\"0.5\"];\n" + "Mary [pos=\"31,90\", width=\"0.86111\", height=\"0.5\"];\n"
            + "Sally [pos=\"109,90\", width=\"0.80556\", height=\"0.5\"];\n" + "Acct2 [pos=\"255,18\", width=\"0.91667\", height=\"0.5\"];\n"
            + "Ed [pos=\"255,90\", width=\"0.75\", height=\"0.5\"];\n" + "Casey [pos=\"334,90\", width=\"0.94444\", height=\"0.5\"];\n"
            + "Brian -> Bob [pos=\"e,198.1,105.1 239.38,146.38 229.43,136.43 216.39,123.39 205.35,112.35\"];\n"
            + "Bob -> Acct1 [pos=\"e,118.42,33.294 166.41,75.43 154.86,65.289 139.25,51.582 126.17,40.102\"];\n"
            + "Debbie -> Mary [pos=\"e,40.354,107.27 60.559,144.57 55.96,136.08 50.334,125.69 45.228,116.27\"];\n"
            + "Mary -> Acct1 [pos=\"e,85.508,33.934 46.535,74.021 55.87,64.42 67.913,52.033 78.3,41.349\"];\n"
            + "Debbie -> Sally [pos=\"e,99.646,107.27 79.441,144.57 84.04,136.08 89.666,125.69 94.772,116.27\"];\n"
            + "Sally -> Acct2 [pos=\"e,230.4,30.132 131.81,78.75 155.77,66.933 193.72,48.221 221.17,34.685\"];\n"
            + "Brian -> Ed [pos=\"e,255,108.41 255,143.83 255,136.13 255,126.97 255,118.42\"];\n"
            + "Ed -> Acct2 [pos=\"e,255,36.413 255,71.831 255,64.131 255,54.974 255,46.417\"];\n"
            + "Brian -> Casey [pos=\"e,316.72,105.75 271.75,146.73 282.6,136.84 296.92,123.79 309.1,112.7\"];\n"
            + "Casey -> Acct2 [pos=\"e,272.11,33.592 316.86,74.377 305.99,64.47 291.75,51.494 279.67,40.488\"];\n" + "}\n";

    public static void main(String[] args) {
       try {
          DotPositionReader dotReader = new DotPositionReader();

          DotWriter dotWriter = new DotWriter();

          boolean directed = true;
          Graph graph = new Graph(directed);
          Table nodeTable = graph.getNodeTable();
          nodeTable.addColumn("Name", String.class);
          Node brian = graph.addNode();
          brian.setString("Name", "brian");
          Node bob = graph.addNode();
          brian.setString("Name", "bob");
          Node debbie = graph.addNode();
          brian.setString("Name", "debbie");
          Node mary = graph.addNode();
          brian.setString("Name", "mary");
          Node ed = graph.addNode();
          brian.setString("Name", "ed");
          Node sally = graph.addNode();
          brian.setString("Name", "sally");
          Node casey = graph.addNode();
          brian.setString("Name", "casey");
          Node acct1 = graph.addNode();
          brian.setString("Name", "acct1");
          Node acct2 = graph.addNode();
          brian.setString("Name", "acct2");

          graph.addEdge(brian, bob);
          graph.addEdge(bob, acct1);
          graph.addEdge(debbie, mary);
          graph.addEdge(mary, acct1);
          graph.addEdge(debbie, sally);
          graph.addEdge(sally, acct2);
          graph.addEdge(brian, ed);
          graph.addEdge(ed, acct2);
          graph.addEdge(brian, casey);
          graph.addEdge(casey, acct2);

          StringWriter source = new StringWriter();
          PrintWriter writer = new PrintWriter(source);
          dotWriter.write(graph, writer);

          File f = new File("test.dot");

          try (FileOutputStream outputStream = new FileOutputStream(f)) {
             writer = new PrintWriter(outputStream);

             writer.write(source.toString());
             writer.flush();
          }
          List<String> commands = new ArrayList<String>();

          commands.add("C:\\Program Files\\Graphviz2.26.3\\bin\\dot.exe");
          commands.add("-Tdot");
          commands.add(f.getAbsolutePath());

          String[] dotArgs = commands.toArray(new String[0]);

          Process dotProcess = Runtime.getRuntime().exec(dotArgs);
          StringBuilder dotResults = new StringBuilder();

          try (InputStream istream = dotProcess.getInputStream();
               InputStreamReader inputStreamReader = new InputStreamReader(istream);
               BufferedReader reader = new BufferedReader(inputStreamReader)) {
             String line;

             while ((line = reader.readLine()) != null) {
                dotResults.append(line);
                dotResults.append('\n');
             }
          }
          System.out.println(dotProcess.exitValue());

          System.out.println(source.toString());

          dotReader.read(null, new BufferedReader(new StringReader(dotResults.toString())));

          dotReader = new DotPositionReader();

          try (FileReader fileReader = new FileReader(new File("C:/Documents and Settings/eduardo/My Documents/test.dot"));
               BufferedReader buf = new BufferedReader(fileReader)) {
             dotReader.read(null, buf);
          }
       } catch (Exception ignored) {
       }
    }
}
