package debatable.resources;

import debatable.api.TokenResponse;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static debatable.util.RestAssuredUtil.setBaseUri;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;

public class TokenResourceTest {
    @Before
    public void setup() {
        setBaseUri();
    }

    @Test
    public void getToken() {
        String jwtTokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        TokenResponse tokenResponse =
                RestAssured.get("/chat/token?identity=foobar")
        .then()
                .extract()
                .as(TokenResponse.class);

        assertThat(tokenResponse.token, matchesPattern(jwtTokenRegex));
    }
}
