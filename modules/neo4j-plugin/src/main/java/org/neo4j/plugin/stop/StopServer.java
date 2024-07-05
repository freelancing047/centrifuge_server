package org.neo4j.plugin.stop;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/stop")
public class StopServer {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/now")
    public Response getAllNodes(@PathParam("nodeId") long nodeId) {
        System.exit(0);
        return null;
    }
}
