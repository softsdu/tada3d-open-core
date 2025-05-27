package com.zlp.platform.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style=Style.RPC)
public interface IDataQueryService extends IServiceInterface  { 
	@WebMethod
	public String getTableData(@WebParam(name = "requestParam") String requetParam) throws Exception; 
}
