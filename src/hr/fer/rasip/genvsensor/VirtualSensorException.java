package hr.fer.rasip.genvsensor;

public class VirtualSensorException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public VirtualSensorException() {
		super();
	}
	
	public VirtualSensorException(String message) {
		super(message);
	}
	
	public VirtualSensorException(Throwable t) {
		super(t);
	}
	
	public VirtualSensorException(Throwable t, String message) {
		super(message, t);
	}
}
