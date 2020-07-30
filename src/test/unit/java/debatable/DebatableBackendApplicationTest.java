package debatable;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DebatableBackendApplicationTest {
    @Test
    public void getName() {
        String expectedName = "debatable-backend";
        DebatableBackendApplication app = new DebatableBackendApplication();
        assertThat(app.getName(), is(expectedName));
    }
}