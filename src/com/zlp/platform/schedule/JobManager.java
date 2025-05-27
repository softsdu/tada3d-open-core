package com.zlp.platform.schedule;
 
import java.math.BigInteger; 
import java.sql.SQLException; 
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List; 

import org.apache.log4j.Logger;
import org.hibernate.Session; 
import com.zlp.platform.common.SysConfig; 
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess; 
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor; 

//任务管理器
public class JobManager implements IJobManager, InitializingBean{

	private static Logger logger=Logger.getLogger(JobManager.class);

	private ThreadPoolTaskExecutor jobThreadPool = null;
	public ThreadPoolTaskExecutor getJobThreadPool() {
		return jobThreadPool;
	}
	public void setJobThreadPool(ThreadPoolTaskExecutor jobThreadPool) {
		this.jobThreadPool = jobThreadPool;
	}
	
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess ;
	}  

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	private boolean running = false;
	public void setRunning(boolean running){
		this.running = running;		
	}
	public boolean getRunning(){
		return this.running;
	}     

	private boolean autoStart = true;
	public void setAutoStart(boolean autoStart){
		this.autoStart = autoStart;		
	}
	public boolean getAutoStart(){
		return this.autoStart;
	}     
	
	public void afterPropertiesSet() throws Exception  
    {  
		if(this.getAutoStart()){
			//重新计算所有定时任务下次执行时间，即将原有的jobInstance作废，创建新的jobInstance
			this.renewAllJobInstance(); 
		} 
    }  
	
	public void run() throws Exception{ 
		this.renewAllJobInstance(); 
	}
	
	public void stop(){
		this.setRunning(false);
	}
	
	public void renewAllJobInstance() throws Exception{
		this.setRunning(false);
		Session dbSession = null;
		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			List<String> allJobIds = this.getAllJobIds(dbSession);
			for(String jobId : allJobIds){
				this.deleteWaitingJobInstance(dbSession, jobId);
				this.createJobInstance(dbSession, jobId);
			}
			this.setRunning(true);
		}
		catch(Exception ex){
			throw new Exception("RenewAllJobInstance error.", ex);
		}
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}
	
	public String createJob( Session dbSession, String name, String code, String runExp, String runParameters, Date planTime, int maxRetryTime, int hasRetryTime, int retryDelay, String userId, JobType jobType){
		//创建skd_job记录
		Data jobData = DataCollection.getData("skd_Job");
		HashMap<String, Object> jobFieldValues = new HashMap<String, Object>();
		jobFieldValues.put("name", name);
		jobFieldValues.put("code", code);
		jobFieldValues.put("runexp", runExp);
		jobFieldValues.put("runparameters", runParameters);
		jobFieldValues.put("plantime", planTime);
		jobFieldValues.put("maxretrytime", maxRetryTime);
		jobFieldValues.put("hasretrytime", hasRetryTime);
		jobFieldValues.put("retrydelay", retryDelay);
		jobFieldValues.put("userid", userId);
		jobFieldValues.put("jobtype", jobType.toString());	
		jobFieldValues.put("statusname", StatusType.waiting);	
		jobFieldValues.put("createtime", new Date());		
		jobFieldValues.put("isdeleted", "N");		
		String jobId = this.dBParserAccess.insertByData(dbSession, jobData, jobFieldValues); 
		return jobId;
	}
	
	//停用正在waiting的任务
	public void deleteWaitingJobInstance(Session dbSession, String jobId){
		String updateJobInstanceSql = "update skd_jobinstance set statusname = 'deleted' where jobid = " + SysConfig.getParamPrefix() + "jobid and statusname = 'waiting'";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("jobid", jobId);
		this.dBParserAccess.update(dbSession, updateJobInstanceSql, p2vs); 
	}	

	//getTimeConfig
	public String getTimeConfig(Session dbSession, String jobId) throws Exception{
		//获取timeConfig
		String selectJobSql = "select sj.id as id, sj.timeconfig as timeconfig from skd_job sj where id = " + SysConfig.getParamPrefix() + "jobid";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("jobid", jobId);
		List<String> alias = new ArrayList<String>();
		alias.add("id");
		alias.add("timeconfig");
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("timeconfig", ValueType.String);
		
		DataTable jobTable = this.dBParserAccess.selectList(dbSession, selectJobSql, p2vs, alias, fieldValueTypes);
		String timeconfig = jobTable.getRows().get(0).getStringValue("timeconfig");		
		return timeconfig;
	}	

	//getAllJobs
	public List<String> getAllJobIds(Session dbSession) throws Exception{
		//获取timeConfig
		String selectJobSql = "select sj.id as id from skd_job sj where isdeleted = 'N' and isactive = 'Y'";
		List<String> alias = new ArrayList<String>();
		alias.add("id"); 
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("id", ValueType.String); 
		
		DataTable jobTable = this.dBParserAccess.selectList(dbSession, selectJobSql, null, alias, fieldValueTypes);
		List<DataRow> jobRows = jobTable.getRows();
		List<String> ids = new ArrayList<String>();
		for(DataRow jobRow : jobRows){
			ids.add(jobRow.getStringValue("id"));
		}
		return ids;
	}	

	//创建skd_jobinstance记录
	public void createJobInstance(Session dbSession, String jobId) throws Exception{
		
		String timeconfig = this.getTimeConfig(dbSession, jobId);
		Date currentTime = new Date();
		Date planTime = this.getNextRunTime(timeconfig); 
		Data jobInstanceData = DataCollection.getData("skd_JobInstance");
		HashMap<String, Object> jobInstanceFieldValues = new HashMap<String, Object>();
		jobInstanceFieldValues.put("jobid", jobId); 
		jobInstanceFieldValues.put("plantime", planTime);
		jobInstanceFieldValues.put("createtime", currentTime);
		jobInstanceFieldValues.put("statusname", StatusType.waiting.toString());
		this.dBParserAccess.insertByData(dbSession, jobInstanceData, jobInstanceFieldValues); 
	}
	
	private Date getNextRunTime(String timeConfig) throws Exception{
		Date currentTime = new Date();
		Calendar currentCalendar = Calendar.getInstance(); 
		currentCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		currentCalendar.setTime(currentTime);
		int currentYear = currentCalendar.get(Calendar.YEAR);
		int currentMonth = currentCalendar.get(Calendar.MONTH);
		int currentDay = currentCalendar.get(Calendar.DATE);
		int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = currentCalendar.get(Calendar.MINUTE);
		int currentSecond = currentCalendar.get(Calendar.SECOND);
		Integer currentTimeValue = currentHour * 10000 + currentMinute * 100 + currentSecond;
		Integer currentDateValue = currentYear * 10000 + currentMonth * 100 + currentDay;
		
		String[] splitedTimeStrs = timeConfig.split(";");
		if(splitedTimeStrs.length == 6){
			//按照日期设定 
			try{
				List<Integer> years = this.sortValues(splitedTimeStrs[0].split(","), currentYear, currentYear + 10);
				List<Integer> months = this.sortValues(splitedTimeStrs[1].split(","), 0, 11);
				List<Integer> days = this.sortValues(splitedTimeStrs[2].split(","), 1, 31);
				List<Integer> hours = this.sortValues(splitedTimeStrs[3].split(","), 0, 23);
				List<Integer> minutes = this.sortValues(splitedTimeStrs[4].split(","), 0, 59);
				List<Integer> seconds = this.sortValues(splitedTimeStrs[5].split(","), 0, 59);
				List<Integer> timeValues = new ArrayList<Integer>();
				for(Integer hour : hours){
					for(Integer minute : minutes){
						for(Integer second : seconds){
							timeValues.add(hour * 10000 + minute * 100 + second);
						}						
					}
				}
				List<Integer> dateValues = new ArrayList<Integer>();
				for(Integer year : years){
					for(Integer month : months){
						for(Integer day : days){
							dateValues.add(year * 10000 + month * 100 + day);
						}						
					}
				}

				boolean getNextDay = false;
				String nextTimeStr = null;
				if(currentTimeValue > timeValues.get(timeValues.size() - 1)){
					nextTimeStr = String.format("%06d", timeValues.get(0));
					getNextDay = true;
				}
				else{
					for(Integer timeValue : timeValues){
						if(currentTimeValue < timeValue){
							nextTimeStr = String.format("%06d", timeValue);
							break;
						}
					}
				}

				String nextDateStr = ""; 
				for(Integer dateValue : dateValues){
					if(currentDateValue <= dateValue){
						if(!getNextDay || currentDateValue < dateValue){ 
							nextDateStr = String.format("%08d", dateValue); 
							break;
						}
					}
				}
				Calendar nextDate = Calendar.getInstance();
				nextDate.set(Calendar.MILLISECOND, 0);
				
				if(nextDateStr.isEmpty()){
					return null;
				}
				else{
					nextDate.set(Integer.parseInt(nextDateStr.substring(0, 4)),
							Integer.parseInt(nextDateStr.substring(4, 6)),
							Integer.parseInt(nextDateStr.substring(6, 8)),
							Integer.parseInt(nextTimeStr.substring(0, 2)),
							Integer.parseInt(nextTimeStr.substring(2, 4)),
							Integer.parseInt(nextTimeStr.substring(4, 6))); 
					return nextDate.getTime();
				}
			}
			catch(Exception ex){
				throw new Exception("执行时间设置错误, timeConfig = " + timeConfig);
			}
		}
		else if(splitedTimeStrs.length == 5){
			//按照星期几设定
			try{
				int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);	
	
				List<Integer> years = this.sortValues(splitedTimeStrs[0].split(","), currentYear, currentYear + 10);
				List<Integer> weekdays = this.sortValues(splitedTimeStrs[1].split(","), 1, 7);
				List<Integer> hours = this.sortValues(splitedTimeStrs[3].split(","), 0, 23);
				List<Integer> minutes = this.sortValues(splitedTimeStrs[4].split(","), 0, 59);
				List<Integer> seconds = this.sortValues(splitedTimeStrs[5].split(","), 0, 59);
				List<Integer> timeValues = new ArrayList<Integer>();
				for(Integer hour : hours){
					for(Integer minute : minutes){
						for(Integer second : seconds){
							timeValues.add(hour * 10000 + minute * 100 + second);
						}						
					}
				}
	
				boolean getNextDay = false;
				String nextTimeStr = null;
				if(currentTimeValue > timeValues.get(timeValues.size() - 1)){
					nextTimeStr = String.format("%06d", timeValues.get(0));
					getNextDay = true;
				}
				else{
					for(Integer timeValue : timeValues){
						if(currentTimeValue < timeValue){
							nextTimeStr = String.format("%06d", timeValue);
							break;
						}
					}
				}
	
				Integer nextWeekDay= null; 
				for(Integer weekDayValue : weekdays){
					if(currentDayOfWeek <= weekDayValue){
						if(!getNextDay || currentDayOfWeek < weekDayValue){
							nextWeekDay = weekDayValue;
							break;
						}
					}
				}
				Calendar nextDate = Calendar.getInstance();
				nextDate.setTime(new Date());
				nextDate.set(Calendar.MILLISECOND, 0);
				nextDate.set(Calendar.DATE, nextDate.get(Calendar.DATE) + (nextWeekDay > currentDayOfWeek ? nextWeekDay - currentDayOfWeek: 7 + nextWeekDay - currentDayOfWeek));
				if(years.contains(nextDate.get(Calendar.YEAR))){
					nextDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(nextTimeStr.substring(0, 2)));
					nextDate.set(Calendar.MONTH, Integer.parseInt(nextTimeStr.substring(2, 4)));
					nextDate.set(Calendar.SECOND, Integer.parseInt(nextTimeStr.substring(4, 6)));
					return nextDate.getTime();			
				}
				else{
					return null;
				}
			}
			catch(Exception ex){
				throw new Exception("执行时间设置错误, timeConfig = " + timeConfig);
			}
		}
		else{
			throw new Exception("执行时间格式设置错误, 应为 '年;月;日;时;分;秒' 或 '年;周几 ;时;分;秒', timeConfig = " + timeConfig);
		} 
	}
	
	public List<String> checkTimeConfig(String timeConfig) throws Exception{
		List<String> errors = new ArrayList<String>(); 	
		Date currentTime = new Date();
		Calendar currentCalendar = Calendar.getInstance(); 
		currentCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		currentCalendar.setTime(currentTime);
		int currentYear = currentCalendar.get(Calendar.YEAR); 
		
		String[] splitedTimeStrs = timeConfig.split(";");
		if(splitedTimeStrs.length == 6){
			try{
				//按照日期设定 
				List<Integer> years = this.sortValues(splitedTimeStrs[0].split(","), currentYear, currentYear + 10);
				List<Integer> months = this.sortValues(splitedTimeStrs[1].split(","), 0, 11);
				List<Integer> days = this.sortValues(splitedTimeStrs[2].split(","), 1, 31);
				List<Integer> hours = this.sortValues(splitedTimeStrs[3].split(","), 0, 23);
				List<Integer> minutes = this.sortValues(splitedTimeStrs[4].split(","), 0, 59);
				List<Integer> seconds = this.sortValues(splitedTimeStrs[5].split(","), 0, 59); 	
			}
			catch(Exception ex){
				errors.add("执行时间设置错误, timeConfig = " + timeConfig + "\r\n" + ex.getMessage());
			}
		}
		else if(splitedTimeStrs.length == 5){
			try{
				//按照星期几设定
				List<Integer> years = this.sortValues(splitedTimeStrs[0].split(","), currentYear, currentYear + 10);
				List<Integer> weekdays = this.sortValues(splitedTimeStrs[1].split(","), 1, 7);
			}
			catch(Exception ex){
				errors.add("执行时间设置错误, timeConfig = " + timeConfig + ex.getMessage());
			}
		}
		else{
			errors.add("执行时间格式设置错误, 应为 '年;月;日;时;分;秒' 或 '年;周几 ;时;分;秒', timeConfig = " + timeConfig);
		} 
		return errors;
	}
	
	private List<Integer> sortValues(String[] values, Integer minValue, Integer maxValue) throws Exception{
		List<Integer> ints = new ArrayList<Integer>(); 
		for(String value : values){  
			if(value.equals("*")){
				for(Integer i=minValue; i<=maxValue; i++){
					ints.add(i);
				}
				return ints;
			}
		}
		for(String value : values){  
			if(!value.isEmpty()){
				Integer intValue = null;
				try{
					intValue  = Integer.parseInt(value);
					if(maxValue != null && intValue > maxValue){
						throw new Exception(value + " can not be more than " + maxValue);
					}
					else if(minValue != null && intValue < minValue){
						throw new Exception(value + " can not be less than " + maxValue);
					}
					else{	
						this.addToSortedList(ints, intValue);
					}
				}
				catch(Exception ex){
					throw new Exception("Can not convert " + value + " to integer");
				}
			}
		}
		return ints;
	}
	
	private void addToSortedList(List<Integer> ints, Integer intValue){
		for(int i = 0;i < ints.size(); i++){
			if(ints.get(i) > intValue){
				ints.add(i, intValue);
				return;
			}
		}	
		ints.add(intValue);
	}
	
	public boolean checkCanRun(Session dbSession, String jobId) throws SQLException{
		String selectSql = "select count(1) as rowCount from skd_job sj where sj.id = " + SysConfig.getParamPrefix() + "jobid and sj.isactive = 'Y' and sj.isdeleted = 'N'";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("jobid", jobId);
		int rowCount = ((BigInteger)this.dBParserAccess.selectOne(dbSession, selectSql, p2vs)).intValue(); 
		return rowCount > 0;
	}
	
	public void updateJobInstance( Session dbSession, String jobInstanceId, StatusType statusType, String note) throws Exception{ 
		Date currentTime = new Date();
		HashMap<String, Object> jobInstanceFieldValues = new HashMap<String, Object>();
		jobInstanceFieldValues.put("jobinstanceid", jobInstanceId);
		jobInstanceFieldValues.put("note", note);
		String updateSql = "";
		switch(statusType){ 
			//正在执行
			case running:{
				updateSql = "update skd_jobinstance set statusname = " + SysConfig.getParamPrefix() + "statusname, runtime = " + SysConfig.getParamPrefix() + "runtime, note = " + SysConfig.getParamPrefix() + "note where id = " +SysConfig.getParamPrefix() +"jobinstanceid";
				jobInstanceFieldValues.put("statusname", StatusType.running.toString());
				jobInstanceFieldValues.put("runtime", currentTime);
			}
			break;
			//执行出错
			case error:{
				updateSql = "update skd_jobinstance set statusname = " + SysConfig.getParamPrefix() + "statusname, endtime = " + SysConfig.getParamPrefix() + "endtime, note = " + SysConfig.getParamPrefix() + "note where id = " +SysConfig.getParamPrefix() +"jobinstanceid";
				jobInstanceFieldValues.put("statusname", StatusType.running.toString());
				jobInstanceFieldValues.put("endtime", currentTime);
			}
			break;
			//执行完成
			case succeed:{
				updateSql = "update skd_jobinstance set statusname = " + SysConfig.getParamPrefix() + "statusname, endtime = " + SysConfig.getParamPrefix() + "endtime, note = " + SysConfig.getParamPrefix() + "note where id = " +SysConfig.getParamPrefix() +"jobinstanceid";
				jobInstanceFieldValues.put("statusname", StatusType.succeed.toString());
				jobInstanceFieldValues.put("endtime", currentTime);
			}
			break;
			default:{
				throw new Exception("Error status type, statusName = " + statusType.toString() + ", jobInstanceId = " + jobInstanceId);
			}
		}  
		 this.dBParserAccess.update(dbSession, updateSql, jobInstanceFieldValues); 
	}  
	
	public void executeJobThreads(){
		try{
			if(this.getRunning()){
				ThreadPoolTaskExecutor threadPool = this.getJobThreadPool();
			    int activeThreadCount = threadPool.getActiveCount();
			    int maxThreadCount = threadPool.getMaxPoolSize();
			    int canAddThreadCount = maxThreadCount - activeThreadCount;
			    
			    if(canAddThreadCount > 0){
			    	Date currentTime = new Date();
				    String jobInstanceSql = "select jobinst.id as id, " + SysConfig.getParamPrefix() + "currenttime as currenttime, jobinst.jobid as jobid, jobinst.statusname as statusname, jobinst.plantime as plantime, job.runexp as runexp, job.runparameters as runparameters from skd_jobinstance jobinst left outer join skd_job job on jobinst.jobid = job.id where job.isdeleted = 'N' and  job.isactive = 'Y' and jobinst.statusname = 'waiting' and jobinst.plantime < " + SysConfig.getParamPrefix() + "currenttime order by jobinst.plantime asc";
				    HashMap<String, Object> p2vs = new HashMap<String, Object>();
				    p2vs.put("currenttime", currentTime);
				    List<String> alias = new ArrayList<String>();
				    alias.add("id");
				    alias.add("currenttime");
				    alias.add("jobid");
				    alias.add("statusname");
				    alias.add("plantime");	  
				    alias.add("runexp");	  
				    alias.add("runparameters");	    
				    HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
				    fieldValueTypes.put("id", ValueType.String);
				    fieldValueTypes.put("currenttime", ValueType.Time);
				    fieldValueTypes.put("jobid", ValueType.String);
				    fieldValueTypes.put("statusname", ValueType.String);
				    fieldValueTypes.put("plantime", ValueType.Time);	 
				    fieldValueTypes.put("runexp", ValueType.String);
				    fieldValueTypes.put("runparameters", ValueType.String);   
					Session dbSession = null;
					try{
						dbSession = this.transactionManager.getSessionFactory().openSession();	
					    DataTable jobInstTable = this.dBParserAccess.selectList(dbSession, jobInstanceSql, p2vs, alias, fieldValueTypes, 0, canAddThreadCount);
			
					    List<DataRow> jobInstRows = jobInstTable.getRows();
					    for(DataRow jobInstRow : jobInstRows){
					    	String jobId = jobInstRow.getStringValue("jobid");
					    	String jobInstanceId = jobInstRow.getStringValue("id");
					    	try{
								this.updateJobInstance(dbSession, jobInstanceId, StatusType.running, "");  
						    	String runExp = jobInstRow.getStringValue("runexp");
						    	String runParameters = jobInstRow.getStringValue("runparameters");
						    	JobInstanceRunner jobInstRunner = new JobInstanceRunner();
						    	jobInstRunner.setJobId(jobId);
						    	jobInstRunner.setJobInstanceId(jobInstanceId);
						    	jobInstRunner.setRunExp(runExp);
						    	jobInstRunner.setRunParameters(runParameters);
						    	jobInstRunner.setJobManager(this);
						    	Thread runThread = threadPool.createThread(jobInstRunner);
						    	runThread.start();
					    	}
					    	catch(Exception instEx){
					    		instEx.printStackTrace();
								logger.error("executeJobThread error, jobInstId = " + jobInstanceId, instEx);
					    	}
					    }		
					}
					catch(Exception ex){
						throw new Exception("ExecuteJobThreads error. " , ex);
					}
					finally{
						if(dbSession != null){
							dbSession.close();
						}
					}
			    }
			    else{
			    	logger.info("executeJobThreads线程已满，不能启动新的线程.");
			    }
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			logger.error("executeJobThreads error", ex);
		}	    
	}
}
