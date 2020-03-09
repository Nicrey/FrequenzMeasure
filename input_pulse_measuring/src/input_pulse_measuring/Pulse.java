package input_pulse_measuring;

import java.util.ArrayList;

public class Pulse {

	public long startPress;
	public long endPress;
	public long duration;
	public long middlePress;

	public Pulse(long startPress, long endPress) {
		super();
		this.startPress = startPress;
		this.endPress = endPress;
		this.duration = endPress - startPress;
		this.middlePress = startPress + duration / 2;
	}

	public String toString(long start, ArrayList<Pulse> pulses, ArrayList<Double> perfect) {
		int index = pulses.indexOf(this);
		return "\n" + index + "	" + (startPress - start) + "	" + (middlePress - start) + "	" + (endPress - start)
				+ "	" + duration + "	" + perfect.get(index) + "	" + ((middlePress - start) - perfect.get(index));
	}
}
