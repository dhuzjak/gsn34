package gsn.vsensor;

import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.utils.ParamParser;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import org.jfree.chart.title.TextTitle;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.general.DefaultValueDataset;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

/*
 * TemperatureVS gets the following parameters:
 * 'max-icon-number' - integer
 * 'height' - integer, icon height in pixels
 * 'width' - integer, icon width in pixels
 * 'type' - string
 * 'timeout' - string, data timeout in minutes, 0 for no timeout
 * 'lower-limit' - integer
 * 'upper-limit' - integer, must be larger than 'lower-limit'
 */
 
/*
 * TemperatureVS gets the following StreamElement:
 * 'ID' - integer
 * 'TEMPERATURE' - double
 * and the appropriate timestamp
 */

public class TemperaturesVirtualSensor extends AbstractVirtualSensor {
   
   private final transient Logger logger = Logger.getLogger( this.getClass() );

   //This holds thermometer images for each of the sensors
   private final HashMap <Integer, JFreeChart> images = new HashMap <Integer, JFreeChart>();
   
   //This holds times of arrival for thermometer data
   private HashMap <Integer, Long> timestamps = new HashMap <Integer, Long>();
   
   //Variables for output   
   private int maxNumberOfIcons; //Maximum number of ThermometerPlot icons
   
   private int height; //Icon height
   
   private int width; //Icon width
   
   private int timeout; //Timeout
   
   private String type; //Type
   
   private int lowerLimit; //Limit for thermometer green -> yellow transition (thermometer subrange limits)
   
   private int upperLimit; //Limit for thermometer yellow -> red transition (thermometer subrange limits)
   
   private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( 64 * 1024 );
   
   public boolean initialize(){
      TreeMap <String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();
      
      //Setting maximum number of sensors
      this.maxNumberOfIcons = ParamParser.getInteger( params.get("max-icon-number"), 5 );
      
      //A thermometer must be at least 130px wide and 260px tall -> optimal width : height = 1 : 2
      //Setting output image height
      this.height = ParamParser.getInteger( params.get("height"), 260 ); //Default and minimum value for icon height is 260px
      
      //Setting output image height
      this.width = ParamParser.getInteger( params.get("width"), 130 ); //Default and minimum value for icon width is 130px
      
      //Setting timeout
      this.timeout = ParamParser.getInteger( params.get("timeout"), 0 );
      
      //Setting type
      this.type = params.get("type");
      
      //Setting upper and lower limit
      this.lowerLimit = ParamParser.getInteger( params.get("lower-limit"), 25 );
      this.upperLimit = ParamParser.getInteger( params.get("upper-limit"), 40 );
      
      if(this.lowerLimit >= this.upperLimit){
      	logger.warn("lower-limit parameter must be smaller than upper-limit parameter");
      	return false;
      }
      
      if(this.maxNumberOfIcons <= 0){
      	logger.warn("max-icon-number parameter must be greater than zero");
      	return false;
      }
      
      if(this.height < 200){
      	logger.warn("height parameter must be at least 200");
      	return false;
      }
      
      if(this.width < 130){
      	logger.warn("width parameter must be at least 130");
      	return false;
      }
      
      if(this.timeout < 0){
      	logger.warn("timeout must be non-negative");
      	return false;
      }
      
      return true;
   }
   
   public void dataAvailable ( String inputStreamName , StreamElement streamElement ) {
      if( logger.isDebugEnabled() ) logger.debug( new StringBuilder( "Data received under the name *" ).append( inputStreamName ).append( "* to the TemperaturesVS." ).toString( ) );
      
      //Remove data older than specified timeout
      if(timeout > 0 && this.images.size() > 0){
      	long current = System.currentTimeMillis();
      	
      	//Get keys
      	Integer[] keys = this.timestamps.keySet().toArray(new Integer[0]);
      	
      	for(Integer i : keys){
      		if(current - this.timestamps.get(i) > this.timeout * 60000){
      			this.images.remove(i);
      			this.timestamps.remove(i);
      		}
      	}
      }
      
      //Get data from streamElement - deserialize
      int receivedSensorID = Integer.parseInt( streamElement.getData("ID").toString() );
      double receivedTemperature = Double.parseDouble( streamElement.getData("TEMPERATURE").toString() );
      long receivedTimestamp = streamElement.getTimeStamp();
      
      SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
      
      //Make picture for this sensor if allowed by maxNumberOfIcons
      if(this.images.containsKey(receivedSensorID) || this.images.size() < this.maxNumberOfIcons){
      
      	//Make ThermometerPlot
      	ThermometerPlot tp = new ThermometerPlot( new DefaultValueDataset(receivedTemperature) );
      	tp.setUnits(ThermometerPlot.UNITS_CELCIUS);
      	tp.setAxisLocation(ThermometerPlot.LEFT);
      	
      	//Scale: from x-25 to x+25
      	int l = (int)receivedTemperature / 50;
      	if(receivedTemperature < 0.0) l -= 1;
      	int u = l + 1;
      	tp.setRange(l * 50, u * 50);
      	
      	//Setting colors
      	tp.setMercuryPaint(Color.blue.darker());
      	tp.setSubrangePaint(ThermometerPlot.NORMAL, Color.green.darker());
        tp.setSubrangePaint(ThermometerPlot.WARNING, Color.orange);
        tp.setSubrangePaint(ThermometerPlot.CRITICAL, Color.red.darker());
        
        //Setting shape - only JFreeChart 1.0.7 and newer
        tp.setGap(2);
        tp.setBulbRadius(30);
        tp.setColumnRadius(15);
        
        //Setting warning and critical limits
        tp.setSubrange(ThermometerPlot.NORMAL, 0, this.lowerLimit);
        tp.setSubrange(ThermometerPlot.WARNING, this.lowerLimit, this.upperLimit);
        tp.setSubrange(ThermometerPlot.CRITICAL, this.upperLimit, this.upperLimit + 200);
        
      	//Produce output image using available ThermometerPlots
	JFreeChart chart = new JFreeChart( "Sensor ID: " + receivedSensorID, JFreeChart.DEFAULT_TITLE_FONT, tp, false );
	chart.addSubtitle( new TextTitle( timeFormat.format( new Date(receivedTimestamp) ).toString(), new Font("SansSerif", Font.PLAIN, 11) ) );
	chart.setBackgroundPaint(new Color(0,0,0,0));
	
      	//Add it to the map
      	this.images.put(receivedSensorID, chart);
      	
      	//Also remember the timestamp
      	this.timestamps.put(receivedSensorID, receivedTimestamp);
      } else {
      	logger.warn( "ThermometerVS drops the input because it exceeds specified maximum number of images to be plotted." );
      	return;
      }

      //Get keys and sort them to preserve order of images
      Integer[] keys = this.images.keySet().toArray(new Integer[0]);
      Arrays.sort(keys);
      
      //Conecting all plots (thermometers) to make a single image
      int rows;
      int cols;
      int gap = 10; //Gap between icons
      
      if(keys.length >= 3) cols = 3;
      else cols = keys.length;
      
      rows = keys.length / 3;
      if(keys.length % 3 != 0) rows++;
      
      //Make resulting image
      if(this.type.equalsIgnoreCase("SVG")){
      	this.byteArrayOutputStream.reset();
      	
	DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
	Document sensorDoc = domImpl.createDocument(null, "svg", null);
	SVGGraphics2D svg2d = new SVGGraphics2D(sensorDoc);

	//Iterate through sorted IDs of sensors
	for(int i = 0; i < keys.length; i++){

		//Get chart and create an image out of it
		JFreeChart ch = this.images.get(keys[i]);

		if(ch == null) continue;

		ch.draw(svg2d, new Rectangle((i % 3) * (this.width + gap), (i / 3) * (this.height + gap), this.width, this.height));
	}
	
	svg2d.setSVGCanvasSize(new Dimension( cols * this.width + (cols - 1) * gap, rows * this.height + (rows - 1) * gap));
	
	try{
		Writer out = new OutputStreamWriter(this.byteArrayOutputStream, "UTF-8");
		svg2d.stream(out, false);
		out.flush();
		out.close();
	} catch( IOException e ) {
		logger.warn( e.getMessage(), e );
		return;
	}
	      
      } else {
      
      	BufferedImage img = new BufferedImage( cols * this.width + (cols - 1) * gap, rows * this.height + (rows - 1) * gap, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = img.createGraphics();

	//Iterate through sorted IDs of sensors
	for(int i = 0; i < keys.length; i++){

		//Get chart and create an image out of it
		JFreeChart ch = this.images.get(keys[i]);

		if(ch == null) continue;

		BufferedImage tImg = ch.createBufferedImage(this.width, this.height);
		g2d.drawImage(tImg, (i % 3) * (this.width + gap), (i / 3) * (this.height + gap), Color.white, null);
	}

	g2d.dispose();

	//Write chart to PNG byte array (this.byteArrayOutputStream)
	this.byteArrayOutputStream.reset();
	try{
		ImageIO.write(img, "png", this.byteArrayOutputStream);
		this.byteArrayOutputStream.flush();
		//ImageIO.write(img, "png", new File("/home/aurora/Desktop/slika.png"));
	} catch( IOException e ) {
		logger.warn( e.getMessage(), e );
		return;
	}
      }      
      
      //Create StreamElement
      StreamElement output = new StreamElement( new String[] {"Temperatures"}, new Byte[] {DataTypes.BINARY}, new Serializable[] {this.byteArrayOutputStream.toByteArray()}, System.currentTimeMillis() );
      
      //Push generated image
      dataProduced( output );
   }
   
   public void dispose(){
   }
   
}
