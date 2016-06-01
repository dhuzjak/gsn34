package gsn.wrappers.rasip;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.DataTypes;
import gsn.wrappers.*;

import java.net.*;
import java.io.*;

public class SerialNetworkWrapper extends AbstractWrapper {

	private final int varcharDataLength = 100;

	private transient DataField[] outputStructureCache = new DataField[] {new DataField( "ID", "INTEGER", "Sensor ID" ), new DataField( "latitude", "DOUBLE", "Sensor latitude" ), new DataField( "longitude", "DOUBLE", "Sensor longitude" ), new DataField( "DATA", "VARCHAR(" + varcharDataLength + ")", "Sensor data" )};
	private final transient Logger logger = Logger.getLogger( SerialNetworkWrapper.class );
	private static ServerSocket serverSock;
	private static int port = 5556;
	private static final int maxClients = 10;

	@Override
	public DataField[] getOutputFormat() {
		return outputStructureCache;
	}

	@Override
	public boolean initialize() {
	
		logger.warn("Initializing SerialNetworkWrapper");
	
		int paramPort = -1;
		
		try{
			paramPort = Integer.parseInt(getActiveAddressBean().getPredicateValue("port"));
		} catch(Exception e){
			logger.warn("Error parsing port value.");
			paramPort = -1;
		}
		
		if(paramPort > 0){
			logger.warn("Setting port to value: " + paramPort);
			this.port = paramPort;
		}else{
			logger.warn("Setting port to default value: " + this.port);
		}
	
		logger.warn( "Creating socket - TCP port " + this.port + "." );

		try {
			serverSock = new ServerSocket( port, maxClients );
		} catch ( Exception e ) {
			logger.error( "Creating socket unsuccessful. " + e.getMessage() );
			return false;
		}

		return true;
	}

	@Override
	public void dispose() {
		try {
			serverSock.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public String getWrapperName() {
		return "Network server for serialized data";
	}

	private void sendSE( String str ) {

		String[] bits = str.split( ";" );

		int ID = -1;
		double latitude = 91, longitude = 181;
		String data = "";
		StreamElement se;

		for ( int i = 0; i < bits.length; i++ ) {
			String[] s = bits[i].split( ":" );
			
			if(s.length != 2) continue;
			
			if(s[0].trim().equalsIgnoreCase("ID")){
				try {
					ID = Integer.parseInt( s[1] );
				} catch ( Exception e ) {
					System.out.println( e.getMessage() );
					System.out.println( "Error parsing data. Data ignored." );
				}
				
				continue;
			}
			
			if(s[0].trim().equalsIgnoreCase("latitude")){
				try {
					latitude = Double.parseDouble( s[1] );
				} catch ( Exception e ) {
					System.out.println( e.getMessage() );
					System.out.println( "Error parsing data. Data ignored." );
				}
				
				continue;
			}
			
			if(s[0].trim().equalsIgnoreCase("longitude")){
				try {
					longitude = Double.parseDouble( s[1] );
				} catch ( Exception e ) {
					System.out.println( e.getMessage() );
					System.out.println( "Error parsing data. Data ignored." );
				}
				
				continue;
			}
			
			data += s[0].trim() + ":" + s[1].trim();
			if(i != bits.length -1) data += ";";
		}
		
		if(ID == -1  || data.equals("")){
			logger.warn("There is something wrong with data. Cannot proceed.");
			return;
		}
		
		se = new StreamElement( new String[] {"ID", "latitude", "longitude", "DATA"}, new Byte[] {DataTypes.INTEGER, DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.VARCHAR}, new Serializable[] {ID, latitude, longitude, data}, System.currentTimeMillis() );
		postStreamElement( se );
	}

	@Override
	public void run() {

		Socket clientSocket;

		BufferedReader in;
		PrintWriter out;

		while ( isActive() ) {
			try {

				clientSocket = serverSock.accept();
				logger.info( "Client connected." );

				clientSocket.setSoTimeout( 10000 );

				in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
				out = new PrintWriter( clientSocket.getOutputStream() );

				//Slanje upita
				out.println( "?" );
				out.flush();

				//Citanje odgovora koji je oblika <fielda name>:<value>;<field name>:<value>;...
				String line;

				try {
					line = in.readLine();
				} catch ( Exception e ) {
					System.out.println( e.getMessage() );
					out.println( "NO DATA" );
					out.flush();
					clientSocket.close();
					continue;
				}

				line = line.replace( "*HELLO*", "" ).trim();

				//Obrada podataka
				sendSE( line );

				//Slanje odgovora
				out.println( "DONE" );
				out.flush();

				//Zatvaranje komunikacije
				clientSocket.close();
				logger.info( "Client disconnected." );

			} catch ( IOException e ) {
				System.out.println( e.getMessage() );
			}
		}
	}
}
