package com.summithillsoftware.ultimate.ui.twitter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.twitter.TwitterClient;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class TwitterSignonDialog extends UltimateDialogFragment {
	// widgets
	private ViewFlipper viewFlipper;
	protected View loadingView;
	protected View signonView;
	protected WebView webView;
	private Button cancelButton;
	private TextView statusTextView;
	private ProgressBar progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_twitter_signon, container,
				false);
		connectWidgets(view);
		registerWidgetListeners();
		return view;
	}

	/** The system calls this only when creating the layout in a dialog. */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setCanceledOnTouchOutside(false);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		registerDialogCancelListener(dialog);
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		requestSignon();
	}

	private void connectWidgets(View view) {
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
		loadingView = (View) view.findViewById(R.id.loadingView);
		signonView = (View) view.findViewById(R.id.signonView);
		webView = (WebView) view.findViewById(R.id.webView); 
		statusTextView = (TextView) view.findViewById(R.id.statusTextView);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}


	private void registerWidgetListeners() {
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismissDialog();
			}
		});
	}

	private void registerDialogCancelListener(Dialog dialog) {
		// catch back button and 
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					((TwitterActivity)getActivity()).signonDismissed();
					return true;
				}
				return false;
			}
		});
	}

	
	private void dismissDialog() {
		try {
			((TwitterActivity)getActivity()).signonDismissed();
			dismiss();
		} catch (Exception e) {
			UltimateLogger.logWarning( "Error dismissing dialog", e);
		}
	}

	private void requestSignon() {
		if (CloudClient.current().isConnected()) {
			setProgressText(R.string.label_twitter_waiting_for_signon);
			showLoadingView();
			configureWebView();
			new TwitterGetAuthenticationUrlAsyncTask().execute();
		} else {
			displayNoConnectivity();
		}
	}
	
	private void requestSignon(String twitterSignonUrl) {
		if (twitterSignonUrl == null) {
			displayTwitterError();
		} else {
			webView.loadUrl(twitterSignonUrl);
		}
	}
	
	private void configureWebView() {
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished (WebView view, String url) {
				if (!isShowSignonView()) {
					showSignonView();
				} else if (TwitterClient.current().isAuthenticationCallbackUrl(url)) {
					if (TwitterClient.current().isAuthenticationCancelledCallbackUrl(url)) {
						dismissDialog();
					} else {
						new TwitterSetCredentialsAsyncTask(url).execute();
					}
				}
			}
		});
	}
	
	private void setProgressText(int resid) {
		statusTextView.setText(getString(resid));
	}
	
	private void showLoadingView() {
		viewFlipper.setDisplayedChild(0);
	}
	
	private boolean isShowSignonView() {
		return viewFlipper.getDisplayedChild() == 1;
	}
	
	private void showSignonView() {
		webView.setVisibility(View.VISIBLE);
		viewFlipper.setDisplayedChild(1);
	}
	
	protected void displayCompleteAndThenDismiss() {
		Timer timer = new Timer();
		setProgressText(R.string.toast_signon_complete);
		progressBar.setVisibility(View.GONE);
		showLoadingView();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run() {
						TwitterSignonDialog.this.dismissDialog();
					}
				});
			}
		};
		timer.schedule(task, 1000);
	}
	
	private void displayTwitterError() {
		displayTwitterError(
				getString(R.string.alert_twitter_signon_error_title),
				getString(R.string.alert_twitter_signon_error_message));
	}
	
	private void displayNoConnectivity() {
		displayTwitterError(
				getString(R.string.alert_cloud_not_connected_error_title),
				getString(R.string.alert_cloud_not_connected_error_message));
	}
	
	private void displayTwitterError(String title, String message) {
		((UltimateActivity)getActivity()).displayErrorMessage(title, message, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						dismissDialog();
					}
				});
	}
	
	private class TwitterGetAuthenticationUrlAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... paramArrayOfParams) {
			return TwitterClient.current().getAuthenticationURL();
		}
		@Override
		protected void onPostExecute(String twitterSignonUrl) {
			requestSignon(twitterSignonUrl);
		}
	}
	
	private class TwitterSetCredentialsAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private String callbackUrl;
		
		public TwitterSetCredentialsAsyncTask(String callbackUrl) {
			super();
			this.callbackUrl = callbackUrl;
		}
		
		@Override
		protected Boolean doInBackground(Void... paramArrayOfParams) {
			return TwitterClient.current().setTwitterCredentialsFromCallbackUrl(callbackUrl);
		}
		@Override
		protected void onPostExecute(Boolean credentialsSaved) {
			if (credentialsSaved) {
				displayCompleteAndThenDismiss();
			} else {
				displayTwitterError();
			}
		}

	}


}
