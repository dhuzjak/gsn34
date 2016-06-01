package hr.fer.rasip.wrappers.parser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nikola on 21.02.2015.
 */
public class RegularDataParser implements IWaspMoteGatewayDataParser {

    protected HashMap<String, String> mapping;

    @Override
    public Serializable[] parseData(String data) {
        int i = 0;
        int[] values = new int[mapping.keySet().size()];
        //System.out.println(data);
        for (String s : mapping.keySet()) {
            /*if (data.split("!" + s + "!").length == 3) {
                values[i++] = Integer.getInteger(data.split("!" + s + "!")[1], 0);
            }*/
            Pattern pattern = Pattern.compile("!"+s+"!"+"(.+?)"+"!"+s+"!");
            Matcher matcher = pattern.matcher(data);

            matcher.find();
            //System.out.println(s + " : " + matcher.group(1));
            try {
                values[i] = Integer.parseInt(matcher.group(1), 10);
            } catch (Exception e) {
                values[i] = 0;
            }
            i++;
        }

        i = 0;

        Serializable[] serial = new Serializable[values.length];
        for (int val : values) {
            serial[i] = val;
            //System.out.println(serial[i]);
            i++;
        }

        return serial;
    }
}
