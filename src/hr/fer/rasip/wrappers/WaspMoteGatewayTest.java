package hr.fer.rasip.wrappers;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;
import hr.fer.rasip.WaspMoteGatewayDataHolder;

import java.io.*;

/**
 * Created by Nikola on 21.02.2015.
 */
public class WaspMoteGatewayTest extends AbstractWrapper {

    private int                     threadCounter = 0;
    private AddressBean addressBean;
    private  DataField []			dataField;
    private int 					dataStringLength; //velicina data dijela paketa (100)
    private int 					moteIDLength; //velicina moteID dijela paketa (20)
    private String[] dataStreams;

    @Override
    public DataField[] getOutputFormat() {
        return dataField;
    }

    @Override
    public boolean initialize() {
        setName("WaspMoteGatewayTest" + (++threadCounter ));
        addressBean = getActiveAddressBean();

        dataStringLength= addressBean.getPredicateValueAsInt("data-string-length",100);
        moteIDLength = addressBean.getPredicateValueAsInt("mote-id-length",20);

        dataField = new DataField[] {new DataField("moteID","varchar(" + moteIDLength + ")" , "Identifier of WaspMote node" ),
                new DataField("data", "varchar(" + dataStringLength+ ")", "Data part of package")};

        dataStreams = new String[]{"termometar-1", "zDigital", "zAnalog"};
        return true;
    }

    public void run () {
        int i = 0;
        while (true) {
            String stream = dataStreams[i] + ".txt";
            String data = "";
            try {
                sleep(20000);
                FileInputStream reader = new FileInputStream(stream);
                int c = 0;
                while ((c = reader.read()) != -1) {
                    data += (char)c;
                }
                WaspMoteGatewayDataHolder.getWaspMoteGatewayDataHolderInstance().addGatewayData(dataStreams[i], data);
                postStreamElement(new Serializable[]{dataStreams[i], data});
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
            i = i % dataStreams.length;
        }
    }

    @Override
    public void dispose() {
        threadCounter--;
    }

    @Override
    public String getWrapperName() {
        return "WaspMoteGatewayTest";
    }
}
