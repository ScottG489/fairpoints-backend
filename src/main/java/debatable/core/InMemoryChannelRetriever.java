package debatable.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

// TODO: Make use of getOrDefault to make code cleaner?
public class InMemoryChannelRetriever {
    public final Map<String, Map<String, LinkedList<String>>> topics;

    public InMemoryChannelRetriever(Map<String, Map<String, LinkedList<String>>> topics) {
        this.topics = topics;
    }

    public synchronized Channel getChannel(Topic topic, Viewpoint viewpoint) {
        String channelId;
        if (topics.containsKey(topic.getId())) {
            Map<String, LinkedList<String>> viewpointChannelsMap = topics.get(topic.getId());
            Viewpoint opposingViewpoint = getOpposingViewpoint(viewpoint);
            if (viewpointChannelsMap.containsKey(opposingViewpoint.getStance())) {
                LinkedList<String> opposingViewpointChannels =
                        viewpointChannelsMap.get(opposingViewpoint.getStance());
                if (opposingViewpointChannels.isEmpty()) {
                    channelId = generateRandomChannelId();
                    if (viewpointChannelsMap.containsKey(viewpoint.getStance())) {
                        LinkedList<String> currentViewpointChannels = viewpointChannelsMap.get(viewpoint.getStance());
                        currentViewpointChannels.add(channelId);
                    } else {
                        LinkedList<String> channels = new LinkedList<>();
                        channels.add(channelId);
                        viewpointChannelsMap.put(viewpoint.getStance(), channels);
                    }
                } else {
                    channelId = opposingViewpointChannels.remove();
                }
            } else {
                channelId = generateRandomChannelId();
                viewpointChannelsMap.get(viewpoint.getStance()).add(channelId);
            }
        } else {
            Map<String, LinkedList<String>> viewpointChannelMap = new HashMap<>();
            channelId = generateRandomChannelId();
            LinkedList<String> channels = new LinkedList<>();
            channels.add(channelId);
            viewpointChannelMap.put(viewpoint.getStance(), channels);
            topics.put(topic.getId(), viewpointChannelMap);
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