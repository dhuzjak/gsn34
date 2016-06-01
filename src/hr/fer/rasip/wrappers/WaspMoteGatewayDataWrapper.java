package hr.fer.rasip.wrappers;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.http.OutputStructureHandler;
import gsn.wrappers.AbstractWrapper;
import hr.fer.rasip.WaspMoteGatewayDataHolder;
import hr.fer.rasip.wrappers.parser.IWaspMoteGatewayDataParser;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * Created by Nikola on 19.02.2015.
 * This class reads data from WaspMoteGatewayDataHolder based
 * on sensorId
 */
public abstract class WaspMoteGatewayDataWrapper extends AbstractWrapper {

    private int                     threadCounter = 0;
    private AddressBean addressBean;
    private final transient Logger logger = Logger.getLogger(WaspMoteGatewayDataWrapper.class);
    private int samplingRate;
    private final int DEFAULT_SAMPLING_RATE = 60000;

    public abstract DataField[] getOutputFormat();

    @Override
    public boolean initialize() {
        setName("WaspMoteGatewayDataWrapper" + (++threadCounter ));
        addressBean = getActiveAddressBean();

        samplingRate = addressBean.getPredicateValueAsInt("sampling-rate", DEFAULT_SAMPLING_RATE);

        return true;
    }

    @Override
    public void dispose() {
        threadCounter--;
    }

    @Override
    public String getWrapperName() {
        return "WaspMoteGatewayDataWrapper";
    }

    public void run() {
        while(isActive()){

            try {
                Thread.sleep(samplingRate);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            String data = WaspMoteGatewayDataHolder.getWaspMoteGatewayDataHolderInstance().getStringValue(getSensorId());
            if (data != null) {
                try {
                    IWaspMoteGatewayDataParser parser = getParser();
                    postStreamElement(parser.parseData(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }

    }

    public abstract IWaspMoteGatewayDataParser getParser ();

    public abstract String getSensorId();
}
