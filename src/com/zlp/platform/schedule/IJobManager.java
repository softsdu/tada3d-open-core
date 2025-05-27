package com.zlp.platform.schedule;
 
import java.sql.SQLException;  
import java.util.Date; 
import java.util.List; 
import org.hibernate.Session;  

public interface IJobManager{
	boolean getRunning();

	String createJob( Session dbSession, String name, String code, String runExp, String runParameters, Date planTime, int maxRetryTime, int hasRetryTime, int retryDelay, String userId, JobType jobType);
   
	void deleteWaitingJobInstance(Session dbSession, String jobId) throws SQLException;

	boolean checkCanRun(Session dbSession, String jobId) throws SQLException;

	void createJobInstance(Session dbSession, String jobId) throws Exception;

	void updateJobInstance( Session dbSession, String jobInstanceId, StatusType statusType, String note) throws Exception;
	
	List<String> checkTimeConfig(String timeConfig) throws Exception;
	
	String getTimeConfig(Session dbSession, String jobId) throws Exception;
}
