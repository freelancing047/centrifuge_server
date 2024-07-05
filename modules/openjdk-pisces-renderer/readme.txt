OpenJDK 7 Pisces Renderer

This is a snapshot of the OpenJDK Pisces renderer.  No code has been changed; merely a source import for controlled
compilation and distribution.

To use the Pisces Renderer the following steps should be performed:

1. mvn package.  This will compile the source and produce a jar file located in the target directory.

2. Copy the resulting jar into your jre's lib/ext directory.  The java2d RenderingEngine utilizes 
   Java's ServiceLoader approach.

3. Set the JVM property sun.java2d.renderer to point to the Pisces Renderer:
      
      java -Dsun.java2d.renderer=sun.java2d.pisces.PiscesRenderingEngine
      
