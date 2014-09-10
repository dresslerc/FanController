package carstendressler.com.util;

/**
 * Created by Carsten on 7/27/2014.
 */
public class HTTPHeader {
    private final String name;
    private final String value;

    public HTTPHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
