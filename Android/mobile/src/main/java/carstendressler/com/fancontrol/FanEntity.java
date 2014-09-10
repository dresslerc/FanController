package carstendressler.com.fancontrol;

/**
 * Created by Carsten on 7/25/2014.
 */
public class FanEntity {

    public enum Status1 { UNKNOWN, OFF, LOW, MEDIUM, HIGH }

    public String Name;
    public int Address;
    public Status1 Status;
}