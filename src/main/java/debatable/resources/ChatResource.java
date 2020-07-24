package debatable.resources;

import debatable.core.Channel;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

@Path("/chat")
@Slf4j
public class ChatResource {
    public static final Map<Topic, HashMap<Viewpoint, LinkedList<Channel>>> topics = new HashMap<>();

    // TODO: Make use of getOrDefault to make code cleaner?
    // TODO: This has concurrency issues since we're not taking atomic actions after retrieving values.
    // TODO:   e.g. we check and an opposing viewpoint doesn't exist and before we add it another thread
    // TODO:   does the same check and also sees it doesn't exist. Thus we end up with a topic that has
    // TODO:   opposing viewpoint channels waiting when they should have been matched up. How to test
    // TODO:   concurrency?
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
            @QueryParam("topicId") @NotEmpty String topicId,
            @QueryParam("viewpoint") @NotEmpty String viewpointStance) {
        Topic topic = new Topic(topicId);
        Viewpoint viewpoint = new Viewpoint(viewpointStance);

        Channel channel;
        if (topics.containsKey(topic)) {
            HashMap<Viewpoint, LinkedList<Channel>> viewpointChannelsHashMap = topics.get(topic);
            Viewpoint opposingViewpoint = getOpposingViewpoint(viewpoint);
            if (viewpointChannelsHashMap.containsKey(opposingViewpoint)) {
                LinkedList<Channel> opposingViewpointChannels = viewpointChannelsHashMap.get(opposingViewpoint);
                if (opposingViewpointChannels.isEmpty()) {
                    channel = generateRandomChannel();
                    if (viewpointChannelsHashMap.containsKey(viewpoint)) {
                        LinkedList<Channel> currentViewpointChannels = viewpointChannelsHashMap.get(viewpoint);
                        currentViewpointChannels.add(channel);
                    } else {
                        LinkedList<Channel> channels = new LinkedList<>();
                        channels.add(channel);
                        viewpointChannelsHashMap.put(viewpoint, channels);
                    }
                } else {
                    channel = opposingViewpointChannels.poll();
                }
            } else {
                channel = generateRandomChannel();
                viewpointChannelsHashMap.get(viewpoint).add(channel);
            }
        } else {
            HashMap<Viewpoint, LinkedList<Channel>> viewpointChannelHashMap = new HashMap<>();
            channel = generateRandomChannel();
            LinkedList<Channel> channels = new LinkedList<>();
            channels.add(channel);
            viewpointChannelHashMap.put(viewpoint, channels);
            topics.put(topic, viewpointChannelHashMap);
        }

        return Response.ok(channel).build();
    }

    private Viewpoint getOpposingViewpoint(Viewpoint viewpoint) {
        return viewpoint.getStance().equals("agree")
                ? new Viewpoint("disagree")
                : new Viewpoint("agree");
    }

    private Channel generateRandomChannel() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String randomAlpha = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return new Channel(randomAlpha);
    }
}
