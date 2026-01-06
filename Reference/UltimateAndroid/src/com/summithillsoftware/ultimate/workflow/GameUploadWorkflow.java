package com.summithillsoftware.ultimate.workflow;

import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudResponseHandler;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.Team;

public class GameUploadWorkflow extends CloudWorkflow {
	String cloudId = null;
	
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
				if (cloudId == null) {
					uploadCurrentTeam();
				} else {
					uploadCurrentGame();
				};	
				break;
			case TeamUploadComplete:
				uploadCurrentGame();
				break;
			default:
				break;
			}
		}
	}
	
	private void uploadCurrentTeam() {
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.TeamUploadStarted);
		CloudClient.current().submitUploadTeam(Team.current(), new CloudResponseHandler() {
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					saveCloudId((String)responseObect);
					setStatus(CloudWorkflowStatus.TeamUploadComplete);
					resume();
				} else if (status == CloudResponseStatus.Unauthorized) {
					setStatus(CloudWorkflowStatus.CredentialsRejected);
				} else {
					setLastErrorStatus(status);					
					setStatus(CloudWorkflowStatus.Error);
				}
			}
		});
	}
	

	private void uploadCurrentGame() {
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.GameUploadStarted);
		CloudClient.current().submitUploadGame(Game.current(), Team.current(), new CloudResponseHandler() {
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					setStatus(CloudWorkflowStatus.GameUploadComplete);
					Game.current().setUploaded(true);
					Game.current().save();
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
		this.cloudId = cloudId;
		Team.current().setCloudId(cloudId);
		Team.current().save();
	}

}
