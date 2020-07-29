package debatable.resources;

import debatable.core.Channel;
import debatable.core.InMemoryChannelDeterminer;
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
import java.util.LinkedList;
import java.util.Map;

@Path("/chat/channel")
@Slf4j
public class ChatResource {
    private final InMemoryChannelDeterminer inMemoryChannelDeterminer;
    Map<String, Map<String, LinkedList<String>>> channelsStore;

    public ChatResource(
            InMemoryChannelDeterminer inMemoryChannelDeterminer,
            Map<String, Map<String, LinkedList<String>>> channelsStore) {
        this.inMemoryChannelDeterminer = inMemoryChannelDeterminer;
        this.channelsStore = channelsStore;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
            @QueryParam("topicId") @NotEmpty String topicId,
            @QueryParam("viewpoint") @NotEmpty String viewpointStance) {
        Topic topic = new Topic(topicId);
        Viewpoint viewpoint = new Viewpoint(viewpointStance);

        Channel channel = inMemoryChannelDeterminer.determineChannel(topic, viewpoint, channelsStore);

        return Response.ok(channel).build();
    }
}
