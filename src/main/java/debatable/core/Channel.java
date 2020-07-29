package debatable.core;

import lombok.Value;

import java.io.Serializable;

@Value
public class Channel implements Serializable {
    String id;
}
