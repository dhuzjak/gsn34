package hr.fer.rasip.wrappers.parser;

import gsn.beans.DataField;

import java.io.Serializable;

/**
 * Created by Nikola on 19.02.2015.
 */
public interface IWaspMoteGatewayDataParser {
    public Serializable[] parseData(String data);
}
