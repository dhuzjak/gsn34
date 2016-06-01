package hr.fer.rasip.wrappers.parser;

import gsn.beans.DataField;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Nikola on 19.02.2015.
 */
public class TermometarParser extends RegularDataParser {

    //termometar-1 - t, b - all int
    public TermometarParser () {
        mapping = new HashMap<String, String>();
        mapping.put ("t", "temperature");
        mapping.put ("b", "battery");
    }

}
