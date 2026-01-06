package com.summithillsoftware.ultimate.ui.cloud;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.workflow.CloudWorkflowStatus;
import com.summithillsoftware.ultimate.workflow.TeamDownloadWorkflow;
import com.summithillsoftware.ultimate.workflow.Workflow;

public class CloudTeamDownloadDialog extends CloudDialog {
	
	protected void workflowChanged(final Workflow workflow) {
		TeamDownloadWorkflow teamDownloadWorkflow = (TeamDownloadWorkflow)workflow;
		switch (teamDownloadWorkflow.getStatus()) {
		case NotStarted:
			showLoadingView();
			teamDownloadWorkflow.resume();
			break;
		case VersionCheckCompleteVersionUnacceptable:
			showVersionCheckView();
			break;
		case VersionCheckCompleteVersionOk:
			if (hasUserBeenIntroducedToSignon()) {
				showLoadingView();
				teamDownloadWorkflow.resume();
			} else {
				showIntroView();
			}
			break;
		case UserApprovedServerInteraction:
			showLoadingView();
			teamDownloadWorkflow.resume();
			break;
		case CredentialsRejected:
			requestSignon();
			break;	
		case AuthenticationEnded:
			showLoadingView();
			break;				
		case TeamListRetrievalStarted:
			setProgressText(R.string.label_cloud_downloading_teams);
			showLoadingView();
			break;	
		case TeamListRetrievalComplete:
			requestTeamSelection();
			break;				
		case TeamRetrievalStarted:
			setProgressText(R.string.label_cloud_downloading_team);
			showLoadingView();
			break;		
		case TeamRetrievalComplete:
			displayCompleteAndThenDismiss(false);
			break;				
		case Error:
			displayCloudError(teamDownloadWorkflow.getLastErrorStatus());
			break;				
		case Cancel:
			dismissDialog();			
		default:
			break;
		}
	}
	
	private void requestTeamSelection() {
		selectionInstructionsLabel.setText(R.string.label_cloud_download_team_selection_instructions);
		CloudTeamsSelectionListAdaptor adaptor = new CloudTeamsSelectionListAdaptor(this.getActivity());
		selectionListView.setAdapter(adaptor);
		registerTeamSelectedListener();
		showSelectionView();
	}
	
	private void handleTeamSelection(Team team) {
		TeamDownloadWorkflow workflow = (TeamDownloadWorkflow)getWorkflow();
		workflow.setTeamCloudId(team.getCloudId());
		workflow.setStatus(CloudWorkflowStatus.TeamChosen);
		workflow.resume();
	}
	
	private void registerTeamSelectedListener() {
		selectionListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Team selectedTeam = (Team)selectionListView.getAdapter().getItem(position);
				handleTeamSelection(selectedTeam);
			}

		});
	}
	
	@Override
	protected void showIntroView() {
		introTextView.setText(getString(R.string.label_cloud_introduction_team_download));
		super.showIntroView();
	}

}
