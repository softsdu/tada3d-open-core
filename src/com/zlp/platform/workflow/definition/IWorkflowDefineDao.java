package com.zlp.platform.workflow.definition;

import java.sql.SQLException;
import java.util.HashMap;

import org.hibernate.Session;

import com.zlp.platform.common.INcpSession;
import com.zlp.platform.model.sysmodel.DataField; 

public interface IWorkflowDefineDao { 
	void setDBSession(Session dbSession); 
	void deleteWorkflow(String workflowId) throws Exception;
	Wf_Workflow getWorkflow(String workflowId) throws Exception;
	String saveWorkflow(INcpSession ncpSession, Wf_Workflow newWorkflow) throws Exception; 
	HashMap<String, DataField> getDocFields(String docTypeId);
}
