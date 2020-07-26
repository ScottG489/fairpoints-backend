package debatable.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

// TODO: Make use of getOrDefault to make code cleaner?
public class InMemoryChannelRetriever {
    public static final Map<Topic, HashMap<Viewpoint, LinkedList<Channel>>> topics = new HashMap<>();

    public synchronized static Channel getChannel(Topic topic, Viewpoint viewpoint) {
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
                    channel = opposingViewpointChannels.remove();
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