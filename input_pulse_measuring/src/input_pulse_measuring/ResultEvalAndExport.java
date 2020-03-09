package input_pulse_measuring;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ResultEvalAndExport {

	public ArrayList<Pulse> pulses;
	public ArrayList<Pulse> modPulses;
	public ArrayList<Double> perfect;
	public ArrayList<Double> deviations;

	public long time;
	public int presses;
	public long start;

	public double avgFrequency;
	public double avgDeviation;

	public MainFrame m;
	public String resultstr;

	public ResultEvalAndExport(ArrayList<Pulse> pulses, MainFrame m) {
		this.m = m;
		this.pulses = pulses;
		this.resultstr = "";
		this.modPulses = new ArrayList<>(pulses);
		if (pulses.size() < Options.PRECUT + 2) {
			return;
		}
		for (int i = 0; i < Options.PRECUT; i++) {
			modPulses.remove(0);
		}

		long endtime = modPulses.get(modPulses.size() - 1).middlePress;
		start = modPulses.get(0).middlePress;
		presses = modPulses.size() - 1;
		time = endtime - start;
		avgFrequency = (double) presses / ((double) time / 1000);
		perfect = new ArrayList<>();
		deviations = new ArrayList<>();
		double ms = 1000 / this.avgFrequency;
		for (int i = 0; i < modPulses.size(); i++) {
			perfect.add(i * ms);
			deviations.add(modPulses.get(i).middlePress - start - perfect.get(i));
		}
		avgDeviation = deviations.stream().reduce(0.0, (x, y) -> (x + y)) / deviations.size();

	}

	public void printResults() {
		resultstr = "";
		resultstr += "\n~~~~~~~~~~~RESULTS~~~~~~~~~~~~~~~~~~~~\nTEST STOPPED: AVG FREQUENCY: ";
		if (pulses.size() < Options.PRECUT + 2) {
			resultstr += "\n >> NOT ENOUGH PULSES";
		} else {
			resultstr += String.valueOf(avgFrequency);
			resultstr += "\n AVG DEVIATION: " + String.valueOf(avgDeviation);
			resultstr += "\n Presses (excluding first " + Options.PRECUT + ") : " + String.valueOf(presses + 1)
					+ " Presses | Time: " + String.valueOf((double) time / 1000) + " seconds";
			resultstr += "\n~~~~~~~~~~~~RESULTS END~~~~~~~~~~~~~~~~~~~";
		}
		m.println(resultstr, false);
	}

	public void export() throws IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = format.format(new Date()) + ".txt";
		Path pathToFile = Paths.get(Options.EXPORTPATH, filename);
		Files.createDirectories(pathToFile.getParent());
		Files.createFile(pathToFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile.toFile()));
		writer.write(Options.optToString());
		writer.write(resultstr);
		writer.write("\n########### DATAPOINTS: ###############");
		writer.write("\nINDEX	START	MIDDLE	END	DURATION	PERFECT	DIFFERENCE");
		modPulses.stream().forEach(x -> {
			try {
				writer.write(x.toString(start, modPulses, perfect));
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		writer.close();
		m.println("\n >> Data saved to " + pathToFile.toString() + " <<", false);
		m.println("\n####################################################", false);
	}
}
