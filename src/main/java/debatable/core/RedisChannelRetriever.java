package debatable.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class RedisChannelRetriever {
    public Map<Topic, Map<Viewpoint, LinkedList<Channel>>> topics;

    public RedisChannelRetriever(Map<Topic, Map<Viewpoint, LinkedList<Channel>>> topics) {
        this.topics = topics;
    }

    public Channel getChannel(Topic topic, Viewpoint viewpoint) {
        Channel channel;

        if (topics.containsKey(topic)) {
            Map<Viewpoint, LinkedList<Channel>> viewpointChannelsMap = topics.get(topic);
            Viewpoint opposingViewpoint = getOpposingViewpoint(viewpoint);
            if (viewpointChannelsMap.containsKey(opposingViewpoint)) {
                LinkedList<Channel> opposingViewpointChannels = viewpointChannelsMap.get(opposingViewpoint);
                if (opposingViewpointChannels.isEmpty()) {
                    channel = generateRandomChannel();
                    if (viewpointChannelsMap.containsKey(viewpoint)) {
                        LinkedList<Channel> currentViewpointChannels = viewpointChannelsMap.get(viewpoint);
                        currentViewpointChannels.add(channel);
                    } else {
                        LinkedList<Channel> channels = new LinkedList<>();
                        channels.add(channel);
                        viewpointChannelsMap.put(viewpoint, channels);
                    }
                } else {
                    channel = opposingViewpointChannels.remove();
                }
            } else {
                channel = generateRandomChannel();
                viewpointChannelsMap.get(viewpoint).add(channel);
            }
            // TODO: Hmmm need to update. Only difference from in memory implementation
            topics.put(topic, viewpointChannelsMap);
        } else {
            Map<Viewpoint, LinkedList<Channel>> viewpointChannelMap = new HashMap<>();
            channel = generateRandomChannel();
            LinkedList<Channel> channels = new LinkedList<>();
            channels.add(channel);
            viewpointChannelMap.put(viewpoint, channels);
            topics.put(topic, viewpointChannelMap);
        }

        return channel;
    }

    private static Viewpoint getOpposingViewpoint(Viewpoint viewpoint) {
        return viewpoint.getStance().equals("agree")
                ? new Viewpoint("disagree")
                : new Viewpoint("agree");
    }

    private static Channel generateRandomChannel() {
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
