package com.zlp.platform.asynService;

import java.math.BigDecimal;
import java.util.HashMap;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.expression.run.IExternalBase;

public interface IAsynInvokeServiceProcessor extends IExternalBase {
	void asynInvoke(BigDecimal processCount) throws Exception;

	void checkAsynInvokeStatus(BigDecimal checkCount) throws Exception;

	String createAsynInvoke(String serviceId, String userId, String fromName, String fromId, HashMap<String, ValueType> parameterValueTypes, HashMap<String, Object> parameterValues) throws Exception;

}
