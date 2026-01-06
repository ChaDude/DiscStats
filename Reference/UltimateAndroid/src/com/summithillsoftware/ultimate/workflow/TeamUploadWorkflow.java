package com.summithillsoftware.ultimate.workflow;

import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudResponseHandler;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;
import com.summithillsoftware.ultimate.model.Team;

public class TeamUploadWorkflow extends CloudWorkflow {

	public void resume() {
		synchronized (this) {
			switch (getStatus()) {
			case NotStarted:
				checkAppVersion();
				break;
			case VersionCheckCompleteVersionOk:
				// uncomment to force signon
				// CloudClient.current().clearExistingAuthentication();
				uploadCurrentTeam();
				break;				
			case UserApprovedServerInteraction:
				uploadCurrentTeam();
				break;				
			case AuthenticationEnded:
				uploadCurrentTeam();
				break;		
			default:
				break;
			}
		}
	}

	private void uploadCurrentTeam() {
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.TeamUploadStarted);
		Team.current().ensureValid();
		CloudClient.current().submitUploadTeam(Team.current(), new CloudResponseHandler() {
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					String cloudId = (String)responseObect;
					setStatus(CloudWorkflowStatus.TeamUploadComplete);
					saveCloudId(cloudId);
				} else if (status == CloudResponseStatus.Unauthorized) {
					setStatus(CloudWorkflowStatus.CredentialsRejected);
				} else {
					setLastErrorStatus(status);					
					setStatus(CloudWorkflowStatus.Error);
				}
			}
		});
	}
	
	private void saveCloudId(String cloudId) {
		Team.current().setCloudId(cloudId);
		Team.current().save();
	}



}
