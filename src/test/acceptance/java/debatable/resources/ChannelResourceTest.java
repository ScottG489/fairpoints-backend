package debatable.resources;

import debatable.api.ChannelResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static debatable.util.RestAssuredUtil.setBaseUri;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

// TODO: These tests are creating state within the application that they should be cleaning up.
// TODO:   They now naturally clean up state but I've had to code them in a way so that they do so.
// TODO:   How should we clean up the backing store? Perhaps an admin endpoint? Google for this problem.
// TODO:   Just treating it like a "REST" resource where we can DELETE it might make sense
// TODO:
// TODO:
public class ChannelResourceTest {
    @Before
    public void setup() {
        setBaseUri();
    }

    @Test
    public void shouldGetSameChannelForOpposingViewpoints() {
        String topicA = uniqueTopic();
        String agreeViewpoint = "agree";
        String disagreeViewpoint = "disagree";

        ChannelResponse channelResponse = getChannel(topicA, agreeViewpoint, "acceptance-test-1");
        ChannelResponse channelResponse2 = getChannel(topicA, disagreeViewpoint, "acceptance-test-2");

        assertThat(channelResponse.id, is(channelResponse2.id));
    }

    @Test
    public void shouldGetDifferentChannelAfterOthersHaveBeenMatched() {
        String topicA = uniqueTopic();
        String agreeViewpoint = "agree";
        String disagreeViewpoint = "disagree";

        ChannelResponse channelResponse = getChannel(topicA, agreeViewpoint, "acceptance-test-3");
        ChannelResponse channelResponse2 = getChannel(topicA, disagreeViewpoint, "acceptance-test-4");
        ChannelResponse channelResponse3 = getChannel(topicA, agreeViewpoint, "acceptance-test-5");
        ChannelResponse channelResponse4 = getChannel(topicA, disagreeViewpoint, "acceptance-test-6");

        assertThat(channelResponse3.id, is(not(channelResponse.id)));
        assertThat(channelResponse3.id, is(not(channelResponse2.id)));
        assertThat(channelResponse3.id, is(channelResponse4.id));
    }

    private ChannelResponse getChannel(String topicId, String viewpoint, String identity) {
        return given()
                .queryParam("topicId", topicId)
                .queryParam("viewpoint", viewpoint)
                .queryParam("identity", identity)
        .when()
                .get("/chat/channel")
        .then()
                .extract()
                .as(ChannelResponse.class);
    }

    private String uniqueTopic() {
        return "topic-" + UUID.randomUUID();
    }
}
