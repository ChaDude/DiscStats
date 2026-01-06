package com.summithillsoftware.ultimate.workflow;

import java.util.List;

import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudResponseHandler;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;
import com.summithillsoftware.ultimate.model.Team;

public class TeamDownloadWorkflow extends CloudWorkflow {
	private List<Team> teamsAvailable;
	private String teamCloudId;
	private Team downloadedTeam;

	public void resume() {
		synchronized (this) {
			switch (getStatus()) {
			case NotStarted:
				checkAppVersion();
				break;
			case VersionCheckCompleteVersionOk:
				// uncomment to force signon
				// CloudClient.current().clearExistingAuthentication();
				retrieveTeamsList();
				break;	
			case UserApprovedServerInteraction:
				retrieveTeamsList();
				break;				
			case AuthenticationEnded:
				if (teamsAvailable == null) {
					retrieveTeamsList();
				} else if (teamCloudId != null) {
					retrieveTeam();
				}
				break;		
			case TeamChosen:
				retrieveTeam();
				break;				
			default:
				break;
			}
		}
	}

	private void retrieveTeamsList() {
		teamsAvailable = null;
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.TeamListRetrievalStarted);
		CloudClient.current().submitRetrieveTeams(new CloudResponseHandler() {
			@SuppressWarnings("unchecked")
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					teamsAvailable = (List<Team>)responseObect;
					setStatus(CloudWorkflowStatus.TeamListRetrievalComplete);
				} else if (status == CloudResponseStatus.Unauthorized) {
					setStatus(CloudWorkflowStatus.CredentialsRejected);
				} else {
					setLastErrorStatus(status);					
					setStatus(CloudWorkflowStatus.Error);
				}
			}
		});
	}
	
	private void retrieveTeam() {
		if (teamCloudId != null) {
			setLastErrorStatus(CloudResponseStatus.Ok);
			setStatus(CloudWorkflowStatus.TeamRetrievalStarted);
			CloudClient.current().submitRetrieveTeam(teamCloudId, new CloudResponseHandler() {
				@Override
				public void onResponse(CloudResponseStatus status, Object responseObect) {
					if (status == CloudResponseStatus.Ok) {
						Team downloadedTeam = (Team)responseObect;
						saveTeam(downloadedTeam);
						setStatus(CloudWorkflowStatus.TeamRetrievalComplete);
					} else if (status == CloudResponseStatus.Unauthorized) {
						setStatus(CloudWorkflowStatus.CredentialsRejected);
					} else {
						setLastErrorStatus(status);						
						setStatus(CloudWorkflowStatus.Error);
					}
				}
			});
		}
	}

	public List<Team> getTeamsAvailable() {
		return teamsAvailable;
	}

	public void setTeamsAvailable(List<Team> teamsAvailable) {
		this.teamsAvailable = teamsAvailable;
	}

	public Team getDownloadedTeam() {
		return downloadedTeam;
	}

	public String getTeamCloudId() {
		return teamCloudId;
	}

	public void setTeamCloudId(String teamCloudId) {
		this.teamCloudId = teamCloudId;
	}
	
	private void saveTeam(Team downloadedTeam) {
		// refresh team object if current
		boolean isCurrentTeam = Team.isCurrentTeam(downloadedTeam.getCloudId());
		if (isCurrentTeam) {
			Team.setCurrentTeam(null);
		}
		downloadedTeam.ensureValid();
		downloadedTeam.save();
		if (isCurrentTeam) {
			Team.setCurrentTeam(downloadedTeam);
		}
		Team.removeDefaultTeamIfNoLongerRequired();
	}



}
