package debatable.core;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import debatable.core.model.Channel;
import debatable.core.model.Topic;
import debatable.core.model.Viewpoint;

import java.time.Instant;
import java.util.Random;

public class DynamoDbChannelDeterminer {
    public synchronized Channel determineChannel(
            Topic topic,
            Viewpoint viewpoint,
            Table channelsTable) {
        QuerySpec query = new QuerySpec()
                .withConsistentRead(true)
                .withMaxResultSize(1)
                .withValueMap(
                        new ValueMap()
                                .withString(":v_id", topic.getId())
                                .withString(":v_vp", getOpposingViewpoint(viewpoint).getStance()))
                .withKeyConditionExpression("TopicId = :v_id and begins_with(ViewpointCreatedTimestamp, :v_vp)");
        ItemCollection<QueryOutcome> queryResult = channelsTable.query(query);

        Page<Item, QueryOutcome> onlyPage = queryResult.firstPage();
        String channelId;
        if (onlyPage.size() == 1) {
            Item item = onlyPage.getLowLevelResult().getItems().get(0);
            channelId = item.getString("ChannelId");
            DeleteItemSpec del = new DeleteItemSpec()
                    .withReturnValues(ReturnValue.ALL_OLD)
                    .withPrimaryKey(
                            "TopicId",
                            item.getString("TopicId"),
                            "ViewpointCreatedTimestamp",
                            item.getString("ViewpointCreatedTimestamp"));
            channelsTable.deleteItem(del);
        } else {
            channelId = generateRandomChannelId();
            Instant now = Instant.now();
            Item item = new Item()
                    .withString("TopicId", topic.getId())
                    .withString("ViewpointCreatedTimestamp",
                            viewpoint.getStance() + "#" + now.toEpochMilli())
                    .withString("ChannelId", channelId);
            PutItemSpec putItemSpec = new PutItemSpec()
                    .withItem(item)
                    .withReturnValues(ReturnValue.ALL_OLD);
            channelsTable.putItem(putItemSpec);
        }

        return new Channel(channelId);
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
