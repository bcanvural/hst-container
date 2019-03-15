package client.packagename.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.onehippo.cms7.essentials.components.rest.BaseRestResource;

@Path("/hello/")
public class HelloResource extends BaseRestResource {

    @GET
    @Path("/{user}")
    public Response hello(@Context HttpServletRequest request, @PathParam("user") String user) {
        return Response.ok().entity("Hello, World! " + user).build();
    }
}
