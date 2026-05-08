package fairpoints;

import fairpoints.core.ChannelDeterminer;
import fairpoints.core.model.Channel;
import fairpoints.core.model.Topic;
import fairpoints.core.model.Viewpoint;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChannelDeterminerTest {
    private ChannelDeterminer channelDeterminer;
    private Map<String, Map<String, LinkedList<String>>> channelsStore = new HashMap<>();

    @Before
    public void before() {
        channelsStore = new HashMap<>();
        channelDeterminer = new ChannelDeterminer();
    }

    @Test
    public void shouldReturnDifferentChannelsGivenDifferentTopics() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic1"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic2"), new Viewpoint("agree"), channelsStore);

        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnDifferentChannelsGivenSameTopicsAndSameStances() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnSameChannelGivenSameTopicAndDifferenceStances() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsStore);

        assertThat(channel, is(channel2));
    }

    @Test
    public void shouldMatchOpposingViewpointsInOrderTheyWereReceived() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel3 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsStore);
        Channel channel4 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disdisagree"), channelsStore);

        assertThat(channel, is(channel3));
        assertThat(channel2, is(channel4));
    }

    @Test
    public void shouldReturnChannelWithSpecifiedPattern() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic1"), new Viewpoint("agree"), channelsStore);
        assertThat(channel.getId(), matchesPattern("^[a-zA-Z0-9]{10}$"));
    }
}