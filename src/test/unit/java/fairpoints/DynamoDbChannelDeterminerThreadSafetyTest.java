package fairpoints;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import fairpoints.core.DynamoDbChannelDeterminer;
import fairpoints.core.model.Topic;
import fairpoints.core.model.Viewpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

// TODO: If we're getting a real table then this isn't a unit test. Reduce scope
@Ignore
@RunWith(Parameterized.class)
public class DynamoDbChannelDeterminerThreadSafetyTest {
    private DynamoDbChannelDeterminer channelDeterminer;
    private Table channelsTable;
    private static final List<List<Object>> topicsAndViewpoints = new ArrayList<>();

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[1][0];
    }

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
    public void testThreadSafety() throws InterruptedException {
        int numberOfThreads = 50;
        populate(numberOfThreads);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        for (List<Object> l : topicsAndViewpoints) {
            service.execute(() -> {
                channelDeterminer.determineChannel((Topic) l.get(0), (Viewpoint) l.get(1), channelsTable);
            });
        }
        service.shutdown();
        service.awaitTermination(10000, TimeUnit.SECONDS);

        ItemCollection<ScanOutcome> scan = channelsTable.scan(new ScanSpec());

        assertThat(scan.firstPage().size(), is(0));
    }

    private void populate(int numberOfThreads) {
        ArrayList<Object> agreeListA = new ArrayList<>();
        agreeListA.add(new Topic("topicA"));
        agreeListA.add(new Viewpoint("agree"));
        ArrayList<Object> disagreeListA = new ArrayList<>();
        disagreeListA.add(new Topic("topicA"));
        disagreeListA.add(new Viewpoint("disagree"));

        ArrayList<Object> agreeListB = new ArrayList<>();
        agreeListB.add(new Topic("topicB"));
        agreeListB.add(new Viewpoint("agree"));
        ArrayList<Object> disagreeListB = new ArrayList<>();
        disagreeListB.add(new Topic("topicB"));
        disagreeListB.add(new Viewpoint("disagree"));

        for (int i = 0; numberOfThreads > i; i++) {
            topicsAndViewpoints.add(agreeListA);
            topicsAndViewpoints.add(disagreeListA);
            topicsAndViewpoints.add(agreeListB);
            topicsAndViewpoints.add(disagreeListB);
        }
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