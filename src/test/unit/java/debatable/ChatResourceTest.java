package debatable;

import debatable.core.Channel;
import debatable.resources.ChatResource;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.*;

public class ChatResourceTest {

    @Test
    public void shouldReturnDifferentChannelsGivenDifferentTopics() {
        ChatResource chatResource = new ChatResource();

        Channel channel = (Channel) chatResource.getResponse("topic1", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic2", "agree").getEntity();

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnDifferentChannelsGivenSameTopicsAndSameStances() {
        ChatResource chatResource = new ChatResource();

        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "agree").getEntity();

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnSameChannelGivenSameTopicAndDifferenceStances() {
        ChatResource chatResource = new ChatResource();

        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();

        System.out.println(channel);
        assertThat(channel, is(channel2));
    }

    @Test
    public void shouldMatchOpposingViewpointsInOrderTheyWereReceived() {
        ChatResource chatResource = new ChatResource();

        Channel channel = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel2 = (Channel) chatResource.getResponse("topic", "agree").getEntity();
        Channel channel3 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();
        Channel channel4 = (Channel) chatResource.getResponse("topic", "disagree").getEntity();

        System.out.println(channel);
        assertThat(channel, is(channel3));
        assertThat(channel2, is(channel4));
    }
}