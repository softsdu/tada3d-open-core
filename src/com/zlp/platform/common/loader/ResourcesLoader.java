package com.zlp.platform.common.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.zlp.platform.common.exception.LoadResourcesException;

public class ResourcesLoader {

	private final static Log logger = LogFactory.getLog(ResourcesLoader.class);

	/**
	 * 版本控制文件
	 */
	private static final String VERSION_FILE = "Module.version.properties";

	private static final String VERSION_KEY = "version";

	// private static final String VERSION_MODULE_NAME_KEY = "moduleName";

	// private static final String VERSION_MODULE_CLASS_KEY = "moduleClass";

	private static String webHome = "";

	private static String moduleName = "platform";

	private static String moduleClass = "com.zlp.platform.version";

	/**
	 * 判断是否要导入资源包
	 * 
	 * @param pWebHome
	 */
	public static void load(final String pWebHome) {
		// if (pWebHome == null) {
		if (true) {
			return;
		}
		//webHome = pWebHome;

		if (isNeedUpdate(moduleName, moduleClass) == false) {
			logger.debug("版本未发生变化，不需要导出" + moduleName + "资源包!");
			return;
		}
		logger.info("开始导出" + moduleName + "资源文件...");
		try {
			loadResource(moduleName, moduleClass);
		} catch (Exception e) {
			logger.info(moduleName + "资源文件不存在。");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		logger.info("导出" + moduleName + "资源文件成功!");
		// 必须在最后导出版本控制文件，这样可以避免导出过程出错，版本不能得到更新的情况.
		exportVersionFile(moduleName, moduleClass);

	}

	/**
	 * 判断是否有需要更新的资源包
	 * 
	 * @param moduleName
	 *            模块名称
	 * @param className
	 *            类路径
	 * @return
	 * @throws LoadResourcesException
	 */
	private static boolean isNeedUpdate(final String moduleName, final String className) throws LoadResourcesException {

		// 获得当前使用的版本号
		String versionFile = getVersionFile(moduleName);
		Integer oldVersionV1 = 0;
		Integer oldVersionV2 = 0;
		Integer oldVersionV3 = 0;

		Integer newVersionV1 = 0;
		Integer newVersionV2 = 0;
		Integer newVersionV3 = 0;

		File oldVersionFile = new File(versionFile);
		if (oldVersionFile.exists() == false) {
			return true;
		}

		// 获得模块最新版本号
		String newVersion = getNewVersion(moduleName, className);
		Properties fileProperties = new Properties();
		InputStream fileIS = null;

		try {
			fileIS = new FileInputStream(versionFile);
			fileProperties.load(fileIS);
		} catch (FileNotFoundException e) {
			final String MSG = "没有发现版本控制文件:" + versionFile;
			logger.error(MSG, e);
			throw new LoadResourcesException(MSG, e);
		} catch (IOException ex) {
			final String MSG = "读版本控制文件:" + versionFile + "失败!";
			logger.error(MSG, ex);
			throw new LoadResourcesException(MSG, ex);
		}

		String strVersion = fileProperties.getProperty(VERSION_KEY);
		if (strVersion == null) {
			return true;
		}

		String[] oldVersionTemp = strVersion.split(".");
		String[] newVersionTemp = newVersion.split(".");

		// 版本只处理3级
		if (oldVersionTemp.length > 3) {
			return true;
		}

		try {
			// 因为是外部版本控制文件，可能被破坏，所以在这做这种异常处理
			oldVersionV1 = Integer.parseInt(oldVersionTemp[0]);
			oldVersionV2 = Integer.parseInt(oldVersionTemp[1]);
			oldVersionV3 = Integer.parseInt(oldVersionTemp[2]);

		} catch (Exception ex) {
			return true;
		}

		logger.debug(moduleName + "模块旧的资源版本号是:" + strVersion);

		newVersionV1 = Integer.parseInt(newVersionTemp[0]);
		newVersionV2 = Integer.parseInt(newVersionTemp[1]);
		newVersionV3 = Integer.parseInt(newVersionTemp[2]);

		if (newVersionV1 > oldVersionV1 || (newVersionV1 == oldVersionV1 && newVersionV2 > oldVersionV2) || (newVersionV1 == oldVersionV1 && newVersionV2 == oldVersionV2 && newVersionV3 > oldVersionV3)) {
			return true;
		}

		return false;
	}

	/**
	 * 获得使用上传包所在的工程下的版本号
	 * 
	 * @param moduleName
	 *            模块名称
	 * @return
	 */
	private static String getVersionFile(final String moduleName) {
		return webHome + moduleName + "-style/version.properties";
	}

	/**
	 * 
	 * 获得最新的版本号
	 * 
	 * @param moduleName
	 *            模块名称
	 * @param className
	 *            类路径
	 * @return 新版本号
	 * @SuppressWarnings("unchecked")
	 */
	private static String getNewVersion(final String moduleName, final String className) throws LoadResourcesException {
		Properties props = new Properties();
		InputStream is = null;
		Class newClass = null;

		try {
			newClass = Class.forName(className + ".Version").newInstance().getClass();
		} catch (Exception e1) {
			return "0";
		}

		try {
			is = newClass.getResourceAsStream(VERSION_FILE);
			props.load(is);
		} catch (IOException e) {
			final String MSG = "读" + moduleName + "模块版本控制文件:" + VERSION_FILE + "失败!";
			logger.error(MSG, e);
			throw new LoadResourcesException(MSG, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.debug("关闭" + moduleName + "模块文件:" + VERSION_FILE + "输入流失败!");
				}
			}
		}

		String version = props.getProperty(VERSION_KEY);
		logger.debug(moduleName + "模块新的资源版本号是:" + version);
		return version;
	}

	private static void loadResource(String moduleName, String className) throws Exception {
		try {
			if (true)
				return;
			Class newClass = Class.forName(className + ".Version").newInstance().getClass();
			// 平台特殊处理
			moduleName = "integration".equals(moduleName) ? "base" : moduleName;

			// String pResourcesStyle = className.replace(".", "/") + "/" +
			// moduleName + "-style.zip";
			// String pResourcesRoot = className.replace(".", "/") + "/" +
			// moduleName + "-root.zip";
			// String pResourcesPages = className.replace(".", "/") + "/" +
			// moduleName + ".zip";
			// 模板文件
			// String pResourcesFtl = className.replace(".", "/") + "/ftl.zip";
			// String pDestPathStyle = webHome + "/" + moduleName + "-style";
			// String pDestPathRootPages = webHome;
			// String pDestPathPages = webHome + "/page";
			// String pDestPathFtl = webHome + "/WEB-INF/ftl";

			// 解压样式
			// InputStream xtreeResouce =
			// newClass.getClassLoader().getResourceAsStream(pResourcesStyle);
			// ZipInputStream zis = new ZipInputStream(xtreeResouce);
			// ZipUtils.unzip(zis, pDestPathStyle);

			// 解压jsp文件
			// xtreeResouce =
			// newClass.getClassLoader().getResourceAsStream(pResourcesPages);
			// zis = new ZipInputStream(xtreeResouce);
			// ZipUtils.unzip(zis, pDestPathPages);

			// 解压模板文件
			// xtreeResouce =
			// newClass.getClassLoader().getResourceAsStream(pResourcesFtl);
			// zis = new ZipInputStream(xtreeResouce);
			// ZipUtils.unzip(zis, pDestPathFtl);

			// xtreeResouce =
			// newClass.getClassLoader().getResourceAsStream(pResourcesRoot);
			// zis = new ZipInputStream(xtreeResouce);
			// ZipUtils.unzip(zis, pDestPathRootPages);

		} catch (LoadResourcesException e) {
			e.printStackTrace();
			final String MSG = "base".equals(moduleName) ? "integration" : moduleName + "模块解压缩资源文件失败!";
			logger.error(MSG, e);
			throw new LoadResourcesException(MSG, e);
		}
	}

	private static void exportVersionFile(final String moduleName, final String className) throws LoadResourcesException {
		if (true)
			return;
		String versionFile = getVersionFile(moduleName);
		Class newClass = null;
		InputStream in = null;
		FileOutputStream fos = null;
		final int cache = 1024;

		try {
			newClass = Class.forName(className + ".Version").newInstance().getClass();
			in = newClass.getResourceAsStream(VERSION_FILE);
			fos = new FileOutputStream(versionFile);

			byte[] b = new byte[cache];
			int aa = 0;

			while ((aa = in.read(b)) != -1) {
				fos.write(b, 0, aa);
			}
		} catch (Exception ex) {
			final String MSG = moduleName + "模块导出版本控制文件:" + versionFile + "失败!";
			logger.warn(MSG);// 下次还会重新释放资源，但是不影响系统正常运行.
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static void main(String[] args) {
		// getNewVersion(moduleName, className)
	}
}
