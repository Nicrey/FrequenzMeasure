package input_pulse_measuring;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JTextPane;

public class Options {

	////////////////////// MODES //////////////////////
	enum Mode {
		NORMAL, SUPPORTFRQ, PLAYBACKFRQ
	}

	enum OptionState {
		None, ChooseMode, ChooseSupportFrq, ChoosePBBeats, ChoosePBFineTuning, ChooseFurtherOptions, ChangeExport,
		ChangeSave, ChangeKey, ChangePreCutBeats
	};

	public static int PRESSKEY = KeyEvent.VK_NUMPAD0;
	public static Mode MODE = Mode.NORMAL;
	public static Double TARGETFRQ = 1.0;
	///////////////////// Config ///////////////////////
	public static String optParsedNumber = "";
	public static OptionState ostate = OptionState.None;
	////////////////////// DEFAULTS //////////////////////////
	public static final double SUPPORTFRQ_DFL = 1.0;
	public static final int PBBEATS_DFL = 20;
	public static final int PBFINETUNING_DFL = 0;
	////////////////////// SUPPORTFRQ MODE //////////////////////
	// Options
	public static Double SUPPORTFRQ = 1.0;
	// Runtime
	public static boolean SOUNDRUN = false;

	///////////////////// PLAYBACK MODE ////////////////////////
	// Options
	public static int PBBEATS = 20;
	public static int PBFINETUNING = 0;
	// Runtime
	public static ArrayList<Long> PAUSES;
	public static int BEATS;
	public static long LASTPRESS;
	public static long FIRSTPRESS = 0;
	public static boolean SOUNDRUNPB = false;
	public static int SOUNDTIME = 20;

	////////////////////// EXPORT //////////////////////
	public static String EXPORTPATH = "Results";
	public static boolean SAVE = true;
	public static int PRECUT = 2;

	////////////////////// STRINGS //////////////////////
	public static final String SEPARATOR = "\n~~~~~~~~~~~~~~~~~~~~";
	public static final String OPTIONSTRING = SEPARATOR + "\nOptions Menu: [Further Options: Press [F]]"
			+ "\nChoose Mode: \n >> Normal[1] \n >> Supportive Frequency[2] \n >> Playback Frequency[3]\n";
	public static final String SUPPORTFRQTEXT = SEPARATOR
			+ "\nINPUT SUPPORT FREQUENCY (in HZ) TO BE PLAYED:\n(Confirm with Enter)\n >> Frequency: ";
	public static final String PBBEATSTEXT = SEPARATOR
			+ "\nINPUT PLAYBACK DELAY (in beats):\n(Confirm with Enter)\n >> Delay: ";
	public static final String CHANGESAVINGSTR = SEPARATOR + "\nSAVING FILES: \n >> Yes [1] \n >> No[0]";
	public static final String EXPORTCHANGESTR = SEPARATOR
			+ "\nINPUT NEW EXPORT FOLDER:\n(Confirm with Enter)\n >> Folder: ";;
	public static final String CHANGEKEYSTR = SEPARATOR + "\nCHANGE KEY TO BE PRESSED: [Press Any Key] \n";
	public static final String INVALIDKEYSTR = "\n INVALID KEY, PLEASE ENTER ANOTHER KEY \n";
	public static final String PBFINETUNINGSTR = SEPARATOR + "\nInput finetuning in MS with: \n >> ";
	public static final String PRECUTSTR = SEPARATOR + "\nInput Beats to Cut at Start: \n >> ";

	public static String furtherOptionStr() {
		return SEPARATOR + "\nFURTHER OPTIONS: " + "\n >> Change Result Folder [R]: " + EXPORTPATH
				+ "\n >> Change Save Status [S]: " + SAVE + "\n >> Change Press Key [K] : "
				+ KeyEvent.getKeyText(PRESSKEY) + "\n >> Change Cut Beats at Start [C]: " + PRECUT + "\n>> Back [B]";
	}

	public static String waitText() {
		return "\n\n >> PRESS [" + KeyEvent.getKeyText(PRESSKEY)
				+ "] TO START \n >> [ESC] TO END EXPERIMENT AND START EVALUATION \n >> [O] FOR OPTIONS";
	}

	public static String changedSavingStr() {
		return SEPARATOR + "\nCHANGED SAVING OPTION TO: \n >> " + (SAVE ? "YES" : "NO");
	}

	private static String changedPreCutStr() {
		return SEPARATOR + "\nPRECUT BEATS CHANGED TO: \n >> " + PRECUT;
	}

	public static String changedExportStr() {
		return SEPARATOR + "\nEXPORT FOLDER CHANGED TO:\n >> " + EXPORTPATH;
	}

	public static String changedKeyStr() {
		return SEPARATOR + "\nPRESS KEY CHANGED TO: \n >> " + KeyEvent.getKeyText(PRESSKEY);
	}

	public static String optToString() {
		String s = "\n-------------------------\nOptions Chosen: ";
		s += "\n>> Mode: " + getModeString();
		if (MODE == Mode.SUPPORTFRQ) {
			s += "\n	>> Support Frequency: " + SUPPORTFRQ + " Hz";
		}
		if (MODE == Mode.PLAYBACKFRQ) {
			s += "\n	>> Playback Delay: " + PBBEATS + " beats" + "\n	>> Fine Tuning: " + PBFINETUNING + "ms";
		}
		s += "\n---------------------------";
		return s;
	}

	public static String getModeString() {
		if (MODE == Mode.NORMAL) {
			return "NORMAL";
		}
		if (MODE == Mode.SUPPORTFRQ) {
			return "SUPPORT FREQUENCY";
		}
		if (MODE == Mode.PLAYBACKFRQ) {
			return "PLAYBACK FREQUENCY";
		}
		return "";
	}

	public static boolean checkSuppFrequency() {
		if (SUPPORTFRQ > 50) {
			SUPPORTFRQ = 50.0;
			return false;
		}
		int ms = (int) Math.round(1000 / SUPPORTFRQ);

		if (SUPPORTFRQ != 1000 / (double) ms) {
			SUPPORTFRQ = (1000 / (double) ms);
			return false;
		}
		return true;
	}

	public static void parseOptionEvent(KeyEvent e, MainFrame m) {
		if (ostate == OptionState.ChooseMode) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_1:
				Options.MODE = Options.Mode.NORMAL;
				m.closeOptions();
				break;
			case KeyEvent.VK_2:
				Options.MODE = Options.Mode.SUPPORTFRQ;
				ostate = OptionState.ChooseSupportFrq;
				m.println(Options.SUPPORTFRQTEXT, true);
				setDefaultParse(String.valueOf(Options.SUPPORTFRQ), m.j);
				break;
			case KeyEvent.VK_3:
				Options.MODE = Options.Mode.PLAYBACKFRQ;
				ostate = OptionState.ChoosePBBeats;
				m.println(Options.PBBEATSTEXT, true);
				setDefaultParse(String.valueOf(Options.PBBEATS), m.j);
				break;
			case KeyEvent.VK_F:
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), true);
				break;
			default:
				return;
			}
			return;
		}
		if (ostate == OptionState.ChooseSupportFrq) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !optParsedNumber.isEmpty()) {
				Options.SUPPORTFRQ = Double.valueOf(optParsedNumber);
				optParsedNumber = "";
				m.closeOptions();
				return;
			}
			parseDouble(e, m.j);
		}
		if (ostate == OptionState.ChoosePBBeats) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !optParsedNumber.isEmpty()) {
				Options.PBBEATS = Integer.valueOf(optParsedNumber);
				optParsedNumber = "";
				ostate = OptionState.ChoosePBFineTuning;
				m.println(Options.PBFINETUNINGSTR, false);
				setDefaultParse(String.valueOf(Options.PBFINETUNING), m.j);
				return;
			}
			parseInteger(e, m.j);
		}
		if (ostate == OptionState.ChoosePBFineTuning) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !optParsedNumber.isEmpty()) {
				Options.PBFINETUNING = Integer.valueOf(optParsedNumber);
				optParsedNumber = "";
				m.closeOptions();
				return;
			}
			parseSignedInteger(e, m.j);
		}

		if (ostate == OptionState.ChooseFurtherOptions) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				ostate = OptionState.ChooseMode;
				m.println(Options.OPTIONSTRING, true);
				break;
			case KeyEvent.VK_R:
				ostate = OptionState.ChangeExport;
				m.println(Options.EXPORTCHANGESTR, true);
				setDefaultParse(Options.EXPORTPATH, m.j);
				break;
			case KeyEvent.VK_S:
				ostate = OptionState.ChangeSave;
				m.println(Options.CHANGESAVINGSTR, true);
				break;
			case KeyEvent.VK_K:
				ostate = OptionState.ChangeKey;
				m.println(Options.CHANGEKEYSTR, true);
			case KeyEvent.VK_C:
				ostate = OptionState.ChangePreCutBeats;
				m.println(Options.PRECUTSTR, true);
			default:
				break;
			}
			return;
		}
		if (ostate == OptionState.ChangePreCutBeats) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && optParsedNumber != "") {
				Options.PRECUT = Integer.valueOf(optParsedNumber);
				m.println(Options.changedPreCutStr(), true);
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), false);
				optParsedNumber = "";
				return;
			}
			parseInteger(e, m.j);
		}
		if (ostate == OptionState.ChangeKey) {
			if (!(e.getKeyCode() == KeyEvent.VK_ESCAPE) && !(e.getKeyCode() == KeyEvent.VK_O)
					&& !(e.getKeyCode() == KeyEvent.VK_ENTER)) {
				Options.PRESSKEY = e.getKeyCode();
				m.println(Options.changedKeyStr(), true);
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), false);
				return;
			} else {
				m.println(Options.INVALIDKEYSTR, false);
			}

		}
		if (ostate == OptionState.ChangeSave) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_0:
				Options.SAVE = false;
				m.println(Options.changedSavingStr(), true);
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), false);
				break;
			case KeyEvent.VK_1:
				Options.SAVE = true;
				m.println(Options.changedSavingStr(), true);
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), false);
				break;
			default:
				break;
			}
			return;
		}
		if (ostate == OptionState.ChangeExport) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && optParsedNumber != "") {
				Options.EXPORTPATH = optParsedNumber;
				m.println(Options.changedExportStr(), true);
				ostate = OptionState.ChooseFurtherOptions;
				m.println(Options.furtherOptionStr(), false);
				optParsedNumber = "";
				return;
			}
			parseDirectoryString(e, m.j);
		}
	}

	public static void parseBackspace(KeyEvent e, JTextPane j) {
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !optParsedNumber.isEmpty()) {
			optParsedNumber = optParsedNumber.substring(0, optParsedNumber.length() - 1);
			j.setText(j.getText().substring(0, j.getText().length() - 1));
		}
	}

	public static void setDefaultParse(String s, JTextPane j) {
		optParsedNumber = s;
		j.setText(j.getText() + s);
	}

	public static void parseDirectoryString(KeyEvent e, JTextPane j) {
		char c = e.getKeyChar();
		if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-' || c == '#') {
			optParsedNumber += String.valueOf(c);
			j.setText(j.getText() + c);
		}
		parseBackspace(e, j);
	}

	public static void parseDouble(KeyEvent e, JTextPane j) {
		char c = e.getKeyChar();
		if (((c >= '0') && (c <= '9')) || (c == '.')) {
			if (c == '.' && (optParsedNumber.contains(".") || optParsedNumber.isEmpty())) {
				return;
			}
			optParsedNumber += String.valueOf(c);
			j.setText(j.getText() + c);
		}
		parseBackspace(e, j);
	}

	public static void parseSignedInteger(KeyEvent e, JTextPane j) {
		char c = e.getKeyChar();
		if (((c >= '0') && (c <= '9')) || (optParsedNumber.isEmpty() && (c == '-' || c == '+'))) {
			optParsedNumber += String.valueOf(c);
			j.setText(j.getText() + c);
		}
		parseBackspace(e, j);
	}

	public static void parseInteger(KeyEvent e, JTextPane j) {
		char c = e.getKeyChar();
		if (((c >= '0') && (c <= '9'))) {
			optParsedNumber += String.valueOf(c);
			j.setText(j.getText() + c);
		}
		parseBackspace(e, j);
	}
}
