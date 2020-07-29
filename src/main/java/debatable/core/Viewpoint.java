package debatable.core;

import lombok.Value;

import java.io.Serializable;

@Value
public class Viewpoint implements Serializable {
    String stance;
}
