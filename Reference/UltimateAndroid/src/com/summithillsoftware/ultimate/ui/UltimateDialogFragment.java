package com.summithillsoftware.ultimate.ui;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import com.summithillsoftware.ultimate.util.SoundPlayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;

public class UltimateDialogFragment extends DialogFragment {
	private Vibrator vibrator;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (vibrator != null) {
			vibrator.cancel();
		}
	}
	
	protected void errorSoundAndVibrate() {
	   	SoundPlayer.current().playErrorSound();
		if (vibrator == null) {
			vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		}
		vibrator.vibrate(500); // 500 milliseconds
	}
	
	protected void displayErrorMessage(String title, String message) {
		AlertDialog errorDialog = new AlertDialog.Builder(this.getActivity()).create();
 		errorDialog.setTitle(title);
 		errorDialog.setMessage(message);
 		errorDialog.setCancelable(false);
 		errorDialog.setButton(BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
			}
 		});

 		errorDialog.show();
	}
	
	protected void displayConfirmDialog(String title, String message, String yesButtonText, String noButtonText, DialogInterface.OnClickListener yesHandler) {
		AlertDialog errorDialog = new AlertDialog.Builder(this.getActivity()).create();
 		errorDialog.setTitle(title);
 		errorDialog.setMessage(message);
 		errorDialog.setCancelable(false);
 		errorDialog.setButton(BUTTON_POSITIVE, yesButtonText, yesHandler);
 		errorDialog.setButton(BUTTON_NEGATIVE, noButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface paramDialogInterface, int buttonPressed) {
			}
 		});
 		errorDialog.show();
	}
	
	protected Size getScreenSize() {
		return ((UltimateActivity)getActivity()).getScreenSize();
	}
}
