package com.zlp.platform.expression.run;

public interface IExternalBase{
	IDatabaseAccess getDatabaseAccess();

	IDocumentAccess getDocumentAccess();

	ISystemModelAccess getSystemModelAccess();
}
