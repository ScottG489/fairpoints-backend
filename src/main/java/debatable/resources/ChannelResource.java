package debatable.resources;

import com.amazonaws.services.dynamodbv2.document.Table;
import debatable.api.ChannelResponse;
import debatable.core.ChannelDeterminer;
import debatable.core.DynamoDbChannelDeterminer;
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

@Path("/chat/channel")
@Slf4j
public class ChannelResource {
    private final DynamoDbChannelDeterminer channelDeterminer;
    private final Table channelsTable;

    public ChannelResource(
            DynamoDbChannelDeterminer channelDeterminer,
            Table channelsTable) {
        this.channelDeterminer = channelDeterminer;
        this.channelsTable = channelsTable;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
            @QueryParam("topicId") @NotEmpty String topicId,
            @QueryParam("viewpoint") @NotEmpty String viewpointStance) {
        Topic topic = new Topic(topicId);
        Viewpoint viewpoint = new Viewpoint(viewpointStance);

        Channel channel = channelDeterminer.determineChannel(topic, viewpoint, channelsTable);

        ChannelResponse response = new ChannelResponse();
        response.id = channel.getId();
        return Response.ok(response).build();
    }
}
