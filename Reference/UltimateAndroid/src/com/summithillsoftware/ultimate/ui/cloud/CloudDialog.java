package com.summithillsoftware.ultimate.ui.cloud;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;
import com.summithillsoftware.ultimate.ui.callout.CalloutTracker;
import com.summithillsoftware.ultimate.util.UltimateLogger;
import com.summithillsoftware.ultimate.workflow.CloudWorkflow;
import com.summithillsoftware.ultimate.workflow.CloudWorkflowStatus;
import com.summithillsoftware.ultimate.workflow.OnWorkflowChangedListener;
import com.summithillsoftware.ultimate.workflow.Workflow;

public abstract class CloudDialog extends UltimateDialogFragment implements OnWorkflowChangedListener {
	private static final String WORKFLOW_ID_ARG = "workflowId";
	private static final String ACCESS_CHECK_PATH = "access-test.jsp";
	private static final String ACCESS_CHECK_URL = CloudClient.SCHEME_HOST + "/" + ACCESS_CHECK_PATH;
	private static final String SIGNON_PAGE_JS_OBJECT_NAME = "UltimateAndroid";
	
	// widgets
	private ViewFlipper viewFlipper;
	protected View introView;
	protected View loadingView;
	protected View signonView;
	protected View selectionView;
	protected WebView webView;
	protected TextView introTextView;
	protected TextView selectionInstructionsLabel;
	protected ListView selectionListView;
	private TextView versionMessageTextView;
	private Button cancelButton;
	private Button continueButton;
	private TextView statusTextView;
	private ProgressBar progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_cloud, container, false);
		connectWidgets(view);
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
	public void onStart() {
		super.onStart();
		populateView();
		registerWidgetListeners();
		getWorkflow().setChangeListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		workflowChanged(getWorkflow());
	}
	
	
	private void connectWidgets(View view) {
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		continueButton  = (Button) view.findViewById(R.id.continueButton);
		viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
		introView = (View) view.findViewById(R.id.introView);
		loadingView = (View) view.findViewById(R.id.loadingView);
		selectionView = (View) view.findViewById(R.id.selectionView);
		signonView = (View) view.findViewById(R.id.signonView);
		webView = (WebView) view.findViewById(R.id.webView); 
		statusTextView = (TextView) view.findViewById(R.id.statusTextView);
		selectionInstructionsLabel = (TextView) view.findViewById(R.id.selectionInstructionsLabel);
		introTextView = (TextView) view.findViewById(R.id.introTextView);
		selectionListView = (ListView) view.findViewById(R.id.selectionListView);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		versionMessageTextView = (TextView) view.findViewById(R.id.versionMessageTextView);
	}

	private void populateView() {

	}

	private void registerWidgetListeners() {
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismissDialog();
			}
		});
		continueButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleUserWantsToSignon();
			}
		});
	}

	private void registerDialogCancelListener(Dialog dialog) {
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				// Nothing yet...just coded in case we want to do something here
				// later
			}
		});
	}
	
	public void setWorkflow(CloudWorkflow workflow) {
		UltimateApplication.current().setActiveWorkflow(workflow);
		Bundle args = new Bundle();
		args.putString(WORKFLOW_ID_ARG, workflow.getWorkflowId());
        setArguments(args);
	}

	private String getWorkflowId() {
		return getArguments().getString(WORKFLOW_ID_ARG);
	}
	
	protected void showIntroView() {
		viewFlipper.setDisplayedChild(0);
	}
	
	protected void showLoadingView() {
		viewFlipper.setDisplayedChild(1);
	}
	
	protected void showSignonView() {
		webView.setVisibility(View.VISIBLE);
		viewFlipper.setDisplayedChild(2);
	}
	
	protected void showSelectionView() {
		viewFlipper.setDisplayedChild(3);
	}
	
	protected void showVersionCheckView() {
		try {
			versionMessageTextView.setText(getWorkflow().getCloudMetaInfo().getMessageToUser());
		} catch (Exception e) {
			UltimateLogger.logError("Unable to retrieve app version check message", e);
		}
		viewFlipper.setDisplayedChild(4);
	}

	protected void dismissDialog() {
		// make sure no crash if rotate while waiting for timer to pop
		try {
			getWorkflow().setStatus(CloudWorkflowStatus.Cancel);
			if (getActivity() instanceof Refreshable) {
				((Refreshable)getActivity()).refresh();
			}
			dismiss();
		} catch (Exception e) {
			UltimateLogger.logWarning( "Error dismissing dialog", e);
		}
	}
	
	protected CloudWorkflow getWorkflow() {
		Workflow workflow = UltimateApplication.current().getActiveWorkflow();
		// verify the work flow didn't change
		if (getWorkflowId() != null && !workflow.getWorkflowId().equals(getWorkflowId())) {
			UltimateLogger.logError( "Workflow invalid...changed since view opened");
			dismiss();
		}
		return (CloudWorkflow)UltimateApplication.current().getActiveWorkflow();
	}

	protected void displayCloudError(CloudResponseStatus status) {
		String title = null;
		String message = null;
		if (status == CloudResponseStatus.NotConnectedToInternet) {
			title = getString(R.string.alert_cloud_not_connected_error_title);
			message = getString(R.string.alert_cloud_not_connected_error_message);
		} else if (status == CloudResponseStatus.Timeout) {
			title = getString(R.string.alert_cloud_timeout_error_title);
			message = getString(R.string.alert_cloud_timeout_error_message);
		} else  {
			title = getString(R.string.alert_cloud_error_title);
			message = getString(R.string.alert_cloud_error_message);
		}
		((UltimateActivity)getActivity()).displayErrorMessage(title, message, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						dismissDialog();
					}
				});
	}
	
	
	protected void setProgressText(int resid) {
		statusTextView.setText(getString(resid));
	}

	@Override
	public void onWorkflowChanged(final Workflow workflow) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				workflowChanged(workflow);
			}
		};
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(runnable);
		}
	}
	
	protected void requestSignon() {
		CloudClient.current().clearExistingAuthentication();
		setProgressText(R.string.label_cloud_waiting_for_signon);
		configureWebView();
		String accessTestUrl = ACCESS_CHECK_URL + "?redirect=true&cache-buster=" + System.currentTimeMillis();
		CookieManager.getInstance().removeSessionCookie();
		webView.loadUrl(accessTestUrl);
	}
	
	private void handleUserSignedOn() {
		webView.setVisibility(View.INVISIBLE);
		captureAuthenticationCookie();
		captureEmailFromSignonPage();
		getWorkflow().setStatus(CloudWorkflowStatus.AuthenticationEnded);
		getWorkflow().resume();
	}
	
	private void handleUserWantsToSignon() {
		setUserHasBeenIntroducedToSignon();
		getWorkflow().setStatus(CloudWorkflowStatus.UserApprovedServerInteraction);
	}
	
	private void captureAuthenticationCookie() {
		CookieSyncManager.getInstance().sync();
		CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(CloudClient.SCHEME_HOST); 
        //UltimateLogger.logInfo( "cookie = " + cookie);
        if (cookie != null) {
        	Preferences.current().setCloudAuthenticationCookie(cookie);
        	Preferences.current().save();
        }
	}
	
	private void captureEmailFromSignonPage() {
		webView.loadUrl("javascript:" + SIGNON_PAGE_JS_OBJECT_NAME + ".setUserEmail(document.getElementById('email').value)");
	}
	
	private boolean isAccessTestPageURL(String urlAsString)  {
		try {
			String pagePath = new URL(urlAsString).getPath();
			if (!pagePath.startsWith("/")) {
				pagePath = "/" + pagePath;
			}
			if (pagePath.equalsIgnoreCase("/" + ACCESS_CHECK_PATH)) {
				return true;
			}
		} catch (MalformedURLException e) {
			UltimateLogger.logError( "Could not read URL during signon", e);
			return false;
		}
		return false;
	}
	
	private boolean isUltimateURL(String urlAsString)  {
		try {
			String host = new URL(urlAsString).getHost();
			return host.contains(CloudClient.HOST);
		} catch (MalformedURLException e) {
			UltimateLogger.logError( "Could not read URL during signon", e);
			return false;
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void configureWebView() {
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new SignonJavascriptInterface(), SIGNON_PAGE_JS_OBJECT_NAME);
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished (WebView view, String url) {
				CloudDialog.this.webViewPageLoadFinished(url);
			}
		});
	}
	
	private void webViewPageLoadFinished(String urlAsString) {
		if (!isUltimateURL(urlAsString)) {
			showSignonView();
		} else if (isAccessTestPageURL(urlAsString)) {
			handleUserSignedOn();
		}
	}
	
	protected void displayCompleteAndThenDismiss(boolean isUpload) {
		Timer timer = new Timer();
		setProgressText(isUpload ? R.string.toast_upload_complete : R.string.toast_download_complete);
		progressBar.setVisibility(View.GONE);
		showLoadingView();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							CloudDialog.this.dismissDialog();
						}
					});
				} catch (Exception e) {
					UltimateLogger.logError("error while trying to dismiss cloud dialog", e);
				}
			}
		};
		timer.schedule(task, 1000);
	}
	
	protected boolean hasUserBeenIntroducedToSignon() {
		return CalloutTracker.current().hasCalloutBeenShown(CalloutTracker.CALLOUT_SIGNON_TO_SERVER);
	}
	
	protected void setUserHasBeenIntroducedToSignon() {
		CalloutTracker.current().setCalloutShown(CalloutTracker.CALLOUT_SIGNON_TO_SERVER);
	}
	
	protected abstract void workflowChanged(final Workflow workflow);

}
