package hr.fer.rasip.wrappers;

import gsn.beans.DataField;
import hr.fer.rasip.wrappers.parser.IWaspMoteGatewayDataParser;
import hr.fer.rasip.wrappers.parser.TermometarParser;

/**
 * Created by Nikola on 19.02.2015.
 */
public class Termometar_1Wrapper extends WaspMoteGatewayDataWrapper {
    @Override
    public DataField[] getOutputFormat() {
        DataField[] dataFields = new DataField[]{
                new DataField("temperature", "int", ""), new DataField("battery", "int", "")
        };
        return dataFields;
    }

    @Override
    public IWaspMoteGatewayDataParser getParser() {
        return new TermometarParser();
    }

    @Override
    public String getSensorId() {
        return "termometar-1";
    }


}
