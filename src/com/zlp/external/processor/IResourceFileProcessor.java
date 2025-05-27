package com.zlp.external.processor;

import org.hibernate.Session;
import com.zlp.platform.common.NcpSession;

public interface IResourceFileProcessor {

	void setDBSession(Session dbSession);
}
