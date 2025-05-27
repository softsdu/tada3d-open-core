package com.zlp.external.resource.fbxZip;

import java.io.IOException;
import java.util.List;

import org.hibernate.Session;

import com.zlp.external.processor.IResourceFileProcessor;
import com.zlp.platform.common.NcpSession;

public interface IFbxZipBaseProcessor extends IResourceFileProcessor {
    //获取fbx尺寸 added by ls 20230829
	Double[] getFbxSize(String code) throws Exception;
}
