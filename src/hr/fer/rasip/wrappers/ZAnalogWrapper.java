package hr.fer.rasip.wrappers;

import gsn.beans.DataField;
import hr.fer.rasip.wrappers.parser.IWaspMoteGatewayDataParser;
import hr.fer.rasip.wrappers.parser.ZAnalogParser;

/**
 * Created by Nikola on 19.02.2015.
 */
public class ZAnalogWrapper extends WaspMoteGatewayDataWrapper {
    @Override
    public DataField[] getOutputFormat() {
        DataField[] dataFields = new DataField[]{
                new DataField("ta", "int", ""), new DataField("battery", "int", ""),
                new DataField("ha", "int", ""), new DataField("mv", "int", ""),
                new DataField("ll", "int", "")
        };
        return dataFields;
    }

    @Override
    public IWaspMoteGatewayDataParser getParser() {
        return new ZAnalogParser();
    }

    @Override
    public String getSensorId() {
        return "zAnalog";
    }


}
