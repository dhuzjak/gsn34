package hr.fer.rasip.wrappers.parser;

import gsn.beans.DataField;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Nikola on 19.02.2015.
 */
public class ZAnalogParser extends RegularDataParser {

    //zAnalog - ta, b, ha, mv, ll - all int
    public ZAnalogParser () {
        mapping = new HashMap<String, String>();
        mapping.put ("ta", "ta");
        mapping.put ("b", "battery");
        mapping.put ("ha", "ha");
        mapping.put ("mv", "mv");
        mapping.put ("ll", "ll");
    }

}
