package com.zlp.platform.workflow.definition;
 
import java.sql.SQLException; 
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
 
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.model.sysmodel.Sheet;
import com.zlp.platform.model.sysmodel.SheetCollection;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;
import com.zlp.platform.expression.definition.RunAt;
import com.zlp.platform.expression.definition.ValidateResult;
import com.zlp.platform.expression.definition.Validator;
import java.math.BigInteger; 

//流程定义Dao类
public class WorkflowDefineDao implements IWorkflowDefineDao{

	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}
	private Validator expValidator; 
	public void setExpValidator(Validator expValidator) {
		this.expValidator = expValidator;
	}   
	 
	private Session dbSession = null;
	protected Session getDBSession(){ 
		if(this.dbSession == null){
			throw new RuntimeException("none db session.");
		}
		return this.dbSession;
	} 
	public void setDBSession(Session dbSession){
		this.dbSession = dbSession;
	}
	
	private HashMap<String, ValueType> getDocFieldTypes(String docTypeId){
		HashMap<String, DataField> allFields = this.getDocFields(docTypeId);
		HashMap<String, ValueType> fieldTypes = new HashMap<String, ValueType>();
		if(allFields != null){
			for(String fieldName : allFields.keySet()){
				DataField field = allFields.get(fieldName); 
				ValueType valueType = field.getValueType();
				fieldTypes.put(fieldName, valueType);
			}
		}
		return fieldTypes;
	}
	
	public HashMap<String, DataField> getDocFields(String docTypeId){
		Data docTypeData = DataCollection.getData("wf_DocType");
		Session dbSession = this.getDBSession();
		DataTable dt = this.dBParserAccess.getDtById(dbSession, docTypeData, docTypeId);
		List<DataRow> rows = dt.getRows();
		if(rows.size() > 0){
			DataRow row = rows.get(0);
			String sheetName = row.getStringValue("sheetname");
			Sheet sheet = SheetCollection.getSheet(sheetName);
			String viewName = sheet.getMainPart().getViewName();
			View view = ViewCollection.getView(viewName);
			String dataName = view.getDataName();
			Data docData = DataCollection.getData(dataName);
			HashMap<String, DataField> allFields = docData.getDataFields();
			return allFields;
		}
		else{
			return null;
		}
	}
	
	private boolean checkWorkflow(Session dbSession, Wf_Workflow workflow, List<String> errors) throws Exception{
		
		String workflowId = workflow.getId();
		List<Wf_Node> allNodes = workflow.getNodes();
		List<Wf_Link> allLinks = workflow.getLinks();
		String docTypeId= workflow.getDocTypeId();
		HashMap<String, ValueType> fieldTypes = null;
		
		//判断是否指定了所属组织
		boolean hasOrgId = true;
		if(workflow.getOrgId() == null || workflow.getOrgId().isEmpty()){
			errors.add("未指定适用组织");
			hasOrgId = false;
		}
		
		//判断是否指定了单据类型
		boolean hasDocType = true;
		if(workflow.getDocTypeId() == null || workflow.getDocTypeId().isEmpty()){
			errors.add("未指定适用单据类型");
			hasDocType = false;
			fieldTypes= new HashMap<String, ValueType>();
		}
		else{
			fieldTypes = this.getDocFieldTypes(docTypeId);
		}

		//摘要信息表达式
		this.checkWorkflowExpression("摘要信息表达式", workflow.getAbstractExp(), fieldTypes, RunAt.Server, ValueType.String, errors, false);
				
		//此流程对应的单据类型、组织没有指定另外一个已启用的流程
		if(hasOrgId && hasDocType){
			String checkExistSql = "select count(1) wfCount from wf_workflow wf where wf.orgid=" + SysConfig.getParamPrefix() + "orgid and wf.doctypeid=" + SysConfig.getParamPrefix() + "doctypeid and wf.isactive = 'Y' and wf.id <> " + SysConfig.getParamPrefix() + "id";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("id", workflowId);
			p2vs.put("orgid", workflow.getOrgId());
			p2vs.put("doctypeid", workflow.getDocTypeId()); 
			int existCount = ((BigInteger)this.dBParserAccess.getSingleValue(dbSession, checkExistSql, p2vs)).intValue();
			if(existCount > 0){
				errors.add("已定义此流程, 组织=" + workflow.getOrgName() + "(" + workflow.getOrgCode() + "), 单据类型=" + workflow.getDocTypeName());
			}
		}
				
		//流程只能包含一个开始、一个结束节点
		int startCount = 0;
		int endCount = 0;
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i);
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{NodeTypeLegend.start})){
				startCount++;
			}
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{NodeTypeLegend.end})){
				endCount++;
			}
			if(startCount > 1){
				errors.add("只允许包含一个开始节点.");					
			}
			if(endCount > 1){
				errors.add("只允许包含一个结束节点.");					
			}
		} 
		
		//active节点、开始节点、结束并行节点只能包含一个后续节点
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i);
			int linkFromCount = 0;
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.start,  NodeTypeLegend.active,  NodeTypeLegend.endParallel})){
				String nodeId = node.getId();
				for(int j=0;j<allLinks.size();j++){
					Wf_Link link = allLinks.get(j);
					if(nodeId.equals(link.getFromNodeId())){
						linkFromCount++;
					}
				}
				if(linkFromCount != 1){
					errors.add("节点'" + node.getName() + "'有且只能有一个后续节点.");
				}
			}
		}

		//group内的active节点、判断节点只能连接组内节点，或者子组节点的并行开始节点
		for(int i=0;i<allNodes.size();i++){
			Wf_Node fromNode = allNodes.get(i); 
			if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.active})){
				String fromNodeId = fromNode.getId();
				for(int j=0;j<allLinks.size();j++){
					Wf_Link link = allLinks.get(j);
					if(fromNodeId.equals(link.getFromNodeId())){
						String toNodeId = link.getToNodeId();
						Wf_Node toNode = this.getNodeById(allNodes, toNodeId);
						if(!this.checkSameGroup(fromNode, toNode)){
							if(!this.checkInSubGroup(allNodes, fromNode, toNode)){
								errors.add("节点'" + fromNode.getName() + "'不能连接节点'" + toNode.getName() + "'");
							}
						}
					}
				} 
			}
		}
		
		//如果并行开始节点的入边的起点为并行结束，那么两个点所属的并行节点同组，且不是同一个并行节点
		for(int i=0;i<allNodes.size();i++){
			Wf_Node toNode = allNodes.get(i); 
			if(this.checkIsNodeType(toNode, new NodeTypeLegend[]{ NodeTypeLegend.startParallel})){
				String toNodeId = toNode.getId();
				for(int j=0;j<allLinks.size();j++){
					Wf_Link link = allLinks.get(j);
					Wf_Node fromNode = this.getNodeById(allNodes, link.getFromNodeId());
					if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.endParallel})){
						String toNodeGroupId = toNode.getGroupId();
						String fromNodeGroupId = fromNode.getGroupId();
						if(toNodeGroupId.equals(fromNodeGroupId)){
							errors.add("节点'" + toNode.getName() + "'的后续节点不能是'" + fromNode.getName() + "'");
						}
						else{
							Wf_Node fromGroupNode = this.getNodeById(allNodes, fromNodeGroupId);
							Wf_Node toGroupNode = this.getNodeById(allNodes, toNodeGroupId);
							if(!this.checkSameGroup(fromGroupNode, toGroupNode)){
								errors.add("节点'" + toNode.getName() + "'的后续节点不能是'" + fromNode.getName() + "'");
							}
						}
					}
				}
			}
		}

		//如果并行开始节点的入边的起点为active节点、判断节点、开始节点、并行开始节点，那么其所属并行节点和起点同组
		for(int i=0;i<allNodes.size();i++){
			Wf_Node toNode = allNodes.get(i); 
			if(this.checkIsNodeType(toNode, new NodeTypeLegend[]{ NodeTypeLegend.startParallel})){
				String toNodeId = toNode.getId();
				for(int j=0;j<allLinks.size();j++){
					Wf_Link link = allLinks.get(j);
					Wf_Node fromNode = this.getNodeById(allNodes, link.getFromNodeId());
					if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.active, NodeTypeLegend.judge, NodeTypeLegend.start, NodeTypeLegend.startParallel })){ 
						String toNodeGroupId = toNode.getGroupId();  
						Wf_Node toGroupNode = this.getNodeById(allNodes, toNodeGroupId);
						if(!this.checkSameGroup(fromNode, toGroupNode)){
							errors.add("节点'" + toNode.getName() + "'的后续节点不能是'" + fromNode.getName() + "'");
						}
					}
				}
			}
		}		 

		//如果并行结束节点的出边的止点为active节点、判断节点、开始节点、并行结束节点，那么其所属并行节点和止点同组
		for(int i=0;i<allNodes.size();i++){
			Wf_Node fromNode = allNodes.get(i); 
			if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.endParallel})){
				String fromNodeId = fromNode.getId();
				for(int j=0;j<allLinks.size();j++){
					Wf_Link link = allLinks.get(j);
					Wf_Node toNode = this.getNodeById(allNodes, link.getFromNodeId());
					if(this.checkIsNodeType(toNode, new NodeTypeLegend[]{ NodeTypeLegend.active, NodeTypeLegend.judge, NodeTypeLegend.start, NodeTypeLegend.endParallel })){
						String fromNodeGroupId = fromNode.getGroupId();  
						Wf_Node fromGroupNode = this.getNodeById(allNodes, fromNodeGroupId);
						if(!this.checkSameGroup(toNode, fromGroupNode)){
							errors.add("节点'" + toNode.getName() + "'的后续节点不能是'" + fromNode.getName() + "'");
						}
					}
				}
			}
		}		 

		//除开始节点可以无入边、结束节点一定无出边、并行节点无出边和入边，其他节点至少有一个出、一个入边
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.start })){
				if(this.getLinkOutCount(allLinks, node) == 0){
					errors.add("节点'" + node.getName() + "'必须有后续节点.");
				} 
			}
			else if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.end })){
				if(this.getLinkOutCount(allLinks, node) > 0){
					errors.add("节点'" + node.getName() + "'不允许有后续节点.");
				}
				if(this.getLinkInCount(allLinks, node) == 0){
					errors.add("节点'" + node.getName() + "'必须有前置节点.");
				}
			}
			else if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.parallel })){
				if(this.getLinkOutCount(allLinks, node) > 0){
					errors.add("节点'" + node.getName() + "'不允许有后续节点.");
				}
				if(this.getLinkInCount(allLinks, node) > 0){
					errors.add("节点'" + node.getName() + "'不允许有前置节点.");
				}
			}
			else{
				if(this.getLinkOutCount(allLinks, node) == 0){
					errors.add("节点'" + node.getName() + "'必须有后续节点.");
				}
				if(this.getLinkInCount(allLinks, node) == 0){
					errors.add("节点'" + node.getName() + "'必须有前置节点.");
				}
			}
			
		}

		//并行节点必须包含一个并行开始、一个并行结束节点
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.parallel })){
				String groupNodeId = node.getId();
				int startParallelCount = 0;
				int endParallelCount = 0;
				for(int j=0;j<allNodes.size();j++){
					Wf_Node checkNode = allNodes.get(j); 
					if(this.checkIsNodeType(checkNode, new NodeTypeLegend[]{ NodeTypeLegend.startParallel })
						&& groupNodeId.equals(checkNode.getGroupId())){
						startParallelCount++;
					}
					if(this.checkIsNodeType(checkNode, new NodeTypeLegend[]{ NodeTypeLegend.endParallel })
						&& groupNodeId.equals(checkNode.getGroupId())){
						endParallelCount++;
					}
				}
				if(startParallelCount > 1){
					errors.add("并行节点'" + node.getName() + "'中不能包含多个并行开始节点.");
				}
				if(endParallelCount > 1){
					errors.add("并行节点'" + node.getName() + "'中不能包含多个并行结束节点.");
				}
			}
		}
		
		//active类型节点必须指定审批人表达式
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active })){ 
				this.checkNodeExpression(node.getName(), "审批人表达式", node.getUserExp(), fieldTypes, RunAt.Server, ValueType.String, errors, false); 
			}
		}
		
		//根据active节点、开始节点的操作类型，验证执行活动表达式
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active})){ 
				TriggerType triggerType = node.getTriggerTypeEnum(); 
				switch (triggerType) {
					case clientManual: 
						if(node.getActionPageUrl() == null || node.getActionPageUrl().isEmpty()){
							errors.add("节点'" + node.getName() + "'没有指定执行页面地址.");
						}
						if(node.getReviewPageUrl() == null || node.getReviewPageUrl().isEmpty()){
							errors.add("节点'" + node.getName() + "'没有指定查看历史页面地址.");
						}
						break;
					case externalInvoke:
					case serverAuto: 
					default:
						break;
				}
			}
		}
		
		//开始节点、结束节点、active节点的状态名称不能为空
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active, NodeTypeLegend.start, NodeTypeLegend.end })){
				String statusName = node.getStatusName();
				if(statusName == null || statusName.isEmpty()){
					errors.add("节点'" + node.getName() + "'的状态名称不能为空.");
				}
			}
		}
		
		//验证通过后执行、流转至执行、退回至执行、通过票数表达式不为空时，表达式语法正确
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			
			this.checkNodeExpression(node.getName(), "流转至表达式", node.getInExp(), fieldTypes, RunAt.Server, ValueType.Boolean, errors, true); 

			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.end })){ 
				this.checkNodeExpression(node.getName(), "通过后执行表达式", node.getPassExp(), fieldTypes, RunAt.Server, ValueType.Boolean, errors, true);

				this.checkNodeExpression(node.getName(), "退回至执行表达式", node.getBackInExp(), fieldTypes, RunAt.Server, ValueType.Boolean, errors, true);
			}
			
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active })){ 
				this.checkNodeExpression(node.getName(), "通过票数表达式", node.getTicketExp(), fieldTypes, RunAt.Server, ValueType.Decimal, errors, true); 
			}
			
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active })){ 
				this.checkNodeExpression(node.getName(), "定时处理时间表达式", node.getTimingExp(), fieldTypes, RunAt.Server, ValueType.Time, errors, true); 
			}
		}
		
		//验证开始、活动节点的异步调用设置是否正确
		for(int i=0;i<allNodes.size();i++){
			Wf_Node node = allNodes.get(i); 
			if(this.checkIsNodeType(node, new NodeTypeLegend[]{ NodeTypeLegend.active, NodeTypeLegend.start})){
				String aisId = node.getAisId();
				if(aisId != null && !aisId.isEmpty()){
					
					String aisParameterExp = node.getAisParameterExp();

					JSONObject aisPEObj = JSONProcessor.strToJSON(aisParameterExp);
					
					//获取异步调用服务参数设置					
					List<DataRow> serviceParameterConfigs = this.getAsynInvokeServiceParameterConfig(aisId);
					for(DataRow spRow : serviceParameterConfigs){
						String pName = spRow.getStringValue("name");
						String pValueTypeStr = spRow.getStringValue("valuetype");
						ValueType pValueType = (ValueType)Enum.valueOf(ValueType.class, pValueTypeStr);
						
						if(!aisPEObj.containsKey(pName)){
							errors.add("节点'" + node.getName() + "'的异步服务参数设置错误.");
						}
						else{
							String aisPExp = aisPEObj.getString(pName);
							if(aisPExp != null && !aisPExp.isEmpty()){
								aisPExp = CommonFunction.decode(aisPExp);
								ValidateResult validateResult = this.checkExpression(aisPExp, fieldTypes, RunAt.Server, pValueType);
								if(!validateResult.getSucceed()){
									String expErrorString = CommonFunction.listToString(validateResult.getErrors(), "");
									errors.add("节点'" + node.getName()  + "'的异步服务参数'" + pName + "'验证错误: " + expErrorString + ".");
								}
							}
						}
					}
				}
			}
		}
		
		//边的表达式正确
		for(int i=0;i<allNodes.size();i++){
			Wf_Node fromNode = allNodes.get(i); 
			List<Wf_Link> outLinks = this.getOutLinks(allLinks, fromNode.getId());
			
			//开始节点、active节点、并行结束节点,那么后续边的表达式为空
			if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.start, NodeTypeLegend.active, NodeTypeLegend.endParallel })){	
				for(int j=0;j<outLinks.size();j++){
					Wf_Link link = outLinks.get(j);
					String conditionExp = link.getConditionExp();
					if(conditionExp != null && !conditionExp.isEmpty()){
						errors.add("无需指定节点'" + fromNode.getName() + "'的边表达式.");
					}
				}
			}			
			else if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.judge })){
				int emptyConditionCount = 0;
				for(int j=0;j<outLinks.size();j++){
					Wf_Link link = outLinks.get(j);
					String conditionExp = link.getConditionExp();
					if(conditionExp != null && !conditionExp.isEmpty()){
						this.checkLinkExpression(link.getName(), link.getConditionExp(), fieldTypes, RunAt.Server, ValueType.Boolean, errors, true); 
					}
					else{
						emptyConditionCount++;
					}
				}
				if(emptyConditionCount != 1){
					errors.add("判断节点'" + fromNode.getName() + "'的后续边表达式有且只能有一条为空.");
				}
			}
			else if(this.checkIsNodeType(fromNode, new NodeTypeLegend[]{ NodeTypeLegend.startParallel })){ 
				for(int j=0;j<outLinks.size();j++){
					Wf_Link link = outLinks.get(j);
					String conditionExp = link.getConditionExp();
					if(conditionExp != null && !conditionExp.isEmpty()){
						this.checkLinkExpression(link.getName(), link.getConditionExp(), fieldTypes, RunAt.Server, ValueType.Boolean, errors, true); 
					} 
				} 
			}
		} 
		
		//-------执行阶段判断-------
		//通过审批人表达式获取到的审批人个数，不能少于通过票数表达式运算值
	
		return errors.size() == 0;
	} 
	
	private boolean checkIsNodeType(Wf_Node node, NodeTypeLegend[] nodeTypes){
		for(NodeTypeLegend nodeType : nodeTypes){
			if(nodeType.toString().equals(node.getNodeType())){
				return true;
			}
		}
		return false;
	}
	
	private boolean checkIsNotNodeType(Wf_Node node, NodeTypeLegend[] nodeTypes){
		for(NodeTypeLegend nodeType : nodeTypes){
			if(nodeType.toString().equals(node.getNodeType())){
				return false;
			}
		}
		return true;
	}
	
	private List<DataRow> getAsynInvokeServiceParameterConfig(String aisId) throws SQLException{
		String aisParameterConfigSql = "select aissp.id as id, aissp.name as name, aissp.valuetype as valuetype"
			+ " from ais_serviceparameter aissp where aissp.serviceid = " + SysConfig.getParamPrefix() + "serviceid";
		
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("serviceid", aisId);
		
		List<String> alias = new ArrayList<String>();
		alias.add("id");
		alias.add("name");
		alias.add("valuetype");
		
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("name", ValueType.String);
		fieldValueTypes.put("valuetype", ValueType.String);
		
		Session session = this.getDBSession();
		DataTable spTable = this.dBParserAccess.selectList(session, aisParameterConfigSql, p2vs, alias, fieldValueTypes);
		return spTable.getRows();
	}
	
	private void checkWorkflowExpression(String expName, String exp, HashMap<String, ValueType> fieldTypes, RunAt runAt, ValueType needResultType, List<String> errors, boolean canEmpty) throws Exception{
		if(exp == null || exp.isEmpty()){ 
			if(!canEmpty){
				errors.add("'没有指定流程的" + expName + ".");
			} 
		}
		else{
			ValidateResult validateResult = this.checkExpression(exp, fieldTypes, runAt, needResultType);
			if(!validateResult.getSucceed()){
				String expErrorString = CommonFunction.listToString(validateResult.getErrors(), "");
				errors.add(expName + "'表达式验证错误: " + expErrorString + ".");
			}
		}
	}
	
	private void checkLinkExpression(String linkName, String exp, HashMap<String, ValueType> fieldTypes, RunAt runAt, ValueType needResultType, List<String> errors, boolean canEmpty) throws Exception{
		ValidateResult validateResult = this.checkExpression(exp, fieldTypes, runAt, needResultType);
		if(!validateResult.getSucceed()){
			String expErrorString = CommonFunction.listToString(validateResult.getErrors(), "");
			errors.add("边'" + linkName + "'的表达式" + exp + "验证错误: " + expErrorString + ".");
		}
	}
	
	private void checkNodeExpression(String nodeName,String expName, String exp, HashMap<String, ValueType> fieldTypes, RunAt runAt, ValueType needResultType, List<String> errors, boolean canEmpty) throws Exception{
		if(exp == null || exp.isEmpty()){
			if(!canEmpty){
				errors.add("节点'" + nodeName + "'没有指定" + expName + ".");
			}
		}
		else{
			ValidateResult validateResult = this.checkExpression(exp, fieldTypes, runAt, needResultType);
			if(!validateResult.getSucceed()){
				String expErrorString = CommonFunction.listToString(validateResult.getErrors(), "");
				errors.add("节点'" + nodeName + "'的" + expName + "验证错误: " + expErrorString + ".");
			}
		}
	}
	
	private ValidateResult checkExpression(String exp, HashMap<String, ValueType> fieldTypes, RunAt runAt, ValueType needResultType) throws Exception{
		return this.expValidator.validateExp(exp, fieldTypes, runAt, needResultType);
	}
	
	private List<Wf_Link> getOutLinks(List<Wf_Link> links, String nodeId){
		List<Wf_Link> outLinks = new ArrayList<Wf_Link>();
		int linkCount = links.size(); 
		for(int i=0;i<linkCount;i++){
			Wf_Link link = links.get(i);
			if(link.getFromNodeId().equals(nodeId)){
				outLinks.add(link);
			}
		}
		return outLinks;
	}
	
	private int getLinkInCount(List<Wf_Link> links, Wf_Node node){
		int inCount = 0;
		int linkCount = links.size();
		String nodeId = node.getId();
		for(int i=0;i<linkCount;i++){
			Wf_Link link = links.get(i);
			if(link.getToNodeId().equals(nodeId)){
				inCount++;
			}
		}
		return inCount;
	}
	
	private int getLinkOutCount(List<Wf_Link> links, Wf_Node node){
		int outCount = 0;
		int linkCount = links.size();
		String nodeId = node.getId();
		for(int i=0;i<linkCount;i++){
			Wf_Link link = links.get(i);
			if(link.getFromNodeId().equals(nodeId)){
				outCount++;
			}
		}
		return outCount;
	}

	
	private boolean checkSameGroup(Wf_Node nodeA, Wf_Node nodeB){
		String groupA = nodeA.getGroupId();
		String groupB = nodeB.getGroupId();
		if(groupA == null){
			return groupB == null;
		}
		else{
			return groupA.equals(groupB);
		}
	}
	
	private boolean checkInSubGroup(List<Wf_Node> allNodes, Wf_Node node, Wf_Node subGroupNode){
		String groupId = subGroupNode.getGroupId();
		if(groupId != null && !groupId.isEmpty()){
			Wf_Node groupNode = this.getNodeById(allNodes, groupId);
			return checkSameGroup(node, groupNode);
		}
		else{
			return false;
		}
	}
	
	private boolean checkInParentGroup(List<Wf_Node> allNodes, Wf_Node node, Wf_Node parentGroupNode){
		return checkInSubGroup(allNodes, parentGroupNode, node);
	}
	
	private Wf_Link getLinkById(List<Wf_Link> links, String linkId){
		int linkCount = links.size();
		for(int i=0;i<linkCount;i++){
			Wf_Link link = links.get(i);
			if(link.getId().equals(linkId)){
				return link;
			}
		}
		return null;
	}
	
	private Wf_Node getNodeById(List<Wf_Node> nodes, String nodeId){
		int nodeCount = nodes.size();
		for(int i=0;i<nodeCount;i++){
			Wf_Node node = nodes.get(i);
			if(node.getId().equals(nodeId)){
				return node;
			}
		}
		return null;
	}
	
	@Override 
	public String saveWorkflow(INcpSession ncpSession, Wf_Workflow newWorkflow) throws Exception{
		Session dbSession = this.getDBSession();
		String workflowId = newWorkflow.getId();
		Data workflowData = DataCollection.getData("wf_Workflow");
		Data nodeData = DataCollection.getData("wf_Node");
		Data linkData = DataCollection.getData("wf_Link");
		 
		//如果isActive，判断流程信息是否完整
		if(newWorkflow.getIsActive()){
			List<String> errors = new ArrayList<String>();
			if(!checkWorkflow(dbSession, newWorkflow, errors)){
				StringBuilder sb= new StringBuilder();
				sb.append("无法启用此流程.");
				for(int i=0;i<errors.size();i++){
					sb.append( "\r\n(" + i + ") " + errors.get(i));
				}
				throw new Exception(sb.toString());
			} 
		}
		
		//保存到数据库
		Transaction trans = null;
		try { 
			trans = dbSession.beginTransaction();
			if(this.checkExistAndLock(workflowId)){ 
				Wf_Workflow oldWorkflow = this.readFromDB(newWorkflow.getId()); 
				
				newWorkflow.updateToDB(ncpSession, dbSession, dBParserAccess);

				List<String> oldNodeIds = new ArrayList<String>();
				List<Wf_Node> oldNodes = oldWorkflow.getNodes();
				for(int i=0;i<oldNodes.size();i++){
					Wf_Node node = oldNodes.get(i);
					oldNodeIds.add(node.getId());
				}
				List<String> oldLinkIds = new ArrayList<String>();
				List<Wf_Link> oldLinks = oldWorkflow.getLinks();
				for(int i=0;i<oldLinks.size();i++){
					Wf_Link link = oldLinks.get(i);
					oldLinkIds.add(link.getId());
				}
				
				List<String> newNodeIds = new ArrayList<String>();
				List<Wf_Node> newNodes = newWorkflow.getNodes();
				for(int i=0;i<newNodes.size();i++){
					Wf_Node node = newNodes.get(i);
					newNodeIds.add(node.getId());
				}
				List<String> newLinkIds = new ArrayList<String>();
				List<Wf_Link> newLinks = newWorkflow.getLinks();
				for(int i=0;i<newLinks.size();i++){
					Wf_Link link = newLinks.get(i);
					newLinkIds.add(link.getId());
				}	

				List<String> deleteNodeIds = new ArrayList<String>();
				List<String> updateNodeIds = new ArrayList<String>();
				List<String> insertNodeIds = new ArrayList<String>();
				for(int i=0;i<oldNodeIds.size();i++){
					String oldNodeId = oldNodeIds.get(i);
					if(!newNodeIds.contains(oldNodeId)){
						deleteNodeIds.add(oldNodeId);
					}
					else{
						updateNodeIds.add(oldNodeId);
					}
				}
				for(int i=0;i<newNodeIds.size();i++){
					String newNodeId = newNodeIds.get(i);
					if(!oldNodeIds.contains(newNodeId)){
						insertNodeIds.add(newNodeId);
					} 
				}
				
				List<String> deleteLinkIds = new ArrayList<String>();
				List<String> updateLinkIds = new ArrayList<String>();
				List<String> insertLinkIds = new ArrayList<String>();
				for(int i=0;i<oldLinkIds.size();i++){
					String oldLinkId = oldLinkIds.get(i);
					if(!newLinkIds.contains(oldLinkId)){
						deleteLinkIds.add(oldLinkId);
					}
					else{
						updateLinkIds.add(oldLinkId);
					}
				}
				for(int i=0;i<newLinkIds.size();i++){
					String newLinkId = newLinkIds.get(i);
					if(!oldLinkIds.contains(newLinkId)){
						insertLinkIds.add(newLinkId);
					} 
				}
				
				//判断已有节点，新流程里不存在了，是否可以删除
				
				//更新
				newWorkflow.updateToDB(ncpSession, dbSession, dBParserAccess);
				for(int i=0;i<deleteNodeIds.size();i++){
					String nodeId = deleteNodeIds.get(i);
					this.dBParserAccess.deleteByData(dbSession, nodeData, nodeId);
				}
				HashMap<String, String> tempNodeIdToNewNodeIds = new HashMap<String, String>();
				for(int i=0;i<updateNodeIds.size();i++){
					String nodeId = updateNodeIds.get(i);
					Wf_Node node = newWorkflow.getNode(nodeId);
					node.updateToDB(dbSession, dBParserAccess, workflowId);
					tempNodeIdToNewNodeIds.put(nodeId, nodeId);
				}				
				for(int i=0;i<insertNodeIds.size();i++){
					String tempNodeId = insertNodeIds.get(i);
					Wf_Node node = newWorkflow.getNode(tempNodeId);
					String newNodeId = node.insertToDB(dbSession, dBParserAccess, workflowId);
					tempNodeIdToNewNodeIds.put(tempNodeId, newNodeId);
				} 
				for(int i=0;i<newLinks.size();i++){
					Wf_Link link = newLinks.get(i);
					String fromNodeId = link.getFromNodeId();
					String toNodeId = link.getToNodeId();
					if(tempNodeIdToNewNodeIds.containsKey(fromNodeId)){
						String newNodeId = tempNodeIdToNewNodeIds.get(fromNodeId);
						link.setFromNodeId(newNodeId);
					}
					if(tempNodeIdToNewNodeIds.containsKey(toNodeId)){
						String newNodeId = tempNodeIdToNewNodeIds.get(toNodeId);
						link.setToNodeId(newNodeId);
					}
				}

				for(int i=0;i<newNodes.size();i++){
					Wf_Node node = newNodes.get(i);
					String groupId = node.getGroupId();
					if(groupId != null && !groupId.isEmpty()){
						String newGroupId = tempNodeIdToNewNodeIds.get(groupId);
						String newNodeId = tempNodeIdToNewNodeIds.get(node.getId());
						HashMap<String, Object> fvs = new HashMap<String, Object>();
						fvs.put("groupid", newGroupId);
						this.dBParserAccess.updateByData(dbSession, nodeData, fvs, newNodeId);
					}
				}
								
				for(int i=0;i<deleteLinkIds.size();i++){
					String linkId = deleteLinkIds.get(i);
					this.dBParserAccess.deleteByData(dbSession, linkData, linkId);
				}
				for(int i=0;i<updateLinkIds.size();i++){
					String linkId = updateLinkIds.get(i);
					Wf_Link link = newWorkflow.getLink(linkId);
					link.updateToDB(dbSession, dBParserAccess, workflowId);
				}
				for(int i=0;i<insertLinkIds.size();i++){
					String tempLinkId = insertLinkIds.get(i);
					Wf_Link link = newWorkflow.getLink(tempLinkId);
					link.insertToDB(dbSession, dBParserAccess, workflowId); 
				}
			}
			else{
				//新建
				workflowId = newWorkflow.insertToDB(ncpSession, dbSession, this.dBParserAccess);

				for(int i=0;i<newWorkflow.getNodes().size();i++){
					Wf_Node node = newWorkflow.getNodes().get(i);
					node.insertToDB(dbSession, dBParserAccess, workflowId);
				}
				for(int i=0;i<newWorkflow.getLinks().size();i++){
					Wf_Link link = newWorkflow.getLinks().get(i);
					link.insertToDB(dbSession, dBParserAccess, workflowId);
				}
			} 
			trans.commit();
		}
		catch(Exception ex){
			if(trans != null){
				trans.rollback();
			}
			throw ex;
		}
		return workflowId;
	} 
	
	private boolean checkExistAndLock(String workflowId) throws SQLException{
		Session dbSession = this.getDBSession();
		String checkSql = "select wf.id as id from wf_workflow wf where wf.id = " +SysConfig.getParamPrefix() + "id and wf.isdeleted = 'N' for update";
		HashMap<String, Object> checkP2vs = new HashMap<String, Object>();
		checkP2vs.put("id", workflowId);
		String id = (String) this.dBParserAccess.getSingleValue(dbSession, checkSql, checkP2vs);
		if(id == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	private Wf_Workflow readFromDB(String workflowId) throws Exception{
		Data workflowData = DataCollection.getData("wf_Workflow");
		Data nodeData = DataCollection.getData("wf_Node");
		Data linkData = DataCollection.getData("wf_Link");
		Session dbSession = this.getDBSession();
		DataTable wfTable = this.dBParserAccess.getDtById(dbSession, workflowData, workflowId);
		List<DataRow> wfRows = wfTable.getRows();
		if(wfRows.size() == 0){
			return null;
		}
		else{
			DataRow wfRow = wfRows.get(0);

			DataTable nodeTable = this.dBParserAccess.getDtByFieldValue(dbSession, nodeData, "workflowid", "=", workflowId);
			DataTable linkTable = this.dBParserAccess.getDtByFieldValue(dbSession, linkData, "workflowid", "=", workflowId);			
			
			Wf_Workflow wf = new Wf_Workflow();
			wf.initByDB(wfRow, nodeTable.getRows(), linkTable.getRows());
			
			return wf;
		}
	}
	
	@Override 
	public Wf_Workflow getWorkflow(String workflowId) throws Exception{
		Wf_Workflow wf = this.readFromDB(workflowId);
		if(wf == null){
			throw new Exception("Can not get workflow object from database。 workflowId = " + workflowId);
		}
		else{
			return wf;
		}
	} 
	
	@Override 
	public void deleteWorkflow(String workflowId) throws Exception{
		Data workflowData = DataCollection.getData("wf_Workflow"); 
		Session dbSession = this.getDBSession();
		Transaction trans = null;
		try { 
			trans = dbSession.beginTransaction();
			
			//判断是否存在流程实例，如果存在那么不允许删除
			
			HashMap<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put("isdeleted", "Y");
			this.dBParserAccess.deleteByData(dbSession, workflowData, workflowId);
			trans.commit();
		}
		catch(Exception ex){
			if(trans !=null){
				trans.rollback();
			}
			throw ex;
		}
	} 
}
