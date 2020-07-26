package debatable.resources;

import debatable.core.Channel;
import debatable.core.InMemoryChannelRetriever;
import debatable.core.Topic;
import debatable.core.Viewpoint;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/chat/channel")
@Slf4j
public class ChatResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
            @QueryParam("topicId") @NotEmpty String topicId,
            @QueryParam("viewpoint") @NotEmpty String viewpointStance) {
        Topic topic = new Topic(topicId);
        Viewpoint viewpoint = new Viewpoint(viewpointStance);

        Channel channel = InMemoryChannelRetriever.getChannel(topic, viewpoint);

        return Response.ok(channel).build();
    }
}
