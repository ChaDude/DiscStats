package com.summithillsoftware.ultimate.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;

public class SoundPlayer {
	private static SoundPlayer Current;
	private int errorSoundId;
	
	private SoundPool soundPool;
	
	static {
		Current = new SoundPlayer();
	}

	public static SoundPlayer current() {
		return Current;
	}
	
	public SoundPlayer() {
		super();
	}
	
	public void loadSounds() {
		initSoundPool();
	}
	
	public void playErrorSound() {
		playSound(errorSoundId);
	}
	
	private SoundPool initSoundPool() {
		if (soundPool == null) {
			soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			errorSoundId = soundPool.load(UltimateApplication.current(), R.raw.error_sound, 1);
		}
		return soundPool;
	}
	
	private SoundPool getSoundPool() {
		if (soundPool == null) {
			initSoundPool();
		}
		return soundPool;
	}
	
	private void playSound(int soundID) {
	    AudioManager audioManager = (AudioManager) UltimateApplication.current().getSystemService(Context.AUDIO_SERVICE);
	    float actualVolume = (float) audioManager
	        .getStreamVolume(AudioManager.STREAM_MUSIC);
	    float maxVolume = (float) audioManager
	        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    float volume = actualVolume / maxVolume;
	    getSoundPool().play(soundID, volume, volume, 1, 0, 1f);
	}

}
