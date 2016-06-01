package gsn.wrappers.rasip;

import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;
import gsn.wrappers.general.UDPWrapper;
import gsn.beans.AddressBean;
import gsn.beans.StreamElement;
import gsn.beans.DataTypes;

import java.io.InputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.tempuri.DataToString;

public class mUDPWrapper extends AbstractWrapper {
	
	private static final String    FIELD_NAME_DATA    = "DATA";
	
	private final transient Logger logger        = Logger.getLogger( mUDPWrapper.class );
	   
	private int                    threadCounter = 0;
	   
	public InputStream             is;
	   
	private AddressBean            addressBean;
	
	private int                    port;
	
	private DatagramSocket         socket;
	
	private int dataLength;
	
	public boolean initialize (  ) {
	      addressBean = getActiveAddressBean( );
	      dataLength = addressBean.getPredicateValueAsInt("data-string-length",1024);
	      try {
	         port = Integer.parseInt( addressBean.getPredicateValue( "port" ) );
	         socket = new DatagramSocket( port );
	      } catch ( Exception e ) {
	         logger.warn( e.getMessage( ) , e );
	         return false;
	      }
	      setName( "mUDPWrapper-Thread" + ( ++threadCounter ) );
	      return true;
	}
	
	public void run ( ) {
	      byte [ ] receivedData = new byte [ 1024 ];
	      DatagramPacket receivedPacket = null;
	      while ( isActive( ) ) {
	         try {
	            receivedPacket = new DatagramPacket( receivedData , receivedData.length );
	            socket.receive( receivedPacket );
	            String dataRead = new String( receivedPacket.getData( ) );
	            dataRead = dataRead.split("!end!")[0];
	            dataRead += "!end!";
	            if ( logger.isDebugEnabled( ) ) logger.debug( "mUDPWrapper received a packet : " + dataRead );
	            StreamElement streamElement = new StreamElement( new String [ ] { FIELD_NAME_DATA } , new Byte [ ] { DataTypes.VARCHAR } , new Serializable [ ] { dataRead.trim().replaceAll("\u0000", "")} , System.currentTimeMillis( ) );
	            postStreamElement( streamElement );	            
	         } catch ( IOException e ) {
	            logger.warn( "Error while receiving data on UDP socket : " + e.getMessage( ) );
	         }
	      }
	   }
	
	public  DataField [] getOutputFormat ( ) {
	      return new DataField[] {new DataField( FIELD_NAME_DATA , "VARCHAR(" +dataLength+")", "The packet contains varchar data received as a UDP packet." ) };
	     
	   }
	
	public void dispose (  ) {
	      threadCounter--;
	   }
	   public String getWrapperName() {
	    return "mobile network udp";
	}
	   public static void main ( String [ ] args ) {
	   // To check if the wrapper works properly.
	   // this method is not going to be used by the system.
	   }
		

}
