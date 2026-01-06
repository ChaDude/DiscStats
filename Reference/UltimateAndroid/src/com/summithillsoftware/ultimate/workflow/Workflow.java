package com.summithillsoftware.ultimate.workflow;

public abstract class Workflow {
	private String workflowId;
	private OnWorkflowChangedListener changeListener;

	public Workflow() {
		super();
		workflowId = java.util.UUID.randomUUID().toString();
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public OnWorkflowChangedListener getChangeListener() {
		return changeListener;
	}

	public void setChangeListener(OnWorkflowChangedListener changeListener) {
		this.changeListener = changeListener;
	}
	
	protected void notifyChange() {
		if (changeListener != null) {
			changeListener.onWorkflowChanged(this);
		}
	}
	
	public abstract void resume();
	
	
	

}
