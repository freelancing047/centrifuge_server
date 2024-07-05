package com.csi.chart;

import java.util.Collection;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.csi.util.data.ExpressionRegistry;

@Path(value="api/charting")
public class ChartCapabilitiesResource
{

    @GET
    @Path("functions")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> listFunctions( @QueryParam("type") String dataType )
    {
        if( dataType == null ) {
            ResponseBuilder builder = Response.status(Status.BAD_REQUEST).entity("query parameter type is required");
            builder.type(MediaType.TEXT_PLAIN);
            Response response = builder.build();
            throw new WebApplicationException(response);
        }
        
        dataType = dataType.toLowerCase();

        ExpressionRegistry registry = ExpressionRegistry.instance();
        Collection<String> functionList = registry.getFunctionsByType(dataType);
        return functionList;
    }

}
