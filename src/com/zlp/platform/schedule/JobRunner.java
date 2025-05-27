package com.zlp.platform.schedule; 
 
import java.util.TimerTask;

public class JobRunner extends TimerTask{

	private JobManager jobManager = null;

	public JobManager getJobManager() {
		return jobManager;
	}

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}
	
	@Override
	public void run() {
		this.jobManager.executeJobThreads(); 
	}

}
