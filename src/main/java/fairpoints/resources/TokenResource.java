package fairpoints.resources;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import fairpoints.api.TokenResponse;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/chat/token")
@Slf4j
public class TokenResource {
    private final String twilioAccountSid;
    private final String twilioApiKey;
    private final String twilioApiSecret;
    private final String twilioChatServiceSid;

    public TokenResource(String twilioAccountSid,
                         String twilioApiKey,
                         String twilioApiSecret,
                         String twilioChatServiceSid) {
        this.twilioAccountSid = twilioAccountSid;
        this.twilioApiKey = twilioApiKey;
        this.twilioApiSecret = twilioApiSecret;
        this.twilioChatServiceSid = twilioChatServiceSid;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(@QueryParam("identity") String identity) {
        String generatedToken = generateToken(identity);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.token = generatedToken;

        return Response.ok(tokenResponse).build();
    }

    String generateToken(String identity) {
        ChatGrant grant = new ChatGrant();
        grant.setServiceSid(twilioChatServiceSid);

        AccessToken token = new AccessToken.Builder(
                twilioAccountSid,
                twilioApiKey,
                twilioApiSecret
        ).identity(identity).grant(grant).build();

        return token.toJwt();
    }
}
