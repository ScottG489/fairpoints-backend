package debatable;

import debatable.core.Channel;
import debatable.core.InMemoryChannelDeterminer;
import debatable.core.Topic;
import debatable.core.Viewpoint;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

// TODO: This class feels a bit too high level. I'm not sure if we should be unit testing the chat
// TODO:   resource rather than the channel retriever.
public class InMemoryChannelDeterminerTest {
    private InMemoryChannelDeterminer inMemoryChannelDeterminer;
    private Map<String, Map<String, LinkedList<String>>> channelsStore = new HashMap<>();

    @Before
    public void before() {
        channelsStore = new HashMap<>();
        inMemoryChannelDeterminer = new InMemoryChannelDeterminer();
    }

    @Test
    public void shouldReturnDifferentChannelsGivenDifferentTopics() {
        Channel channel = inMemoryChannelDeterminer.determineChannel(new Topic("topic1"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = inMemoryChannelDeterminer.determineChannel(new Topic("topic2"), new Viewpoint("agree"), channelsStore);

        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnDifferentChannelsGivenSameTopicsAndSameStances() {
        Channel channel = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnSameChannelGivenSameTopicAndDifferenceStances() {
        Channel channel = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsStore);

        assertThat(channel, is(channel2));
    }

    @Test
    public void shouldMatchOpposingViewpointsInOrderTheyWereReceived() {
        Channel channel = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel2 = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsStore);
        Channel channel3 = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsStore);
        Channel channel4 = inMemoryChannelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disdisagree"), channelsStore);


        assertThat(channel, is(channel3));
        assertThat(channel2, is(channel4));
    }
}