package dk.mmj.evhe.publicServer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class PublicServerResource {

    @GET
    @Path("type")
    @Produces(MediaType.TEXT_HTML)
    public String test() {
        return "<b>ServerType:</b> Public Server";
    }
}
