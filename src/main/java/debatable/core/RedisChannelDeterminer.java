package debatable.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class RedisChannelDeterminer {
    public Channel getChannel(
            Topic topic,
            Viewpoint viewpoint,
            Map<String, Map<String, LinkedList<String>>> channelsStore) {
        String channelId;
        Map<String, LinkedList<String>> viewpointChannelsMap =
                channelsStore.getOrDefault(topic.getId(), new HashMap<>());

        if (channelsStore.containsKey(topic.getId())) {
            LinkedList<String> opposingViewpointChannels =
                    getOpposingViewpointChannels(viewpoint, viewpointChannelsMap);
            if (opposingViewpointChannels.isEmpty()) {
                channelId = createChannelEntry(viewpoint, viewpointChannelsMap);
            } else {
                channelId = opposingViewpointChannels.remove();
            }
        } else {
            channelId = createChannelEntry(viewpoint, viewpointChannelsMap);
            channelsStore.put(topic.getId(), viewpointChannelsMap);
        }

        channelsStore.put(topic.getId(), viewpointChannelsMap);

        return new Channel(channelId);
    }

    private LinkedList<String> getOpposingViewpointChannels(
            Viewpoint viewpoint,
            Map<String, LinkedList<String>> viewpointChannelsMap) {
        return viewpointChannelsMap.getOrDefault(
                getOpposingViewpoint(viewpoint).getStance(),
                new LinkedList<>());
    }

    private String createChannelEntry(Viewpoint viewpoint, Map<String, LinkedList<String>> viewpointChannelsMap) {
        String channelId;

        channelId = generateRandomChannelId();
        LinkedList<String> currentViewpointChannels =
                viewpointChannelsMap.getOrDefault(viewpoint.getStance(), new LinkedList<>());
        currentViewpointChannels.add(channelId);
        viewpointChannelsMap.put(viewpoint.getStance(), currentViewpointChannels);

        return channelId;
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

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
