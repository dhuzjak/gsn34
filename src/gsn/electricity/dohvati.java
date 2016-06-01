package gsn.electricity;

import jssc.SerialPort;
import jssc.SerialPortTimeoutException;

import com.sun.org.apache.xpath.internal.operations.Equals;
import jssc.SerialPortException;
/**
 *
 * @author Igor
 */
public class dohvati {
    String[] s = new String[8];
    //String portName = "COM9";
    
    public String [] reading(String portName) throws InterruptedException {
        
        byte[] acknowledgeMessage = new byte[]{0x06,0x30,0x30,0x31,0x0D,0x0A};
        byte[] closeMessage=new byte[]{0x01,0x42,0x30,0x03,0x71};
        
        byte[] fullPowerMessage=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x30,0x28,0x29,0x03,0x5a};
        byte[] fullPowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x31,0x28,0x29,0x03,0x5b};
        byte[] fullPowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x32,0x28,0x29,0x03,0x58};
    
        byte[] maxCummulativePowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x32,0x2e,0x31,0x28,0x29,0x03,0x51};
        byte[] maxCummulativePowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x32,0x2e,0x32,0x28,0x29,0x03,0x52};
    
        byte[] currentPowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x34,0x2e,0x31,0x28,0x29,0x03,0x57};
        byte[] currentPowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x34,0x2e,0x32,0x28,0x29,0x03,0x54};
    
        byte[] currentPhase1=new byte[] {0x01, 0x52, 0x31, 0x02, 0x33, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x66};
        byte[] currentPhase2=new byte[] {0x01, 0x52, 0x31, 0x02, 0x35, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x60};
        byte[] currentPhase3=new byte[] {0x01, 0x52, 0x31, 0x02, 0x37, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x62};
        
        SerialPort serialPort = new SerialPort(portName);
        
        try {
            serialPort.openPort();
            serialPort.setParams(300, 7, 1, 2);
            
            serialPort.writeBytes("/?!\r\n".getBytes());
            serialPort.readString(15 , 1000);
            Thread.sleep(800);
            //if(!(serialPort.readString(15)).equals(""))           
            serialPort.writeBytes(acknowledgeMessage);
            while(!(serialPort.readString(1, 1000).equals(")")))
            //System.out.println(serialPort.readString(1));
            Thread.sleep(1);
            Thread.sleep(800);
            //if(!(serialPort.readString(11)).equals(""))
            serialPort.writeBytes(fullPowerMessage);
            s[0] = serialPort.readString(18, 1000);
            Thread.sleep(100);
            //if(!(s[0].equals("")))
            serialPort.writeBytes(currentPhase1);
            s[1] = serialPort.readString(10, 1000);
            Thread.sleep(100);
            //if(!(s[1].equals("")))
            serialPort.writeBytes(maxCummulativePowerT1Message);
            s[2] = serialPort.readString(15, 1000);
            Thread.sleep(100);
            
            serialPort.writeBytes(maxCummulativePowerT2Message);
            s[3] = serialPort.readString(15, 1000);
            Thread.sleep(100);
            
            serialPort.writeBytes(fullPowerT1Message);
            s[4] = serialPort.readString(17, 1000);
            Thread.sleep(100);
            
            serialPort.writeBytes(fullPowerT2Message);
            s[5] = serialPort.readString(17, 1000);
            Thread.sleep(100);
            
            serialPort.writeBytes(currentPhase2);
            s[6] = serialPort.readString(10, 1000);
            Thread.sleep(100);
            
            serialPort.writeBytes(currentPhase3);
            s[7] = serialPort.readString(10, 1000);
            Thread.sleep(100);
            
            /*System.out.println("Current Power T1 Message successfully writen to port: " + serialPort.writeBytes(currentPowerT1Message));
            s[8] = serialPort.readString(19);
            Thread.sleep(100);
            
            System.out.println("Current Power T2 Message successfully writen to port: " + serialPort.writeBytes(currentPowerT2Message));
            s[9] = serialPort.readString(19);
            Thread.sleep(100);*/
            
            serialPort.writeBytes(closeMessage);
            serialPort.readString(1, 1000);
            //if(!(serialPort.readString(2)).equals(""))          
            serialPort.closePort();
            //System.out.println(s);
        }
        catch (SerialPortException ex){
            System.out.println(ex);
        } catch (SerialPortTimeoutException ex) {
        	ex.printStackTrace();
        }
        return s;
    }
    
}
