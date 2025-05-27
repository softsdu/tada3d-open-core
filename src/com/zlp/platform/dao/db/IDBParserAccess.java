package com.zlp.platform.dao.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 
import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.model.sysmodel.Data;

public interface IDBParserAccess {
	ISequenceGenerator getSequenceGenerator();  
	void setSequenceGenerator(ISequenceGenerator sequenceGenerator);
	int getRecordCountBySqlParser(Session session, SelectSqlParser sqlParser,HashMap<String, Object> p2vs, String joinClause, String where) throws RuntimeException;
	int getRecordCountBySqlParser(Session session, SelectSqlParser sqlParser,HashMap<String, Object> p2vs, String where) throws RuntimeException;
	DataTable getDtBySqlParser(Session session, SelectSqlParser sqlParser, int currentPage, int pageSize, HashMap<String, Object> p2vs, String join, String where, String orderby) throws RuntimeException;
	DataTable getDtBySqlParser(Session session, SelectSqlParser sqlParser, int currentPage, int pageSize, HashMap<String, Object> p2vs,  String where, String orderby) throws RuntimeException;
	SelectSqlParser getSqlParser(String sqlParserName) throws RuntimeException;
	void updateByData(Session session, Data data, JSONObject rowValue, String idValue) throws RuntimeException;
	void insertByData(Session session, Data data, JSONObject rowValue, String idValue) throws RuntimeException;
	List<String> getIds(Session session, String idFieldName, Data data,  String fieldName, String operateStr, Object fieldValue) throws RuntimeException;
	Object getSingleValue(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException;
	List<Object[]> selectList(Session session, String sql) throws RuntimeException;
	List<Object[]> selectList(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException;
	DataTable getMultiLineValues(Session session, String sql, HashMap<String, Object> p2vs, String[] fieldNames, ValueType[] fieldTypes) throws RuntimeException;
	String[] insertByData(Session session, Data data,  List<HashMap<String, Object>> allFieldValues) throws RuntimeException, Exception;
	String insertByData(Session session, Data data, HashMap<String, Object> fieldValues) throws RuntimeException;
	void insertByData(Session session, Data data, HashMap<String, Object> fieldValues, String newIdValue) throws RuntimeException, Exception;
	void updateByData(Session session, Data data, HashMap<String, Object> fieldValues, String idValue) throws RuntimeException;
	void deleteByData(Session session, Data data,  List<String> ids) throws RuntimeException;
	void deleteByData(Session session, Data data,  String id) throws RuntimeException;
	void deleteByData(Session session, Data data, String fieldName, String operateStr, Object fieldValue) throws RuntimeException;
	DataTable getDtByIds(Session session, Data data, List<String> ids ) throws RuntimeException;
	DataTable getDtById(Session session, Data data, String idValue) throws RuntimeException;
	DataTable getDt(Session session, Data data) throws RuntimeException;
	void update(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException;
	void insert(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException;
	void delete(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException;
	DataTable selectList(Session dbSession, String sql, HashMap<String, Object> p2vs, List<String> alias, Map<String, ValueType> fieldValueTypes ) throws SQLException;
	DataTable selectList(Session dbSession, String sql, HashMap<String, Object> p2vs, List<String> alias, Map<String, ValueType> fieldValueTypes, int fromIndex, int onePageRowCount) throws SQLException;
	Object selectOne(Session dbSession, String sql) throws SQLException;
    Object selectOne(Session dbSession, String sql, HashMap<String, Object> p2vs) throws SQLException;
    DataTable getDtByFieldValue(Session session, Data data,  String fieldName, String operateStr, Object fieldValue) throws RuntimeException;
}
