package debatable.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class InMemoryChannelRetriever {
    public final Map<String, Map<String, LinkedList<String>>> topics;

    public InMemoryChannelRetriever(Map<String, Map<String, LinkedList<String>>> topics) {
        this.topics = topics;
    }

    public synchronized Channel getChannel(Topic topic, Viewpoint viewpoint) {
        String channelId;
        Map<String, LinkedList<String>> viewpointChannelsMap = topics.getOrDefault(topic.getId(), new HashMap<>());

        if (topics.containsKey(topic.getId())) {
            Viewpoint opposingViewpoint = getOpposingViewpoint(viewpoint);
            LinkedList<String> opposingViewpointChannels =
                    viewpointChannelsMap.getOrDefault(opposingViewpoint.getStance(), new LinkedList<>());
            if (opposingViewpointChannels.isEmpty()) {
                channelId = generateRandomChannelId();
                LinkedList<String> currentViewpointChannels =
                        viewpointChannelsMap.getOrDefault(viewpoint.getStance(), new LinkedList<>());
                currentViewpointChannels.add(channelId);
                viewpointChannelsMap.put(viewpoint.getStance(), currentViewpointChannels);
            } else {
                channelId = opposingViewpointChannels.remove();
            }
        } else {
            channelId = generateRandomChannelId();
            LinkedList<String> channels = new LinkedList<>();
            channels.add(channelId);
            viewpointChannelsMap.put(viewpoint.getStance(), channels);
            topics.put(topic.getId(), viewpointChannelsMap);
        }

        return new Channel(channelId);
    }

    private static Viewpoint getOpposingViewpoint(Viewpoint viewpoint) {
        return viewpoint.getStance().equals("agree")
                ? new Viewpoint("disagree")
                : new Viewpoint("agree");
    }

    private static String generateRandomChannelId() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String randomAlpha = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return randomAlpha;
    }
}