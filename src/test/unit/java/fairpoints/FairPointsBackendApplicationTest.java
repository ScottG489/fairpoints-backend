package fairpoints;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FairPointsBackendApplicationTest {
    @Test
    public void getName() {
        String expectedName = "fairpoints-backend";
        FairPointsBackendApplication app = new FairPointsBackendApplication();
        assertThat(app.getName(), is(expectedName));
    }
}