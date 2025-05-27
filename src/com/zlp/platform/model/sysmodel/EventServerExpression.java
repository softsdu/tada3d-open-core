package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.List;

import com.zlp.platform.expression.definition.ExpTreePart;
import com.zlp.platform.expression.definition.Parameter;
import com.zlp.platform.expression.definition.RunAt;
import com.zlp.platform.expression.definition.ValidateResult;
import com.zlp.platform.expression.definition.Validator;

public class EventServerExpression extends EventExpression{
	
	//可执行表达式
	private ExpTreePart expTreePart = null;
	public ExpTreePart getExpTreePart() throws Exception {
		if(expTreePart == null){
			String exp = this.getExp();
			String eventCategory = this.getEventCategory();
			String eventName = this.getEventName();
			String eventResultValueType = this.getEventResultValueType();
			String cAndN = eventCategory + "." + eventName;
			Event event = EventCollection.getEvent(cAndN);
			if(event == null){
				throw new RuntimeException("Can not find event named " + cAndN);
			}
			else{
				Validator vali = new Validator();
				List<Parameter> ps = new ArrayList<Parameter>();
			    List<EventParameter> allEventParameters = event.getAllParameters();
			    for(EventParameter ep : allEventParameters){
			    	Parameter p = new Parameter();
			    	p.setName(ep.getName());
			    	p.setValueType(ep.getValueType());
			    	p.setDescription(ep.getDescription());
			    	ps.add(p);
			    }
				ValidateResult vr = vali.validateExp(exp, ps, RunAt.Server, event.getResultValueType());
				if(vr.getSucceed()){
					
				}
			}
		}
		return expTreePart;
	}
	public void setExpTreePart(ExpTreePart expTreePart) {
		this.expTreePart = expTreePart;
	} 
}
