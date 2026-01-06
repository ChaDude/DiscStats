package com.summithillsoftware.ultimate.workflow;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.cloud.CloudMetaInfo;
import com.summithillsoftware.ultimate.cloud.CloudResponseHandler;
import com.summithillsoftware.ultimate.cloud.CloudResponseStatus;

public abstract class CloudWorkflow extends Workflow {
	private CloudWorkflowStatus status = CloudWorkflowStatus.NotStarted;
	private CloudResponseStatus lastErrorStatus;
	private CloudMetaInfo cloudMetaInfo;

	public CloudWorkflowStatus getStatus() {
		return status;
	}

	public void setStatus(CloudWorkflowStatus status) {
		CloudWorkflowStatus oldStatus = this.status;
		this.status = status;
		if (oldStatus != this.status) {
			notifyChange();
		}
	}
	
	public CloudResponseStatus getLastErrorStatus() {
		return lastErrorStatus;
	}

	public void setLastErrorStatus(CloudResponseStatus lastErrorStatus) {
		this.lastErrorStatus = lastErrorStatus;
	}
	
	public CloudMetaInfo getCloudMetaInfo() {
		return cloudMetaInfo;
	}
	
	public void checkAppVersion() {
		setLastErrorStatus(CloudResponseStatus.Ok);
		setStatus(CloudWorkflowStatus.VersionCheckStarted);
		String appVersion = UltimateApplication.current().getAppVersion();
		CloudClient.current().submitVersionCheck(appVersion, new CloudResponseHandler() {
			@Override
			public void onResponse(CloudResponseStatus status, Object responseObect) {
				if (status == CloudResponseStatus.Ok) {
					cloudMetaInfo = (CloudMetaInfo)responseObect;
					if (cloudMetaInfo.isAppVersionAcceptable()) {
						setStatus(CloudWorkflowStatus.VersionCheckCompleteVersionOk);
					} else {
						setStatus(CloudWorkflowStatus.VersionCheckCompleteVersionUnacceptable);
					}
				} else {
					setLastErrorStatus(status);						
					setStatus(CloudWorkflowStatus.Error);
				}
			}
		});
	}



}
