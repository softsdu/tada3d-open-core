package com.zlp.platform.model.sysmodel.dbStructure;

public enum FieldPropertyType {
	DataType,
	Length,
	FractionLength, 
	Collation,
	KeyType,
	ShowWidth,
	ShowName,
	IsHidden,
	CanQuery,
	CanSortable,
	ShowIndex,
	
	//不可为空 added by ls 20190729
	NotNullable,
	
	//默认值  added by ls 20190729
	DefaultValue,
	
	//注释 added by ls 20190729
	Comment
}
