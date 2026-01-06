package com.summithillsoftware.ultimate.ui.cloud;

import android.app.Activity;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.team.TeamActivity;
import com.summithillsoftware.ultimate.workflow.CloudWorkflowStatus;
import com.summithillsoftware.ultimate.workflow.TeamUploadWorkflow;
import com.summithillsoftware.ultimate.workflow.Workflow;

public class CloudTeamUploadDialog extends CloudDialog {
	boolean uploadFinished = false;

	protected void workflowChanged(final Workflow workflow) {
		TeamUploadWorkflow teamUploadWorkflow = (TeamUploadWorkflow) workflow;
		switch (teamUploadWorkflow.getStatus()) {
		case NotStarted:
			showLoadingView();
			teamUploadWorkflow.resume();
			break;
		case VersionCheckCompleteVersionUnacceptable:
			showVersionCheckView();
			break;
		case VersionCheckCompleteVersionOk:
			if (hasUserBeenIntroducedToSignon()) {
				showLoadingView();
				teamUploadWorkflow.resume();
			} else {
				showIntroView();
			}
			break;
		case UserApprovedServerInteraction:
			showLoadingView();
			teamUploadWorkflow.resume();
			break;
		case CredentialsRejected:
			requestSignon();
			break;
		case TeamUploadStarted:
			setProgressText(R.string.label_cloud_uploading_team);
			showLoadingView();
			break;
		case TeamUploadComplete:
			uploadFinished = true;
			displayCompleteAndThenDismiss(true);
			break;
		case Error:
			teamUploadWorkflow.setStatus(CloudWorkflowStatus.Cancel);
			displayCloudError(teamUploadWorkflow.getLastErrorStatus());
			break;
		case Cancel:
			dismissDialog();
			break;
		default:
			break;
		}
	}

	@Override
	protected void showIntroView() {
		introTextView
				.setText(getString(R.string.label_cloud_introduction_team_upload));
		super.showIntroView();
	}

	@Override
	protected void dismissDialog() {
		Activity activity = getActivity();
		super.dismissDialog();
		if (uploadFinished) {
			if (activity instanceof TeamActivity) {
				((TeamActivity) activity).teamWasUploaded();
			}
		}
	}

}
