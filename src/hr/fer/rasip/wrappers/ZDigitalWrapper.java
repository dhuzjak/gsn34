package hr.fer.rasip.wrappers;

import gsn.beans.DataField;
import hr.fer.rasip.wrappers.parser.IWaspMoteGatewayDataParser;
import hr.fer.rasip.wrappers.parser.ZDigitalParser;

/**
 * Created by Nikola on 19.02.2015.
 */
public class ZDigitalWrapper extends WaspMoteGatewayDataWrapper {
    @Override
    public DataField[] getOutputFormat() {
        DataField[] dataFields = new DataField[]{
                new DataField("ta", "int", ""), new DataField("battery", "int", ""),
                new DataField("ha", "int", ""), new DataField("ea", "int", ""),
                new DataField("ll", "int", ""), new DataField("te", "int", "")
        };
        return dataFields;
    }

    @Override
    public IWaspMoteGatewayDataParser getParser() {
        return new ZDigitalParser();
    }

    @Override
    public String getSensorId() {
        return "zDigital";
    }
}
