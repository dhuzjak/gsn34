package gsn.vsensor;

import gsn.beans.DataTypes;
import gsn.beans.SensorData;
import gsn.beans.StreamElement;
import gsn.utils.ParamParser;
import hr.fer.rasip.wrappers.FieldMappings;

import java.io.IOException;
import java.io.Serializable;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.DecimalFormat;
import java.awt.BasicStroke;
import java.net.URL;
import java.awt.Rectangle;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
 
 /**
  * This VS takes the following parameters (in XML description file)
  * 'field-names' - represents physical properties measured, separated by commas
  * 'units' - represents units for field names; strings, separated by commas
  * 'max-sensor-number' - integer
  * 'width' - integer (>= 300)
  * 'type' - string
  * 'timeout' - string, data timeout in minutes, 0 for no timeout
  * 'display' - string; "METER" (default)/ "BAR" / "BOOLEAN", separated by commas; how field data is displayed
  * 'separator' - ";" or "!" determines parsing algorithm for message
  * 'thermo-limit' - double, temperature when thermometer icons change the color of mercuy
  * 'icon-dimension' - integer, dimension of icons
  * 'include-geo-data' - boolean, determines if geo data is included in streamelement (output stream should declare additional field named "Geo" of type binary:image/png for geo image)
  */
  
 /**
  * This VS should get StreamElements that look like:
  * 'ID' <int> represents a unique sensor
  * 'latitude' <double> represents sensor's latitude
  * 'longitude' <double> represents sensor's longitude
  * 'DATA' <string> string that contains measured data <field name>:<value>;<field name>:<value>;....
  * Each ID (sensor) will get its own part of the plot
  */
    
public class SVGDataDisplayVirtualSensor extends AbstractVirtualSensor {
   
   private final transient Logger logger = Logger.getLogger( this.getClass() );
   
   //Constants for displays
   public static final int METER = 1;
   public static final int BAR = 2;
   public static final int BOOLEAN = 3;
   public static final int THERMO = 4;
   
   //This represents physical properties being measured, their respective units and how they are displayed
   private String[] fields;
   private String[] units;
   private int[] display;
   private String separator;
   
   //Variables for output
   private int timeout; //Timeout in minutes
   private int maxNumberofSensors; //Maximum number of resulting plots displayed
   private Double thermoLimit;

   private int iconWidth = 250;
   private int iconHeight = 250; 
   private Integer iconDimension;  
   private int width; //Resulting image width
   private final int gap = 20; //Gap between field plots
   
   //Default latitude and longitude specified in VS description file
   private String defaultLatitude;
   private String defaultLongitude;
   
   //Will hold resulting output
   private ByteArrayOutputStream byteArrayOutputStreamData = new ByteArrayOutputStream( 64 * 1024 );
   private ByteArrayOutputStream byteArrayOutputStreamGeo = new ByteArrayOutputStream( 64 * 1024 );
   
   //This holds data for each of the sensors
   private final HashMap <String, SensorData> dataFromSensors = new HashMap<String, SensorData>();
   
   //This is a string formatter for data and time
   private final SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
   
   //For making a SVG document
   DOMImplementation domImpl;
   Document sensorDoc;
   SVGGraphics2D svg2d;
   
   //Flag for geo data
   private boolean reloadGeoData;
   private boolean includeGeoData;
   
   private void makeSVGImage(){
   	int x;
   	int y = 0;
   	int totalHeight = 0;
   	int iconsPerRow = this.width / this.iconWidth;
   	
   	//Get keys and sort them to preserve order of images
   	String[] keys = this.dataFromSensors.keySet().toArray(new String[0]);
   	Arrays.sort(keys);
   
   	for(String a : keys){
   		SensorData sd = this.dataFromSensors.get(a);
   		
   		//Calculate icon arrangement
   		int totalNumberOfIcons = sd.getNumberOfElements();
   		
   		//Write sensor ID and time
   		svg2d.setPaint(Color.BLACK);
   		svg2d.setFont(new Font("SansSerif", Font.BOLD, 18));
   		
   		y += svg2d.getFontMetrics().getHeight();
   		x = 5;
   		String s = "Sensor ID: " + sd.getID();
   		
   		svg2d.drawString(s, x, y);
   		//svg2d.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1));
   		//svg2d.drawLine(x, y+2, x + svg2d.getFontMetrics().stringWidth(s), y+2);
   		
   		svg2d.setFont(new Font("SansSerif", Font.BOLD, 14));
		s = "Measured on: " + timeFormat.format( new Date(sd.getTimestamp()) ).toString();
		
		y += svg2d.getFontMetrics().getHeight() + 5;
	
		svg2d.drawString(s, x, y);
		
		//Move down to draw icons
		y += 10;
		x = 0;
		
		int iconsPasted = 0;
		
		//Draw icons
		for(int i = 0; i < this.fields.length; i++){
			Double data = sd.get(this.fields[i]);
	
			if( data == null )
				continue;
		
			//Boundaries for display on icon scale
			int lower, upper;
		
			if(this.units[i].equals("%")){
				lower = 0;
				upper = 100;
			} else {
				lower = (int)(data / 50);
			      	if(data < 0.0) lower -= 1;
			      	
			      	upper = lower + 1;
			      	lower *= 50;
				upper *= 50;
			}
		
			//Make plot
			if( this.display[i] == SVGDataDisplayVirtualSensor.BAR ){
			
				//Drawing bar
				int rectangleX = x + this.iconWidth / 10;
				int rectangleY = y + (int)(5 * this.iconHeight / 10);
				int rectangleW = 8 * this.iconWidth / 10;
				int rectangleH = (int)(1.05 * this.iconHeight / 10);
	
				int tickLength = 2 * this.iconHeight / 100;
				int tickLabelSize = 4 * this.iconHeight / 100;
				int valueSize = 10 * this.iconHeight / 100;
				
				double fillPercentage = (data - lower) / (upper - lower);
				
				//Draw title
				svg2d.setColor(Color.BLACK);
				svg2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
				s = this.fields[i];
				
				int titleX = x + this.iconWidth / 2 - svg2d.getFontMetrics().stringWidth(s) / 2;
				int titleY = y + svg2d.getFontMetrics().getHeight();
				svg2d.drawString(s, titleX, titleY);
				
				//Draw rectangles
				svg2d.setColor(Color.WHITE);
				svg2d.fillRoundRect(rectangleX, rectangleY, rectangleW, rectangleH, 4, 4);
				
				svg2d.setColor(new Color(0x617aff));
				svg2d.fillRoundRect(rectangleX, rectangleY, (int)(rectangleW * fillPercentage), rectangleH, 4, 4);
	
				svg2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1));
				svg2d.setColor(Color.BLACK);
				svg2d.drawRoundRect(rectangleX, rectangleY, rectangleW, rectangleH, 4, 4);
				
				//Draw labels and ticks
				int y1 = rectangleY + rectangleH;
				int y2 = y1 + tickLength;
				int x1 = rectangleX;
	
				svg2d.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1));
				svg2d.setColor(Color.BLACK);
				svg2d.setFont(new Font("SansSerif", Font.PLAIN, tickLabelSize));
				
				for(int j = 0; j < 5; j++){
				    //Draw tick
				    svg2d.drawLine(x1, y1, x1, y2);
				    
				    //Draw tick label
				    s = ""+(lower+j*(upper-lower)/4);
				    svg2d.drawString(s, x1 - svg2d.getFontMetrics().stringWidth(s) / 2, y2 + svg2d.getFontMetrics().getHeight());
				    
				    //Move to the next tick on the right
				    x1 += rectangleW / 4;
				}
	
				s = data + " " + this.units[i];
	
				svg2d.setFont(new Font("SansSerif", Font.PLAIN, valueSize));
	
				x1 = x + this.iconWidth / 2 - svg2d.getFontMetrics().stringWidth(s) / 2;
				y1 = rectangleY - 8 * this.iconHeight / 100;
				svg2d.drawString(s, x1, y1);
			
			} else if( this.display[i] == SVGDataDisplayVirtualSensor.BOOLEAN ){
			
				//Draw on/off button
				int buttonX = x + 25 * this.iconWidth / 100;
				int buttonY = y + 25 * this.iconHeight / 100;
				
				int buttonH = 50 * this.iconHeight / 100;
				int buttonW = 50 * this.iconWidth / 100;
				
				//Draw title
				svg2d.setColor(Color.BLACK);
				svg2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
				s = this.fields[i];
				
				int titleX = x + this.iconWidth / 2 - svg2d.getFontMetrics().stringWidth(s) / 2;
				int titleY = y + svg2d.getFontMetrics().getHeight();
				svg2d.drawString(s, titleX, titleY);
				
				//Button
				svg2d.setColor(Color.WHITE);
				svg2d.fillOval(buttonX, buttonY, buttonW, buttonH);

				svg2d.setColor(Color.BLACK);
				svg2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1));
				svg2d.drawOval(buttonX, buttonY, buttonW, buttonH);

				svg2d.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1));
				
				if(data != 0)
					svg2d.setColor(Color.GREEN.darker());
				else
					svg2d.setColor(Color.RED);
					
				svg2d.drawArc(buttonX+buttonW/4,buttonY+buttonH/4, buttonW/2,buttonH/2, 60, -300);
				svg2d.drawLine(buttonX+buttonW/2, buttonY+2*buttonH/16, buttonX+buttonW/2, buttonY+6*buttonH/16);
				
			} else if( this.display[i] == SVGDataDisplayVirtualSensor.THERMO ){
			
				//Meter
				ThermometerPlot plot = new ThermometerPlot();
			
				//Data
				plot.setDataset(new DefaultValueDataset(data));

				//Settings
				plot.setSubrangeInfo(ThermometerPlot.NORMAL, 10000.0, 11000.0);
				plot.setSubrangeInfo(ThermometerPlot.WARNING, 11000.0, 12000.0);
				plot.setSubrangeInfo(ThermometerPlot.CRITICAL, 12000.0, 13000.0);
				
				plot.setAxisLocation(ThermometerPlot.LEFT);
				plot.setUnits(ThermometerPlot.UNITS_CELCIUS);
				
				if(this.thermoLimit == null || data < this.thermoLimit)
					plot.setMercuryPaint(new Color(0x617aff));
				else 
					plot.setMercuryPaint(Color.red);
				plot.setGap(2);
				plot.setBulbRadius(30);
				plot.setColumnRadius(15);
				
				plot.setSubrangePaint(ThermometerPlot.NORMAL, Color.red);
				plot.setSubrangePaint(ThermometerPlot.WARNING, Color.red);
				plot.setSubrangePaint(ThermometerPlot.CRITICAL, Color.red);
				
				plot.setRange(lower, upper);
				plot.setBackgroundAlpha(0);
				plot.setOutlineVisible(false);
				
				
				//Draw it into document
				JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
				chart.setBackgroundPaint(new Color(0, 0, 0, 0));
				chart.addSubtitle( new TextTitle( this.fields[i], new Font("SansSerif", Font.PLAIN, 16) ) );
				chart.setBorderVisible(false);
				
				chart.draw(svg2d, new Rectangle(x, y, this.iconWidth, this.iconHeight));				
			} else {
				//Draw title
				svg2d.setColor(Color.BLACK);
				svg2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
				s = this.fields[i];
				
				int titleX = x + this.iconWidth / 2 - svg2d.getFontMetrics().stringWidth(s) / 2;
				int titleY = y + svg2d.getFontMetrics().getHeight();
				svg2d.drawString(s, titleX, titleY);
				
				//Calculate corrected dimensions leaving some space for the title on top while retaining plot aspect ratio				
				int startY = titleY + svg2d.getFontMetrics().getMaxDescent();
				int startX = x + (startY - y)/2;
			
				//Meter
				DialPlot dialplot = new DialPlot();
			
				//Data
				dialplot.setDataset(0, new DefaultValueDataset(data));

				//Drawing
				StandardDialFrame standarddialframe = new StandardDialFrame();
				standarddialframe.setStroke( new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1) );
				standarddialframe.setBackgroundPaint(Color.white);
				standarddialframe.setForegroundPaint(Color.BLACK);
				dialplot.setDialFrame(standarddialframe);
				
				DialBackground dialbackground = new DialBackground(Color.WHITE);
				dialplot.setBackground(dialbackground);

				DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
				dialvalueindicator.setFont(new Font("Dialog", 0, 14));
				dialvalueindicator.setOutlinePaint(Color.darkGray);
				dialvalueindicator.setRadius(0.4);
				dialvalueindicator.setAngle(-90);
				dialvalueindicator.setNumberFormat(new DecimalFormat("#' " + this.units[i] + "'"));
				dialplot.addLayer(dialvalueindicator);

				StandardDialScale standarddialscale = new StandardDialScale(lower, upper, -150D, -240D, 10D, 2);
				standarddialscale.setTickRadius(0.94D);
				standarddialscale.setTickLabelOffset(0.18D);
				standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
				dialplot.addScale(0, standarddialscale);	

				org.jfree.chart.plot.dial.DialPointer.Pointer pointer = new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
				pointer.setFillPaint(new Color(0xEB4B4B));
				pointer.setWidthRadius(0.03);
				pointer.setOutlinePaint(new Color(0xEB4B4B));
				dialplot.addPointer(pointer);

				DialCap dialcap = new DialCap();
				dialcap.setFillPaint(Color.WHITE);
				dialcap.setRadius(0.05D);
				dialplot.setCap(dialcap);
				
				//Draw it into document
				JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, dialplot, false);
				chart.setBackgroundPaint(new Color(0, 0, 0, 0));
				chart.setBorderVisible(false);
				
				chart.draw( svg2d, new Rectangle( startX, startY, this.iconWidth - (startY - y), this.iconHeight - (startY - y) ) );
			}
		
			iconsPasted++;
		
			if(iconsPasted % iconsPerRow == 0){
				x = 0;
				y += this.iconHeight + this.gap;
			} else {
				x += this.iconWidth + this.gap;
			}
		}
		
		totalHeight = y;
		if(iconsPasted % iconsPerRow != 0){
			totalHeight += this.iconHeight;
			y += this.iconHeight;
		}
   	}
   	
   	int totalWidth = iconsPerRow * this.iconWidth + (iconsPerRow - 1) * this.gap;
   	svg2d.setSVGCanvasSize(new Dimension(totalWidth, totalHeight));
   }
   
   private boolean checkFieldDeclared(String inStr){
   	for(int i = 0; i < this.fields.length; i++)
   		if(this.fields[i].equalsIgnoreCase(inStr.trim()))
   			return true;
   	
   	return false;
   }
   
   public static final int PARSE_COLON = 1;
   public static final int PARSE_EXCLAMATION = 2;
   
   private SensorData parse(StreamElement se, int parseType){
   	if(parseType == PARSE_COLON){
   		try{
   			SensorData sd = new SensorData(se.getData("ID").toString(), se.getTimeStamp());
   			
   			String receivedData = se.getData("DATA").toString();
		      	String receivedLatitude = se.getData("latitude").toString();
		      	String receivedLongitude = se.getData("longitude").toString();
   			
   			sd.setLatitude(receivedLatitude);
   		   	sd.setLongitude(receivedLongitude);
		      	
		      	//Parsing data received from sensor
		      	String[] pairs = receivedData.split(";");
		      	
		      	for(String s : pairs){ //Checking fields in received StreamElement
		      		String[] pair = s.split(":");
		      		
		      		if(pair.length != 2){
		      			logger.warn("Wrong format - dropping this input.");
		      			continue;
		      		}
		      		
		      		pair[0] = pair[0].trim();
		      		
		      		if(!this.checkFieldDeclared(pair[0])){
		      			logger.warn("Found undeclared field name in received StreamElement - dropping this input.");
		      			continue;
		      		}
		      		
		      		sd.put( pair[0], Double.parseDouble( pair[1] ) ); //Put it in sensor data
		      	}
   			return sd;
   		} catch (Exception e){
   			logger.warn("Error parsing StreamElement");
   			logger.warn(e.getMessage());
   			return null;
   		}
   	} else if(parseType == PARSE_EXCLAMATION){
   		try{
   			SensorData sd = new SensorData(se.getData("moteID").toString(), se.getTimeStamp());
   			
   			String receivedData = se.getData("data").toString();
   			
   			String[] bits = receivedData.split("!!");
   			
   			for(String b : bits){
   				String[] d = b.split("!");
   				
   				if(d.length>4 || d.length<3){
   					logger.warn("Wrong length of bit '" + b + "' when split by '!'");
   					continue;
   				}
   				int i = 0;
   				
   				if(d.length==4 && d[0].trim().equals("")){
   					i++;
   				}
   				
   				String field = FieldMappings.getMapping(d[i]);
   				
   				if(field==null){
   					logger.warn("Cannot find mapping for '" + d[i] + "' to field names. It will be ignored.");
   				}
   				
   				if(!this.checkFieldDeclared(field)){
		      			logger.warn("Found undeclared field name in received StreamElement - dropping this input.");
		      			continue;
		      		}
   				
   				sd.put( field, Double.parseDouble( d[i+1] ) );
   			}
   			
   			return sd;
   		} catch (Exception e){
   			logger.warn("Error parsing StreamElement");
   			logger.warn(e.getMessage());
   			return null;
   		}
   	} else return null;
   }
   
   public boolean initialize(){
      TreeMap <String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();
      
      //Setting maximum number of sensors
      this.maxNumberofSensors = ParamParser.getInteger( params.get("max-sensor-number"), 5 );
      
      //Setting timeout
      this.timeout = ParamParser.getInteger( params.get("timeout"), 0 );
      
      //Setting width
      this.width = ParamParser.getInteger( params.get("width"), this.iconWidth * 2 );
      
      if(this.maxNumberofSensors <= 0){
      	logger.warn("max-icon-number parameter must be greater than zero");
      	return false;
      }
      
      if(this.timeout < 0){
      	logger.warn("timeout must be non-negative, zero for no timeout");
      	return false;
      }
      
      if(this.width < this.iconWidth){
      	logger.warn("image width must be greater or equal " + this.iconWidth);
      	return false;
      }
      
      //Limit for color change on thermometer plot
      try{
      	this.thermoLimit = Double.parseDouble( params.get("thermo-limit") );
      } catch(Exception e) {
      	this.thermoLimit = null;
      }
      
      //Icon dimensions
      try{
      	this.iconDimension = Integer.parseInt( params.get("icon-dimension") );
      } catch(Exception e) {
      	this.iconDimension = null;
      }
      
      if(this.iconDimension != null && this.iconDimension > 50){
      	this.iconHeight = this.iconDimension;
      	this.iconWidth = this.iconDimension;
      }
      
      //Should VS include geo data - default: false
      this.includeGeoData = Boolean.parseBoolean( params.get("include-geo-data") );
      
      //Getting names of fields, units and type of display
      this.fields = params.get("field-names").split(",");
      this.units = new String[this.fields.length];
      this.display = new int[this.fields.length];
      
      this.separator = params.get("separator");
      if(this.separator==null)this.separator=";";
      this.separator=this.separator.trim();
      if(!this.separator.equalsIgnoreCase("!") && !this.separator.equalsIgnoreCase(";")){
      	logger.warn("Wrong separator set. Use '!' or ';'. VS will continue using default (';').");
      	this.separator=";";
      }
      
      String[] parameterUnits = params.get("units").split(",");
      
      if(parameterUnits.length > this.fields.length)
      	logger.warn("Too many unit names. Some will be ignored.");
      
      for(int i = 0; i < this.fields.length; i++){
      
      	this.fields[i] = this.fields[i].trim();
      	
      	if(this.fields[i].equals("")){
      		logger.warn("field name cannot be empty");
      		return false;
      	}
      	
      	if(i >= parameterUnits.length){
      		this.units[i] = "%";
      		continue;
      	}
      	
      	if(parameterUnits[i].equalsIgnoreCase(""))
      		this.units[i] = "%";
      	else
      		this.units[i] = parameterUnits[i];
      		
      	this.units[i] = this.units[i].trim();
      }
      
      String[] parameterDisplay = params.get("display").split(",");
      
      if(parameterDisplay.length > fields.length)
      	logger.warn("Too many display parameters. Some will be ignored.");
      
      for(int i = 0; i < fields.length; i++){
      	if(i >= parameterDisplay.length){
      		this.display[i] = SVGDataDisplayVirtualSensor.METER;
      		continue;
      	}
      	
      	parameterDisplay[i] = parameterDisplay[i].trim();
      
      	if(parameterDisplay[i].equalsIgnoreCase("BOOLEAN"))
      		this.display[i] = SVGDataDisplayVirtualSensor.BOOLEAN;
      	else if(parameterDisplay[i].equalsIgnoreCase("BAR"))
      		this.display[i] = SVGDataDisplayVirtualSensor.BAR;
      	else if(parameterDisplay[i].equalsIgnoreCase("THERMO"))
      		this.display[i] = SVGDataDisplayVirtualSensor.THERMO;
      	else
      		this.display[i] = SVGDataDisplayVirtualSensor.METER;
      }
      
      //Get default geo coordinates
      try{
	      this.defaultLatitude = getVirtualSensorConfiguration().getLatitude().toString();
	      this.defaultLongitude = getVirtualSensorConfiguration().getLongitude().toString();
      } catch(Exception e){
	      this.defaultLatitude = null;
	      this.defaultLongitude = null;
      }
      
      //Setting SVG document
      this.domImpl = GenericDOMImplementation.getDOMImplementation();
      this.sensorDoc = this.domImpl.createDocument(null, "svg", null);
      this.svg2d = new SVGGraphics2D(this.sensorDoc);
      
      //Get geo data
      this.reloadGeoData = true;
      
      return true;
   }
   
   public void dataAvailable ( String inputStreamName , StreamElement streamElement ) {
      if( logger.isDebugEnabled() ) logger.debug( new StringBuilder( "Data received under the name *" ).append( inputStreamName ).append( "* to the SVGDataDisplayVS." ).toString() );
      
      //Remove data older than specified timeout
      if(timeout > 0 && this.dataFromSensors.size() > 0){      	
      	//Get keys
      	String[] keys = this.dataFromSensors.keySet().toArray(new String[0]);
      	
      	for(String i : keys){
      		if( this.dataFromSensors.get(i).isExpired(this.timeout * 60000) ){
      			this.dataFromSensors.remove(i);
      		}
      	}
      }
      
      //Parse StreamElement
      SensorData newSD;
      if(this.separator.equals("!")){
      	newSD = parse(streamElement, SVGDataDisplayVirtualSensor.PARSE_EXCLAMATION);
      } else {
      	newSD = parse(streamElement, SVGDataDisplayVirtualSensor.PARSE_COLON);
      }
      
      if(newSD==null){
      	logger.warn("Coudn't create SensorData object. Ending processing.");
      	return;
      }
      //Make picture for this sensor if allowed by maxNumberofSensors
      if(this.dataFromSensors.containsKey(newSD.getID()) || this.dataFromSensors.size() < this.maxNumberofSensors){
      	
      	//Is it necesary to reload geo data?
      	if( this.dataFromSensors.containsKey(newSD.getID()) && newSD.getLatitude().equals(this.dataFromSensors.get(newSD.getID()).getLatitude()) && newSD.getLongitude().equals(this.dataFromSensors.get(newSD.getID()).getLongitude()) ){
      		this.reloadGeoData = false;
      	} else {
      		this.reloadGeoData = true;
      	}
      	
      	//Save sensor data
      	this.dataFromSensors.put(newSD.getID(), newSD);
      } else {
      	logger.warn( "SVGDataDisplayVS drops the input because it exceeds specified maximum number of images to be plotted." );
      	return;
      }
      
      //Make a SVG image
      makeSVGImage();
      
      //Write SVG data to byte array
      try{/*
	OutputStream outputStream = new FileOutputStream(new File("/home/aurora/Desktop/imagetest.svg"));
	Writer out = new OutputStreamWriter(outputStream, "UTF-8");
	svg2d.stream(out, true);
	outputStream.flush();
	outputStream.close();
	*/
	this.byteArrayOutputStreamData.reset();
      	Writer out = new OutputStreamWriter(this.byteArrayOutputStreamData, "UTF-8");
      	svg2d.stream(out, false); //Write it, use CSS
      	out.flush();
      	out.close();
      } catch (IOException e){
      	System.out.println(e.getMessage());
      	return;
      }
      
      //If no geo data is needed, SE can be sent
      if(!this.includeGeoData){
      	StreamElement output = new StreamElement( new String[] {"Data"}, new Byte[] {DataTypes.BINARY}, new Serializable[] {this.byteArrayOutputStreamData.toByteArray()}, System.currentTimeMillis() );
      	dataProduced( output );
      	return;
      }
      
      //Geo data
      if(this.reloadGeoData){
      		
      	      int geoItemsCounter = 0;
      	      
      	      String[] keys = this.dataFromSensors.keySet().toArray(new String[0]);
      	      Arrays.sort(keys);
      	      	
	      if(this.defaultLatitude != null && this.defaultLongitude != null) geoItemsCounter++;
	      for(String i : keys)
	      	if(this.dataFromSensors.get(i).areCoordinatesSet()) geoItemsCounter++;
	      
	      if(geoItemsCounter > 0){ //If some geo data exists - get geo image from Google
		      String mapURL = "http://maps.googleapis.com/maps/api/staticmap?sensor=false&maptype=roadmap&"; //Making URL query
		      
		      if(this.width > 640) mapURL += "size=640x480" + "&";
		      else mapURL += "size=" + this.width + "x" + ((int)9*this.width/16) + "&";
		      
		      if(this.defaultLatitude != null && this.defaultLongitude != null)
		      	mapURL += "center=" + this.defaultLatitude + "," + this.defaultLongitude + "&markers=color:red|" + this.defaultLatitude + "," + this.defaultLongitude + "&";
		      
		      //Iterate through sorted IDs of sensors and get coordinates
		      char label = 'A';
		      for(String i : keys){
		      	SensorData sd = this.dataFromSensors.get(i);
		      	
		      	if(!sd.areCoordinatesSet()) continue;
		      	
		      	mapURL += "markers=color:blue|label:" + String.valueOf(label) + "|" + sd.getCoordinates() + "&";
		      	label++;
		      	
		      	if(mapURL.length() > 1950) break; //Max URL length is 2048
		      }
		      
		      //Get image
		      try{
		      	BufferedImage mapImage = ImageIO.read(new URL(mapURL)); //Fetch it from Google Maps
		      	this.byteArrayOutputStreamGeo.reset();
		      	ImageIO.write(mapImage, "png", this.byteArrayOutputStreamGeo);
		      } catch(Exception e){
		      	logger.warn("SVGDataDisplayVS: Error fetching geo image.");
		      }
	      }
      }
      
      //Create StreamElement - field names must correspond output names declared in XML file
      StreamElement output = new StreamElement( new String[] {"Data", "Geo"}, new Byte[] {DataTypes.BINARY, DataTypes.BINARY}, new Serializable[] {this.byteArrayOutputStreamData.toByteArray(), this.byteArrayOutputStreamGeo.toByteArray()}, System.currentTimeMillis() );
      
      //Publish StreamElement
      dataProduced( output );
   }
   
   public void dispose(){
   }
}

/*
 *This was moved to gsn/beans
 *
class SensorData{
	
	//Sensor ID
	private final int ID;
	
	//Sensor Data
	private HashMap <String, Double> data = new HashMap <String, Double>();
	
	//Timestamp for received data
	private final long timestamp;
	
	//Geo Coordinates
	private String latitude;
	private String longitude;
	
	public SensorData(int inID, long inTimestamp){
		this.ID = inID;
		this.timestamp = inTimestamp;
		
		latitude = null;
		longitude = null;
	}
	
	public void put(String inKey, Double inValue){
		this.data.put(inKey, inValue);
	}
	
	public String[] getKeys(){
		return this.data.keySet().toArray(new String[0]);
	}
	
	public Double get(String inKey){
		return this.data.get(inKey);
	}
	
	public void remove(String inKey){
		this.data.remove(inKey);
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public int getID(){
		return this.ID;
	}
	
	public int getNumberOfElements(){
		return this.data.size();
	}
	
	public boolean isExpired(long inTimestamp){
		if(this.timestamp < inTimestamp)
			return true;
		return false;
	}
	
	public void setLatitude(String inLatitude){
		if(inLatitude.trim().equalsIgnoreCase("null") || inLatitude.trim().equalsIgnoreCase("") || inLatitude == null)
			return;
			
		try{
			if(Double.parseDouble(inLatitude) < -90 || Double.parseDouble(inLatitude) > 90 ) return;
		}catch(Exception e){		
		}
		
		this.latitude = inLatitude;
	}
	
	public void setLongitude(String inLongitude){
		if(inLongitude.trim().equalsIgnoreCase("null") || inLongitude.trim().equalsIgnoreCase("") || inLongitude == null)
			return;
		
		try{
			if(Double.parseDouble(inLongitude) < -180 || Double.parseDouble(inLongitude) > 180 ) return;
		}catch(Exception e){		
		}

		this.longitude = inLongitude;
	}
	
	public String getCoordinates(){
		return this.latitude + "," + this.longitude;
	}
	
	public String getLatitude(){
		return this.latitude;
	}
	
	public String getLongitude(){
		return this.longitude;
	}
	
	public boolean areCoordinatesSet(){
		if(this.latitude == null || this.longitude == null)
			return false;
		else
			return true;
	}
}*/
