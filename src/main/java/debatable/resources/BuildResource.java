package debatable.resources;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/foo")
@Slf4j
public class BuildResource {
    @GET
    private Response getResponse() {
        log.info("Running image: '{}'");
        return Response.ok().build();
    }
}
