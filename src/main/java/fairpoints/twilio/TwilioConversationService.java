package fairpoints.twilio;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.conversations.v1.service.Conversation;
import com.twilio.rest.conversations.v1.service.conversation.Participant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioConversationService {
    private final String serviceSid;

    public TwilioConversationService(String accountSid,
                                     String apiKey,
                                     String apiSecret,
                                     String serviceSid) {
        Twilio.init(apiKey, apiSecret, accountSid);
        this.serviceSid = serviceSid;
    }

    public void ensureConversationAndAddParticipant(String uniqueName,
                                                    String friendlyName,
                                                    String identity) {
        ensureConversation(uniqueName, friendlyName);
        addParticipant(uniqueName, identity);
    }

    private void ensureConversation(String uniqueName, String friendlyName) {
        try {
            Conversation.creator(serviceSid)
                    .setUniqueName(uniqueName)
                    .setFriendlyName(friendlyName)
                    .create();
        } catch (ApiException e) {
            if (e.getStatusCode() != null && e.getStatusCode() == 409) {
                return;
            }
            throw e;
        }
    }

    private void addParticipant(String conversationUniqueOrSid, String identity) {
        try {
            Participant.creator(serviceSid, conversationUniqueOrSid)
                    .setIdentity(identity)
                    .create();
        } catch (ApiException e) {
            if (e.getStatusCode() != null && e.getStatusCode() == 409) {
                return;
            }
            throw e;
        }
    }
}
