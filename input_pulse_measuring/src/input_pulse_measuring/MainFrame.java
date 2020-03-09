package input_pulse_measuring;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import input_pulse_measuring.Options.Mode;
import input_pulse_measuring.Options.OptionState;

public class MainFrame extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	long start = 0;
	boolean stopped = true;
	boolean resultBlock = false;
	boolean options = false;
	SoundThread soundThread;
	long currPressed = -1;
	long currRelease = -1;
	JTextPane j;
	JScrollPane jsp;
	JScrollBar jsb;
	ArrayList<Pulse> pulses;
	ArrayList<ArrayList<Pulse>> backups;
	String saveText;

	public MainFrame() {
		super("Pulse Listener");
		setBounds(0, 0, 640, 480);
		SoundThread.setup();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		j = new JTextPane();
		j.setText(Options.waitText());
		jsp = new JScrollPane(j);
		pulses = new ArrayList<>();
		backups = new ArrayList<>();
		jsb = jsp.getVerticalScrollBar();
		j.setEditable(false);
		j.setBackground(Color.BLACK);
		j.setForeground(Color.LIGHT_GRAY);
		j.setFont(new Font("courier", Font.BOLD, 15));
		j.addKeyListener(this);
		getContentPane().add(jsp);
	}

	public void stopAndResults() {
		if (Options.MODE == Options.Mode.SUPPORTFRQ) {
			Options.SOUNDRUN = false;
		}
		if (Options.MODE == Options.Mode.PLAYBACKFRQ) {
			Options.SOUNDRUNPB = false;
		}
		ResultEvalAndExport out = new ResultEvalAndExport(pulses, this);
		stopped = true;
		resultBlock = true;
		out.printResults();
		try {
			if (Options.SAVE) {
				out.export();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		resultBlock = false;
		println(Options.waitText(), false);
		Options.MODE = Mode.NORMAL;
	}

	public void resetAndStart() {
		Options.BEATS = 0;
		if (Options.MODE == Options.Mode.SUPPORTFRQ) {
			Options.SOUNDRUN = true;
			soundThread.start();
		}
		if (Options.MODE == Options.Mode.PLAYBACKFRQ) {
			Options.LASTPRESS = 0;
			Options.FIRSTPRESS = 0;
			Options.SOUNDRUNPB = false;
		}
		backups.add(pulses);
		pulses = new ArrayList<>();
		start = 0;
		currPressed = -1;
		currRelease = -1;
	}

	public void showOptions() {
		saveText = j.getText();
		j.setText(Options.OPTIONSTRING);
		options = true;
		Options.optParsedNumber = "";
		Options.ostate = OptionState.ChooseMode;
	}

	public void closeOptions() {
		j.setText(saveText + Options.optToString());
		println(Options.waitText(), false);
		options = false;
		Options.ostate = OptionState.None;
		saveText = "";
		if (Options.MODE == Options.Mode.SUPPORTFRQ) {
			if (!Options.checkSuppFrequency()) {
				println("\n\n<< Frequency has be updated to better match the program: \n >>" + Options.SUPPORTFRQ
						+ " Hz>>", false);
			}
			soundThread = new SoundThread(Options.SUPPORTFRQ);
		}
		if (Options.MODE == Options.Mode.PLAYBACKFRQ) {
			Options.PAUSES = new ArrayList<>();
			soundThread = new SoundThread();
		}
	}

	/**
	 * Adds a pulse to the pulse list
	 * PLAYBACK MODE:
	 * >> Additionally saves pauses between pulses for playback
	 * >> and starts SoundThread if specified Beat Count is reached
	 * 
	 * @param p
	 */
	public void addPulse(Pulse p) {
		pulses.add(p);
		if (pulses.size() > Options.PRECUT) {
			Options.BEATS++;
		}
		if (Options.MODE == Options.Mode.PLAYBACKFRQ) {
			if (Options.FIRSTPRESS == 0) {
				Options.FIRSTPRESS = p.middlePress;
			}
			if (Options.LASTPRESS > 0) {
				Options.PAUSES.add(p.middlePress - Options.LASTPRESS - Options.SOUNDTIME);
			}
			Options.LASTPRESS = p.middlePress;
			if (Options.BEATS == Options.PBBEATS) {
				double silenceMS = (Options.LASTPRESS - Options.FIRSTPRESS) / ((pulses.size() - (Options.PRECUT + 1)));
				System.out.println(silenceMS);
				Options.SOUNDRUNPB = true;
				soundThread.setStartSilence(Math.round(silenceMS) + Options.PBFINETUNING);
				soundThread.start();
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (resultBlock) {
			return;
		}
		if (options) {
			Options.parseOptionEvent(e, this);
			e.consume();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_O && stopped) {
			showOptions();
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !stopped) {
			stopAndResults();
			return;
		}
		if (e.getKeyCode() != Options.PRESSKEY) {
			return;
		}
		if (stopped) {
			stopped = false;
			resetAndStart();
		}
		keyPressedPulse(e);
	}

	public void keyReleased(KeyEvent e) {

		if (e.getKeyCode() != Options.PRESSKEY || options || resultBlock) {
			return;
		}
		keyReleasedPulse(e);
	}

	public void keyTyped(KeyEvent arg0) {
	}

	public void keyPressedPulse(KeyEvent e) {
		long time = e.getWhen();
		if (start == 0) {
			start = time;
		}
		if (currPressed == -1) {
			currPressed = time;
			println("\n######## \nKey Pressed : " + String.valueOf(time), false);
		}
		e.consume();
	}

	public void keyReleasedPulse(KeyEvent e) {
		long time = e.getWhen();
		if (currRelease == -1) {
			currRelease = time;
		}
		if (currRelease != -1 && currPressed != -1) {
			addPulse(new Pulse(currPressed, currRelease));
			currRelease = -1;
			currPressed = -1;
			println("\nKey Released: " + String.valueOf(time) + "\n >> FINISHED BEAT [" + Options.BEATS + "]", false);
		}
		e.consume();
	}

	public void println(String s, boolean erase) {
		if (erase) {
			j.setText(s);
		} else {
			j.setText(j.getText() + s);
		}
		jsb.setValue(jsb.getMaximum());
	}

	// Was wollen wir haben?
	// Programm wird gestartet
	// Leertaste wird das erste Mal gedrückt
	// Danach jedesmal messen wenn Leertaste gedrückt wird
	// Speichern von allen Timestamps (Aufgeteilt in Button Press, Button Release)
	// Nach 40 Sekunden oder so Takt von vornerein wiedergeben (mit Delay etc)
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new MainFrame();
				f.setVisible(true);
			}
		});
	}

}
