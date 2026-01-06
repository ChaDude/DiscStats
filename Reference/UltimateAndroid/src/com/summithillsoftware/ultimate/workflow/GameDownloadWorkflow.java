package com.summithillsoftware.ultimate.workflow;

import java.util.List;

import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudResponseHandler;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.GameDescription;
import com.summithillsoftware.ultimate.model.Team;

public class GameDownloadWorkflow extends CloudWorkflow {
	private List<GameDescription> gamesAvailable;
	private String gameCloudId;
	private Game downloadedGame;

	public void resume() {
		synchronized (this) {
			switch (getStatus()) {
			case NotStarted:
				checkAppVersion();
				break;
			case VersionCheckCompleteVersionOk:
				// uncomment to force signon
				// CloudClient.current().clearExistingAuthentication();
				retrieveGamesList();
				break;
			case AuthenticationEnded:
				if (gamesAvailable == null) {
					retrieveGamesList();
				} else if (gameCloudId != null) {
					retrieveGame();
				}
				break;		
			case GameChosen:
				retrieveGame();
				break;				
			default:
				break;
			}
		}
	}

	private void retrieveGamesList() {
		gamesAvailable = null;
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.TeamListRetrievalStarted);
		CloudClient.current().submitRetrieveGameDescriptions(Team.current().getCloudId(), new CloudResponseHandler() {
			@SuppressWarnings("unchecked")
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					gamesAvailable = (List<GameDescription>)responseObect;
					setStatus(CloudWorkflowStatus.GameListRetrievalComplete);
				} else if (status == CloudResponseStatus.Unauthorized) {
					setStatus(CloudWorkflowStatus.CredentialsRejected);
				} else {
					setLastErrorStatus(status);
					setStatus(CloudWorkflowStatus.Error);
				}
			}
		});
	}
	
	private void retrieveGame() {
		if (gameCloudId != null) {
			setLastErrorStatus(CloudResponseStatus.Ok);
			setStatus(CloudWorkflowStatus.TeamRetrievalStarted);
			CloudClient.current().submitRetrieveGame(Team.current().getCloudId(), gameCloudId, new CloudResponseHandler() {
				@Override
				public void onResponse(CloudResponseStatus status, Object responseObect) {
					if (status == CloudResponseStatus.Ok) {
						Game downloadedGame = (Game)responseObect;
						saveGame(downloadedGame);
						setStatus(CloudWorkflowStatus.GameRetrievalComplete);
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

	private void saveGame(Game downloadedGame) {
		// refresh game object if current
		boolean isCurrentGame = Game.isCurrentGame(downloadedGame.getGameId());
		if (isCurrentGame) {
			Game.setCurrentGame(null);
		}
		downloadedGame.setUploaded(true);
		downloadedGame.save();
		if (isCurrentGame) {
			Game.setCurrentGame(downloadedGame);
		}
	}

	public List<GameDescription> getGamesAvailable() {
		return gamesAvailable;
	}

	public void setGamesAvailable(List<GameDescription> gamesAvailable) {
		this.gamesAvailable = gamesAvailable;
	}

	public String getGameCloudId() {
		return gameCloudId;
	}

	public void setGameCloudId(String gameCloudId) {
		this.gameCloudId = gameCloudId;
	}

	public Game getDownloadedGame() {
		return downloadedGame;
	}

	public void setDownloadedGame(Game downloadedGame) {
		this.downloadedGame = downloadedGame;
	}



}
