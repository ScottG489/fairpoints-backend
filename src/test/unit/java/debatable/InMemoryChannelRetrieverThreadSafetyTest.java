package debatable;

import debatable.core.Channel;
import debatable.core.InMemoryChannelRetriever;
import debatable.core.Topic;
import debatable.core.Viewpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

// NOTE: Since the in memory implementation is deprecated this is mostly for demonstrative purposes.
// NOTE:   However, this may be useful in the future for actual implementations. Keep.
@RunWith(Parameterized.class)
public class InMemoryChannelRetrieverThreadSafetyTest {
    private static final List<List<Object>> topicsAndViewpoints = new ArrayList<>();
    private InMemoryChannelRetriever inMemoryChannelRetriever;

    @Parameterized.Parameters
    public static Object[][] data() {
        // Run this test suite 10 times
        return new Object[10][0];
    }

    @Before
    public void before() {
        inMemoryChannelRetriever = getChannelRetriever();
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        int numberOfThreads = 1000;
        populate(numberOfThreads);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        for (List<Object> l : topicsAndViewpoints) {
            service.execute(() -> {
                inMemoryChannelRetriever.getChannel((Topic) l.get(0), (Viewpoint) l.get(1));
            });
        }
        service.shutdown();
        service.awaitTermination(10000, TimeUnit.SECONDS);

        Map<Viewpoint, LinkedList<Channel>> topicA = inMemoryChannelRetriever.topics.get(new Topic("topicA"));
        LinkedList<Channel> agreeA = topicA.getOrDefault(new Viewpoint("agree"), new LinkedList<>());
        LinkedList<Channel> disagreeA = topicA.getOrDefault(new Viewpoint("disagree"), new LinkedList<>());

        Map<Viewpoint, LinkedList<Channel>> topicB = inMemoryChannelRetriever.topics.get(new Topic("topicB"));
        LinkedList<Channel> agreeB = topicB.getOrDefault(new Viewpoint("agree"), new LinkedList<>());
        LinkedList<Channel> disagreeB = topicB.getOrDefault(new Viewpoint("disagree"), new LinkedList<>());

        assertThat(agreeA.size(), is(0));
        assertThat(disagreeA.size(), is(0));
        assertThat(agreeB.size(), is(0));
        assertThat(disagreeB.size(), is(0));
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

    private InMemoryChannelRetriever getChannelRetriever() {
        Map<Topic, HashMap<Viewpoint, LinkedList<Channel>>> topicViewpointChannelStore = new HashMap<>();
        return new InMemoryChannelRetriever(topicViewpointChannelStore);
    }
}