package hr.fer.rasip.LedMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class LedMatrix {
	
	private static final int I2C_ADDR = 0x70;
    //newer raspberry pi use bus nr 1
    //private static final int I2C_BUS_NR = 0;
    private static final int I2C_BUS_NR = 1;

    private static final byte TURN_ON_OSCILLATOR = (byte) 0x21;
    private static final byte ENABLE_DISPLAY_NO_BLINKING = (byte)0x81;
    private static final byte ENABLE_DISPLAY_BLINK_2HZ = (byte)0x83;
    private static final byte ENABLE_DISPLAY_BLINK_1HZ = (byte)0x85;
    private static final byte ENABLE_DISPLAY_BLINK_HALFHZ = (byte)0x87;
    private static final byte BRIGHTNESS_FULL = (byte) 0xef;
    private static final byte BRIGHTNESS_HALF = (byte) 0xe7;
    public static final int delay = 50;

    private final byte[] rows = new byte[]{ (byte)0x00, (byte)0x02,(byte)0x04,
                                (byte)0x06,(byte)0x08,(byte)0x0a,(byte)0x0c,(byte)0x0e };


    private static List<Byte> displayBuffer = new ArrayList<Byte>();
    static {
        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);

        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);
        displayBuffer.add((byte) 0x00);

    }

    private I2CDevice device;

    public LedMatrix() {
        initializeDevice();
    }
	
	private I2CDevice initializeDevice() {
        device = null;
        try {
            I2CBus bus = I2CFactory.getInstance(I2C_BUS_NR);
            device = bus.getDevice(I2C_ADDR);

            device.write(TURN_ON_OSCILLATOR);
            device.write(ENABLE_DISPLAY_NO_BLINKING);
            device.write(BRIGHTNESS_HALF);

            clearDisplay();
            writeDisplay();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
	
        return device;
    }
    
    public void writeGraphic(byte[] graphic) {
        for(int i=0;i<8;i++) {
            displayBuffer.set(7-i, graphic[i]);
        }
    }

	public void togglePixel(int position) throws IOException {
		
		int y = position % 8;
        int x = position / 8;

		byte value = displayBuffer.get(7-y);
        byte newValue;
	
		newValue = (byte) (value ^ (1 << x));
		displayBuffer.set(7-y, newValue);

	}

    public void drawPixel(int position, boolean on) throws IOException {
    
        int y = position % 8;
        int x = position / 8;
        
        drawPixel(x, y, on);
    }

	

    public void drawPixel(int x, int y, boolean on) throws IOException {
        //System.out.println("x: "+x+"y:"+y);

        byte value = displayBuffer.get(7-y);
        byte newValue;
        if (on) {
            newValue = (byte) (value | (1 << x));
        } else {
            newValue = (byte) (value & ~(1 << x));
        }

        displayBuffer.set(7-y, newValue);
    }

    public void writeDisplay() throws IOException {

        int i = 0;
        for (byte rowValue : displayBuffer) {
            byte line = (byte) ((0xfe & rowValue) >> 1 | (0x01 & rowValue) << 7);
            device.write((byte) i, line);
            i+=2;
        }

    }
    
    public void clearDisplay() throws IOException {

        for(int i=0;i<8;i++) {
            displayBuffer.set(i, (byte) 0x00);
        }
        writeDisplay();

    }
    
    public void setBrightness(String brightness) throws IOException{
    	
    	if(device != null){
	    	switch(brightness){
	    	
	    	case "full":device.write(BRIGHTNESS_FULL);
	    				break;
	    				
	    	case "half":device.write(BRIGHTNESS_HALF);
						break;	
	    	}
    	}
    }

}
