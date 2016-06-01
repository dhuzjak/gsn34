package hr.fer.rasip.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import javax.xml.transform.dom.DOMSource;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.utils.ParamParser;
import gsn.wrappers.AbstractWrapper;

public class YahooWeather2 extends AbstractWrapper {

	private static final String FIELD_NAME_TEMPERATURE = "temperature";
	private static final String FIELD_NAME_HUMIDITY = "humidity";
	private static final int DEFAULT_SAMPLING_RATE = 10000; // 10 seconds
	private static final String DEFAULT_WOEID = "851128"; // where on earth id (default: Zagreb)
	private static final int CONNECT_TIMEOUT = 5000;	// ms
	private static final int READ_TIMEOUT = 5000;	// ms
	private final transient Logger logger = Logger
			.getLogger(YahooWeather2.class);

	private transient DataField[] collection = new DataField[] {
			new DataField(FIELD_NAME_TEMPERATURE, "int",
					"Temperature from yahoo weather for choosen place "),
			new DataField(FIELD_NAME_HUMIDITY, "int",
					"Humidity from yahoo weather for choosen place ") };

	private int samplingRate = DEFAULT_SAMPLING_RATE;
	private String woeid;
	private static int threadCounter = 0;

	@Override
	public DataField[] getOutputFormat() {
		return collection;
	}

	@Override
	public boolean initialize() {
		setName("YahooWeather2-Thread" + (++threadCounter));

		AddressBean addressBean = getActiveAddressBean();

		if (addressBean.getPredicateValue("sampling-rate") != null) {
			samplingRate = ParamParser.getInteger(
					addressBean.getPredicateValue("sampling-rate"),
					DEFAULT_SAMPLING_RATE);
			if (samplingRate < 10000 || samplingRate > 36000000) {
				logger.warn("Sampling rate must be from interval 10000 < sampling-rate < 36000000. It is set to default: "
						+ DEFAULT_SAMPLING_RATE + " ms");
				samplingRate = DEFAULT_SAMPLING_RATE;
			}
		} else {
			logger.warn("Sampling rate set to default: "
					+ DEFAULT_SAMPLING_RATE + " ms");
		}

		woeid = addressBean.getPredicateValue("woeid");
		if (woeid == null) {
			woeid = DEFAULT_WOEID;
			logger.warn("WOEID (where on earth ID - yahoo weather) isn'set and wrapper is collecting data for Zagreb, Croatia. Check your WOEID on http://weather.yahoo.com");
		}

		return true;
	}

	@Override
	public void dispose() {
		threadCounter--;
	}

	@Override
	public String getWrapperName() {
		return "YahooWeatherState";
	}

	public void run() {
		while (isActive()) {
			
			HttpURLConnection conn = null;
			InputStream in = null;
			Document doc = null;
			String data = null;
			
			try {
				Thread.sleep(samplingRate);
				
				conn = (HttpURLConnection) new URL("http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=c").openConnection();
				
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				conn.setRequestMethod("GET");
				
				conn.connect();
				
				in = conn.getInputStream();
				
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
				DOMSource domSource = new DOMSource(doc);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.transform(domSource, result);
				data = writer.toString();
				
				postStreamElement(new Serializable[] { 
						Integer.parseInt(((Element) doc.getDocumentElement()
								.getElementsByTagName("yweather:condition").item(0)).getAttribute("temp")),  
						Integer.parseInt(((Element) doc.getDocumentElement()
								.getElementsByTagName("yweather:atmosphere").item(0)).getAttribute("humidity"))
				});
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (SAXException e) {
				logger.error(e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				logger.error(e.getMessage(), e);
			} catch (NullPointerException e) {
				logger.error(e.getMessage(), e);
				logger.error(data);
			} catch (TransformerException e) {
				logger.error(e.getMessage(), e);
				logger.error(data);
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				IOUtils.closeQuietly(in);
			}
		}
	}

}
