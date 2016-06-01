package hr.fer.rasip.wrappers.parser;

import gsn.beans.DataField;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Nikola on 19.02.2015.
 */
public class ZDigitalParser extends RegularDataParser {

    //zDigital - ta, b, ha, ea, ll, te - all int
    public ZDigitalParser () {
        mapping = new HashMap<String, String>();
        mapping.put ("ta", "ta");
        mapping.put ("b", "battery");
        mapping.put ("ha", "ha");
        mapping.put ("ea", "ea");
        mapping.put ("ll", "ll");
        mapping.put ("te", "te");
    }
}
