package gsn.vsensor;

import gsn.beans.DataTypes;
import gsn.beans.SensorData;
import gsn.beans.StreamElement;
import gsn.utils.ParamParser;
import hr.fer.rasip.wrappers.FieldMappings;

import java.io.IOException;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.DecimalFormat;
import java.awt.BasicStroke;
import java.net.URL;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
 
 /**
  * This VS takes the following parameters (in XML description file)
  * 'field-names' - represents physical properties measured, separated by commas
  * 'units' - represents units for field names; strings, separated by commas
  * 'max-sensor-number' - integer
  * 'width' - integer (>= 250)
  * 'timeout' - string, data timeout in minutes, 0 for no timeout
  * 'display' - string; "METER" (default)/ "BAR" / "BOOLEAN", separated by commas; how field data is displayed
  */
  
 /**
  * This VS should get StreamElements that look like:
  * 'ID' <int> represents a unique sensor
  * 'latitude' <double> represents sensor's latitude
  * 'longitude' <double> represents sensor's longitude
  * 'DATA' <string> string that contains measured data <field name>:<value>;<field name>:<value>;....
  * Each ID (sensor) will get its own part of the plot
  */
    
public class DataDisplayVirtualSensor extends AbstractVirtualSensor {
   
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
   private int maxNumberofSensors; //Maximum number of resulting plots displayed
   
   private int width; //Resulting image width
   private final int gap = 20; //Gap between field plots
   
   private int timeout; //Timeout
   
   private final int iconWidth = 250;
   private final int iconHeight = 250;
   
   //Default latitude and longitude specified in VS description file
   private String defaultLatitude;
   private String defaultLongitude;
   private final int mapImageHeight = 400; //Map image height (must be less than 480 - Google policy)
   
   //Will hold resulting output
   private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( 64 * 1024 );
   
   //This holds data for each of the sensors
   private final HashMap <String, SensorData> dataFromSensors = new HashMap <String, SensorData>();
   
   //This holds subimages for each of the sensors
   private final HashMap <String, BufferedImage> images = new HashMap <String, BufferedImage>();
   
   //This is a string formatter for data and time
   private final SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
   
   private BufferedImage makeImageFromSensorData(SensorData inSensorData){
   
   	//Calculate number of icons
	int totalNumberOfIcons = inSensorData.getNumberOfElements();
	int iconsPerRow = this.width / this.iconWidth;
	int imageHeight = this.iconHeight * (totalNumberOfIcons / iconsPerRow);
	
	//How many vertical gaps will there be?
	int verticalGaps = totalNumberOfIcons / iconsPerRow;
	if(totalNumberOfIcons % iconsPerRow != 0) verticalGaps++;
	verticalGaps--;
	
	if(totalNumberOfIcons % iconsPerRow != 0) imageHeight += this.iconHeight;
	
   	BufferedImage img = new BufferedImage(this.width, imageHeight + 65 + this.gap * verticalGaps, BufferedImage.TYPE_INT_ARGB);
   	
   	Graphics2D g2d = img.createGraphics();
   	
   	//Write sensor ID and timestamp
   	g2d.setPaint(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        String s = "Sensor ID: " + inSensorData.getID();
        
	int x = 5;
	int y = g2d.getFontMetrics().getHeight();
	
	g2d.drawString(s, x, y);
	g2d.drawLine(x, y+2, x + g2d.getFontMetrics().stringWidth(s), y+2);
	
	g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        s = "Measured on: " + timeFormat.format( new Date(inSensorData.getTimestamp()) ).toString();
        
	y += g2d.getFontMetrics().getHeight() + 5;
	
	g2d.drawString(s, x, y);
	
	//Move downwards
	y += 10;
	x = 0;
	int iconsPasted = 0; //Counter for icons that have been pasted into big picture
	
	//Make icons for each field	
	for(int i = 0; i < this.fields.length; i++){
		Double data = inSensorData.get(this.fields[i]);
	
		if( data == null )
			continue;
		
		BufferedImage icon = null;
		
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
		if( this.display[i] == DataDisplayVirtualSensor.BAR ){
			//Drawing bar
			ThermometerPlot plot = new ThermometerPlot(new DefaultValueDataset(data));
			
			plot.setSubrangeInfo(ThermometerPlot.NORMAL, 10000.0, 11000.0);
			plot.setSubrangeInfo(ThermometerPlot.WARNING, 11000.0, 12000.0);
			plot.setSubrangeInfo(ThermometerPlot.CRITICAL, 12000.0, 13000.0);

			plot.setBulbRadius(ThermometerPlot.NONE);
			plot.setGap(ThermometerPlot.NONE);
			plot.setUnits(ThermometerPlot.NONE);
			plot.setMercuryPaint(new Color(0x5998EB));
			plot.setValueLocation(ThermometerPlot.RIGHT);
			plot.setValuePaint(Color.black);	
			plot.setValueFormat(new DecimalFormat("'  '#" + "'" + this.units[i] + "'"));
			plot.setRange(lower, upper);
			plot.setColumnRadius(11);
			plot.setThermometerStroke(new BasicStroke(2.0f));
			plot.setThermometerPaint(Color.black);
			plot.setBackgroundAlpha(0);
			plot.setOutlineVisible(false);
			
			//Make it into bufferedimage
			JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
			chart.addSubtitle( new TextTitle( this.fields[i], new Font("SansSerif", Font.PLAIN, 16) ) );
			chart.setBorderVisible(false);
			icon = chart.createBufferedImage(this.iconWidth, this.iconHeight);
			
		} else if( this.display[i] == DataDisplayVirtualSensor.BOOLEAN ){
		
			icon = new BufferedImage(this.iconWidth, this.iconHeight, BufferedImage.TYPE_INT_ARGB);
			
			//Get switch icon
			String path;
			
			if( data != 0 ){
				//Switch is on
				path = "./icons/onswitch.png";
			} else {
				//Switch is off
				path = "./icons/offswitch.png";
			}
			
			BufferedImage flipFlop;
			try{		
				flipFlop = ImageIO.read(new File(path));
			} catch (IOException ioe){
				logger.warn("Cannot open image of a switch");
				logger.warn(ioe.getMessage(), ioe);
				
				flipFlop = new BufferedImage(this.iconWidth, this.iconHeight, BufferedImage.TYPE_INT_ARGB);
				
				Graphics2D gerr = flipFlop.createGraphics();
				
				String err = "Error getting icon";
				gerr.setPaint(Color.BLACK);
				gerr.setFont(new Font("SansSerif", Font.PLAIN, 16));
				
				gerr.drawString(err, (flipFlop.getWidth() - gerr.getFontMetrics().stringWidth(err)) / 2, gerr.getFontMetrics().getHeight() + 40);
				
				gerr.dispose();
			}
			
			//Paste it into icon image
			Graphics2D g = icon.createGraphics();
			g.setPaint(JFreeChart.DEFAULT_BACKGROUND_PAINT);
			g.fillRect(0, 0, icon.getWidth(), icon.getHeight() );
			
			int posY = (icon.getHeight() - flipFlop.getHeight()) / 2;
			int posX = (icon.getWidth() - flipFlop.getWidth()) / 2;
			
			if(posX <= 0) posX = 0;
			if(posY <= 0) posY = 0;
			
			g.drawImage(flipFlop, posX, posY, null);
			
			//Write field name on top of icon
			String fieldName = this.fields[i];
			g.setPaint(Color.BLACK);
			g.setFont(new Font("SansSerif", Font.PLAIN, 16));
			
			posX = (icon.getWidth() - g.getFontMetrics().stringWidth(fieldName)) / 2;
			posY = g.getFontMetrics().getHeight();
			g.drawString(fieldName, posX, posY);
			
			g.dispose();
		
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
			
			//Make it into bufferedimage
			JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
			chart.addSubtitle( new TextTitle( this.fields[i], new Font("SansSerif", Font.PLAIN, 16) ) );
			chart.setBorderVisible(false);
			icon = chart.createBufferedImage(this.iconWidth, this.iconHeight);				
		} else {
			//Meter
			DialPlot dialplot = new DialPlot();
			
			//Data
			dialplot.setDataset(0, new DefaultValueDataset(data));

			//Drawing
			StandardDialFrame standarddialframe = new StandardDialFrame();
			standarddialframe.setBackgroundPaint(Color.BLACK);
			standarddialframe.setForegroundPaint(Color.BLACK);
			dialplot.setDialFrame(standarddialframe);

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
	
			JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, dialplot, false);
			chart.addSubtitle( new TextTitle( this.fields[i], new Font("SansSerif", Font.PLAIN, 16) ) );
			chart.setBorderVisible(false);
			icon = chart.createBufferedImage(this.iconWidth, this.iconHeight);
		}
		
		//Paste icon into big subpicture
		g2d.drawImage(icon, x, y, null);
		
		iconsPasted++;
		
		if(iconsPasted % iconsPerRow == 0){
			x = 0;
			y += this.iconHeight + gap;
		} else {
			x += this.iconWidth + gap;
			//x += (this.width - (iconsPerRow * this.iconWidth)) / (iconsPerRow - 1); //Distribute icons evenly
		}
	}
	
	g2d.dispose();
	return img;
   }
   
   private boolean checkFieldDeclared(String inStr){
   	for(int i = 0; i < this.fields.length; i++)
   		if(this.fields[i].equalsIgnoreCase(inStr.trim()))
   			return true;
   	
   	return false;
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
      		this.display[i] = DataDisplayVirtualSensor.METER;
      		continue;
      	}
      	
      	parameterDisplay[i] = parameterDisplay[i].trim();
      
      	if(parameterDisplay[i].equalsIgnoreCase("BOOLEAN"))
      		this.display[i] = DataDisplayVirtualSensor.BOOLEAN;
      	else if(parameterDisplay[i].equalsIgnoreCase("BAR"))
      		this.display[i] = DataDisplayVirtualSensor.BAR;
      	else if(parameterDisplay[i].equalsIgnoreCase("THERMO"))
      		this.display[i] = SVGDataDisplayVirtualSensor.THERMO;
      	else
      		this.display[i] = DataDisplayVirtualSensor.METER;
      }
      
      //Get default geo coordinates
      try{
	      this.defaultLatitude = getVirtualSensorConfiguration().getLatitude().toString();
	      this.defaultLongitude = getVirtualSensorConfiguration().getLongitude().toString();
      } catch(Exception e){
	      this.defaultLatitude = null;
	      this.defaultLongitude = null;
      }
      
      return true;
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
   
   public void dataAvailable ( String inputStreamName , StreamElement streamElement ) {
      if( logger.isDebugEnabled() ) logger.debug( new StringBuilder( "Data received under the name *" ).append( inputStreamName ).append( "* to the DataDisplayVS." ).toString() );
      streamElement=new StreamElement(new String[] {"moteID", "data"}, new Byte[] {DataTypes.VARCHAR, DataTypes.VARCHAR}, new Serializable[] {"mote154", "!ta!23.3!ta!!ha!4!ha!!b!76!b!"}, System.currentTimeMillis());
      //Remove data older than specified timeout
      if(timeout > 0 && this.dataFromSensors.size() > 0){
      	long current = System.currentTimeMillis();
      	
      	//Get keys
      	String[] keys = this.dataFromSensors.keySet().toArray(new String[0]);
      	
      	for(String i : keys){
      		if( this.dataFromSensors.get(i).isExpired(this.timeout * 60000) ){
      			this.dataFromSensors.remove(i);
      			this.images.remove(i);
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
      	      	
      	this.dataFromSensors.put(newSD.getID(), newSD);
      
      	//Make image and add it
      	BufferedImage genImg = this.makeImageFromSensorData(newSD);      	
      	this.images.put(newSD.getID(), genImg);
      } else {
      	logger.warn( "DataDisplayVS drops the input because it exceeds specified maximum number of images to be plotted." );
      	return;
      }

      //Get keys and sort them to preserve order of images
      String[] keys = this.dataFromSensors.keySet().toArray(new String[0]);
      Arrays.sort(keys);
      
      //Conecting all subimages to make a single image
      int height = 0;      
      for(String i : keys) height += this.images.get(i).getHeight(); //Sum of heights of all sensors
      
      BufferedImage img = new BufferedImage( this.width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = img.createGraphics();
      
      //Iterate through sorted IDs of sensors and connect images
      int yCoordinate = 0;
      for(String i : keys){
      	g2d.drawImage(this.images.get(i), 0, yCoordinate, null);
      	yCoordinate += this.images.get(i).getHeight();
      }
      
      g2d.dispose();
      
      //Add geo data image from Google Maps
      //Is there any geo data?
      int geoItemsCounter = 0;
      
      if(this.defaultLatitude != null && this.defaultLongitude != null) geoItemsCounter++;
      for(String i : keys)
      	if(this.dataFromSensors.get(i).areCoordinatesSet()) geoItemsCounter++;
      
      if(geoItemsCounter > 0){ //If some geo data exists - get geo image from Google
	      String mapURL = "http://maps.googleapis.com/maps/api/staticmap?sensor=false&maptype=roadmap&"; //Making URL query
	      
	      if(this.width > 640) mapURL += "size=640x" + this.mapImageHeight + "&";
	      else mapURL += "size=" + this.width + "x" + this.mapImageHeight + "&";
	      
	      if(this.defaultLatitude != null && this.defaultLongitude != null)
	      	mapURL += "center=" + this.defaultLatitude + "," + this.defaultLongitude + "&markers=color:red|" + this.defaultLatitude + "," + this.defaultLongitude + "&";
	      
	      //Iterate through sorted IDs of sensors and get coordinates
	      char label = 'A';
	      for(String i : keys){
	      	SensorData sd = this.dataFromSensors.get(i);
	      	
	      	if(!sd.areCoordinatesSet()) continue;
	      	
	      	mapURL += "markers=color:green|label:" + String.valueOf(label) + "|" + sd.getCoordinates() + "&";
	      	label++;
	      	
	      	if(mapURL.length() > 1950) break; //Max URL length is 2048
	      }
	      
	      //Expand existing image with data from sensors
	      BufferedImage newImage = new BufferedImage(this.width, height + this.mapImageHeight, BufferedImage.TYPE_INT_ARGB);
	      Graphics2D ng = newImage.createGraphics();	      
	      ng.drawImage(img, 0, 0, null);
	      ng.dispose();
	      
	      img = newImage;
	      g2d = img.createGraphics();
	      try{
	      	BufferedImage mapImage = ImageIO.read(new URL(mapURL)); //Fetch it from Google Maps
	      	g2d.drawImage(mapImage, 0, img.getHeight() - mapImage.getHeight(), null); //And add it to the bottom
	      } catch(Exception e){
	      	g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
	      	g2d.setColor(Color.BLACK);
	      	g2d.drawString("Error fetching map data from Google Maps", 5, img.getHeight() - 20); //Write error message
	      }
	      
	      //Finish drawing output image
	      g2d.dispose();
      }      
      
      //Write chart to PNG byte array (this.byteArrayOutputStream)
      this.byteArrayOutputStream.reset();
      try{
      	ImageIO.write(img, "png", this.byteArrayOutputStream);
      } catch( IOException e ) {
         logger.warn( e.getMessage(), e );
         return;
      }
      
      //Create StreamElement - field names must correspond output names declared in XML file
      StreamElement output = new StreamElement( new String[] {"Data"}, new Byte[] {DataTypes.BINARY}, new Serializable[] {this.byteArrayOutputStream.toByteArray()}, System.currentTimeMillis() );
      
      //Push generated image
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
	
	public boolean areCoordinatesSet(){
		if(this.latitude == null || this.longitude == null)
			return false;
		else
			return true;
	}
}*/
