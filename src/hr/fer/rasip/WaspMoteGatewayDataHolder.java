package hr.fer.rasip;

import java.util.HashMap;

/**
 * Created by Nikola on 19.02.2015.
 * This class is a singleton which will be filled with data
 * from WaspmoteGatewayWrapper and mUDPWrapper
 */
public class WaspMoteGatewayDataHolder {

    private HashMap<String, String> gatewayData;

    private WaspMoteGatewayDataHolder () {
        this.gatewayData = new HashMap<String, String>();
    }

    private static WaspMoteGatewayDataHolder dataHolder;

    public static WaspMoteGatewayDataHolder getWaspMoteGatewayDataHolderInstance() {
        if (dataHolder == null) {
            WaspMoteGatewayDataHolder.dataHolder = new WaspMoteGatewayDataHolder ();
        }

        return dataHolder;
    }

    public void addGatewayData (String sensorId, String stringValue) {
        gatewayData.put(sensorId, stringValue);
    }

    public String getStringValue (String sensorId) {
        return gatewayData.get(sensorId);
    }
}
