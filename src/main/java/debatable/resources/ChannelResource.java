package debatable.resources;

import debatable.api.ChannelResponse;
import debatable.core.ChannelDeterminer;
import debatable.core.model.Channel;
import debatable.core.model.Topic;
import debatable.core.model.Viewpoint;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Path("/chat/channel")
@Slf4j
public class ChannelResource {
    private final ChannelDeterminer channelDeterminer;
    Map<String, Map<String, LinkedList<String>>> channelsStore;

    public ChannelResource(
            ChannelDeterminer channelDeterminer,
            Map<String, Map<String, LinkedList<String>>> channelsStore) {
        this.channelDeterminer = channelDeterminer;
        this.channelsStore = channelsStore;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
            @QueryParam("topicId") @NotEmpty String topicId,
            @QueryParam("viewpoint") @NotEmpty String viewpointStance) {
        Topic topic = new Topic(topicId);
        Viewpoint viewpoint = new Viewpoint(viewpointStance);

        Channel channel = channelDeterminer.determineChannel(topic, viewpoint, channelsStore);

        ChannelResponse response = new ChannelResponse();
        response.id = channel.getId();
        return Response.ok(response).build();
    }
}
