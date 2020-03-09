package input_pulse_measuring;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundThread extends Thread {

	public static float SAMPLE_RATE = 2000;
	public static float SAMPLES_PER_MS = SAMPLE_RATE / 1000;

	public static int soundsize = (int) (Options.SOUNDTIME * SAMPLES_PER_MS);
	public static int hz = 1200;
	public static double vol = 1.0;

	public static byte[] soundBuf = new byte[soundsize];
	public static final double factor = 1 / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
	public static byte[] silBuf = new byte[1];
	public static byte[] msSilBuf = new byte[(int) SAMPLES_PER_MS];

	public static AudioFormat af = new AudioFormat(SAMPLE_RATE, // sampleRate
			8, // sampleSizeInBits
			1, // channels
			true, // signed
			false); // bigEndian

	private byte[] pulseBuf;
	private SourceDataLine sdl;
	private int pulseLength;

	// PLAYBACK MODE
	private int pauseIdx;
	private long silence;
	private int ms;

	public static void setup() {
		silBuf[0] = (byte) 0;
		for (int i = 0; i < SAMPLES_PER_MS; i++) {
			msSilBuf[i] = silBuf[0];
		}
		for (int i = 0; i < soundsize; i++) {
			double angle = i * factor;
			soundBuf[i] = (byte) (Math.sin(angle) * 127.0 * vol * 0.1);
		}
	}

	public SoundThread(double frequency) {
		int ms = (int) Math.round(1000 / frequency);
		this.pulseLength = (int) (ms * SAMPLES_PER_MS);
		System.out.println(ms);
		System.out.println(pulseLength);
		System.out.println(frequency);
		pulseBuf = new byte[pulseLength];
		for (int i = 0; i < pulseBuf.length; i++) {
			if (i < soundBuf.length) {
				pulseBuf[i] = soundBuf[i];
			} else {
				pulseBuf[i] = silBuf[0];
			}
		}
		try {
			sdl = AudioSystem.getSourceDataLine(af);
			sdl.open(af);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public SoundThread() {
		pauseIdx = 0;
		ms = 0;
		try {
			sdl = AudioSystem.getSourceDataLine(af);
			sdl.open(af);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void setStartSilence(long start) {
		this.silence = start;
	}

	public void run() {
		sdl.start();
		int offset = 0;
		if (Options.Mode.SUPPORTFRQ == Options.MODE) {
			while (Options.SOUNDRUN) {
				int length = (int) Math.min(sdl.available(), pulseLength - offset);
				sdl.write(pulseBuf, offset, length);
				offset = (offset + length) % (int) pulseLength;
			}
		} else {
			while (Options.SOUNDRUNPB) {
				if (ms < silence) {
					if (sdl.available() >= SAMPLES_PER_MS) {
						sdl.write(msSilBuf, 0, (int) SAMPLES_PER_MS);
						ms++;
					}
				} else if (pauseIdx < Options.PAUSES.size()) {
					if (sdl.available() >= SAMPLES_PER_MS) {
						sdl.write(soundBuf, 0, soundsize);
						ms = 0;
						silence = Options.PAUSES.get(pauseIdx);
						pauseIdx++;
					}

				}
			}
		}

		sdl.drain();
		sdl.stop();
		sdl.close();
	}
}
