package debatable;

import debatable.core.Channel;
import debatable.core.InMemoryChannelRetriever;
import debatable.core.Topic;
import debatable.core.Viewpoint;
import debatable.resources.ChatResource;
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
public class ChatResourceTest {
    private ChatResource chatResource;

    @Before
    public void before() {
        chatResource = new ChatResource(getChannelRetriever());
    }

    @Test
    public void shouldReturnDifferentChannelsGivenDifferentTopics() {
        Channel channel = (Channel) chatResource.getResponse("topic1", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic2", "agree").getEntity();

        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnDifferentChannelsGivenSameTopicsAndSameStances() {
        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "agree").getEntity();

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnSameChannelGivenSameTopicAndDifferenceStances() {
        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();

        assertThat(channel, is(channel2));
    }

    @Test
    public void shouldMatchOpposingViewpointsInOrderTheyWereReceived() {
        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel3 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();
        Channel channel4 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();

        assertThat(channel, is(channel3));
        assertThat(channel2, is(channel4));
    }

    // TODO: This test relies too much on knowing the internals of the class under test rather than
    // TODO:   being blackbox. However, I think if the class were cleaned and the logic simplified it
    // TODO:   would not be necessary. So I'll leave this test here until that happens
    @Test
    public void shouldAddViewpointToExistingViewpointQueue() {
        Channel channelAgree = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channelDisagree = (Channel) chatResource.getResponse("topic", "disagree").getEntity();
        Channel channelDisagree2 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();
        Channel channelDisagree3 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();

        assertThat(channelAgree, is(channelDisagree));
        // Assert the last 2 disagree channels are unique
        assertThat(channelDisagree2, is(not(channelAgree)));
        assertThat(channelDisagree2, is(not(channelDisagree)));
        assertThat(channelDisagree3, is(not(channelAgree)));
        assertThat(channelDisagree3, is(not(channelDisagree)));
        assertThat(channelDisagree3, is(not(channelDisagree2)));
    }

    private InMemoryChannelRetriever getChannelRetriever() {
        Map<String, Map<String, LinkedList<String>>> topicViewpointChannelStore = new HashMap<>();
        return new InMemoryChannelRetriever(topicViewpointChannelStore);
    }
}