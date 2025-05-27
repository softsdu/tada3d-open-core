package com.zlp.external.resource.gltfZip;

import java.io.IOException;
import java.util.List;

import org.hibernate.Session;

import com.zlp.external.processor.IResourceFileProcessor;
import com.zlp.platform.common.NcpSession;

public interface IGltfZipBaseProcessor extends IResourceFileProcessor {
    //获取gltf尺寸 added by ls 20230829
	Double[] getGltfSize(String code) throws Exception;
}
