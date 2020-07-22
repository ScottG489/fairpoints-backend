package debatable.util;

import io.restassured.RestAssured;

import static debatable.util.ConfigUtil.getFromConfig;

public class RestAssuredUtil {
    public static void setBaseUri() {
        RestAssured.baseURI = getFromConfig("baseUri");
    }

    public static void setAdminBaseUri() {
        RestAssured.baseURI = getFromConfig("adminBaseUri");
    }
}
