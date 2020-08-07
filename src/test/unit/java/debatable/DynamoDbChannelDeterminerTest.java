package debatable;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import debatable.core.DynamoDbChannelDeterminer;
import debatable.core.model.Channel;
import debatable.core.model.Topic;
import debatable.core.model.Viewpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

// TODO: If we're getting a real table then this isn't a unit test. Reduce scope
@Ignore
public class DynamoDbChannelDeterminerTest {
    private DynamoDbChannelDeterminer channelDeterminer;
    private Table channelsTable;

    @Before
    public void before() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB db = new DynamoDB(client);
        channelsTable = db.getTable("Channels");
        channelDeterminer = new DynamoDbChannelDeterminer();
        clearTable();
    }

    @After
    public void after() {
        clearTable();
    }

    @Test
    public void shouldReturnDifferentChannelsGivenDifferentTopics() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic1"), new Viewpoint("agree"), channelsTable);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic2"), new Viewpoint("agree"), channelsTable);

        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnDifferentChannelsGivenSameTopicsAndSameStances() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsTable);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsTable);

        System.out.println(channel);
        assertThat(channel, is(not(channel2)));
    }

    @Test
    public void shouldReturnSameChannelGivenSameTopicAndDifferenceStances() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsTable);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsTable);

        assertThat(channel, is(channel2));
    }

    @Test
    public void shouldMatchOpposingViewpointsInOrderTheyWereReceived() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsTable);
        Channel channel2 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("agree"), channelsTable);
        Channel channel3 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsTable);
        Channel channel4 = channelDeterminer.determineChannel(new Topic("topic"), new Viewpoint("disagree"), channelsTable);

        assertThat(channel, is(channel3));
        assertThat(channel2, is(channel4));
    }

    @Test
    public void shouldReturnChannelWithSpecifiedPattern() {
        Channel channel = channelDeterminer.determineChannel(new Topic("topic1"), new Viewpoint("agree"), channelsTable);
        assertThat(channel.getId(), matchesPattern("^[a-zA-Z0-9]{10}$"));
    }

    private void clearTable() {
        ItemCollection<ScanOutcome> scan = channelsTable.scan(new ScanSpec());
        scan.forEach(item -> {
            DeleteItemSpec del = new DeleteItemSpec()
                    .withReturnValues(ReturnValue.ALL_OLD)
                    .withPrimaryKey(
                            "TopicId",
                            item.getString("TopicId"),
                            "ViewpointCreatedTimestamp",
                            item.getString("ViewpointCreatedTimestamp"));
            channelsTable.deleteItem(del);
        });
    }
}