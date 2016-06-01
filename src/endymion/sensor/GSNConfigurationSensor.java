package endymion.sensor;

/**
 * Created by Nikola on 21.04.2015.
 */
public class GSNConfigurationSensor extends GSNSensor {

    protected boolean only_last_value;
    protected double sampling_rate;

    public GSNConfigurationSensor () {
        super();
        only_last_value = false;
    }

    public GSNConfigurationSensor (String vSensor) {
        super(vSensor);
        only_last_value = false;
    }

    public boolean isOnly_last_value() {
        return only_last_value;
    }

    public void setOnly_last_value(boolean only_last_value) {
        this.only_last_value = only_last_value;
    }

    public double getSampling_rate() {
        return sampling_rate;
    }

    public void setSampling_rate(double sampling_rate) {
        this.sampling_rate = sampling_rate;
    }
}
