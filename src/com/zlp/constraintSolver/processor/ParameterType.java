package com.zlp.constraintSolver.processor;

public enum ParameterType{
	STRING,
	DECIMAL,
	BOOLEAN,
	DATE,
	TIME,

	//数组类型 added by liyh 20230606
	ARRAY,

	MATERIAL,
	GLTFFILE,
	BINFILE,
	PATHFILE,
	SURFACEFILE,

	//FBX文件 added by ls 20240112
	FBXFILE,
	
	//辅助点文件 added by ls 20230418
	ASSISTFILE,
	
	//增加参数类型 added by ls 20220607
	POINT2D,
	POINT3D,
	POLYLINE2D,
	POLYLINE3D,
	
	//线参数类型 added by ls 20230613
	LINE2D,
	LINE3D,

	//增加path参数类型 added by ls 20230208
	PATH2D,
	PATH3D,
	PATHCLOSED2D,
	PATHCLOSED3D
};
