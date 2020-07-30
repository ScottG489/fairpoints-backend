package debatable.resources;

import debatable.api.ChannelResponse;
import org.junit.Before;
import org.junit.Test;

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
        String topicA = "topicA";
        String agreeViewpoint = "agree";
        String disagreeViewpoint = "disagree";

        ChannelResponse channelResponse =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", agreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        ChannelResponse channelResponse2 =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", disagreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        assertThat(channelResponse.id, is(channelResponse2.id));
    }

    @Test
    public void shouldGetDifferentChannelAfterOthersHaveBeenMatched() {
        String topicA = "topicA";
        String agreeViewpoint = "agree";
        String disagreeViewpoint = "disagree";

        ChannelResponse channelResponse =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", agreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        ChannelResponse channelResponse2 =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", disagreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        ChannelResponse channelResponse3 =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", agreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        ChannelResponse channelResponse4 =
                given()
                        .queryParam("topicId", topicA)
                        .queryParam("viewpoint", disagreeViewpoint)
                .when()
                        .get("/chat/channel")
                .then()
                        .extract()
                        .as(ChannelResponse.class);

        assertThat(channelResponse3.id, is(not(channelResponse.id)));
        assertThat(channelResponse3.id, is(not(channelResponse2.id)));
        assertThat(channelResponse3.id, is(channelResponse4.id));
    }
}
