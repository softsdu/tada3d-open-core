package com.zlp.platform.dao.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.service.UserService;

//根据模型中的定义和传递过来的过滤条件、排序条件等，获取数据
public class DBParserAccess implements IDBParserAccess {
	private static Logger logger = Logger.getLogger(UserService.class);

	// sequence生成
	private ISequenceGenerator sequenceGenerator;

	public ISequenceGenerator getSequenceGenerator() {
		return this.sequenceGenerator;
	}

	public void setSequenceGenerator(ISequenceGenerator sequenceGenerator) {
		this.sequenceGenerator = sequenceGenerator;
	}

	public DBParserAccess() {

	}

	public SelectSqlParser getSqlParser(String sqlParserName) throws RuntimeException {
		SelectSqlParser sqlParser = SqlCollection.getSelectObject(sqlParserName);
		if (sqlParser == null) {
			throw new RuntimeException("can not find sql parser named " + sqlParserName + ".");
		} else {
			return sqlParser;
		}
	}

	public void updateByData(Session session, Data data, JSONObject rowValue, String idValue) throws RuntimeException {
		StringBuilder updateSql = new StringBuilder("update " + data.getSaveDest() + " set ");
		int i = 0;
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		for (Object fieldNameObj : rowValue.keySet()) {
			String fieldName = (String) fieldNameObj;
			DataField dataField = data.getDataField(fieldName);
			if (dataField.getIsSave() && !dataField.getName().equals(data.getIdFieldName())) {
				updateSql.append((i != 0 ? ", " : "") + fieldName + " = " + SysConfig.getParamPrefix() + fieldName);
				Object fieldValue = ValueConverter.convertToDBObject(rowValue.getString(fieldName), dataField.getValueType());
				p2vs.put(fieldName, fieldValue);
				i++;
			}
		}
		updateSql.append(" where " + data.getIdFieldName() + " = " + SysConfig.getParamPrefix() + data.getIdFieldName());
		p2vs.put(data.getIdFieldName(), idValue);

		update(session, updateSql.toString(), p2vs);
	}

	public void updateByData(Session session, Data data, HashMap<String, Object> fieldValues, String idValue) throws RuntimeException {
		StringBuilder updateSql = new StringBuilder("update " + data.getSaveDest() + " set ");
		int i = 0;
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		for (String fieldName : fieldValues.keySet()) {
			DataField dataField = data.getDataField(fieldName);
			if (dataField.getIsSave() && !dataField.getName().equals(data.getIdFieldName())) {
				updateSql.append((i != 0 ? ", " : "") + fieldName + " = " + SysConfig.getParamPrefix() + fieldName);
				Object fieldValue = fieldValues.get(fieldName);
				p2vs.put(fieldName, fieldValue);
				i++;
			}
		}
		updateSql.append(" where " + data.getIdFieldName() + " = " + SysConfig.getParamPrefix() + data.getIdFieldName());
		p2vs.put(data.getIdFieldName(), idValue);

		update(session, updateSql.toString(), p2vs);
	}

	public void insertByData(Session session, Data data, JSONObject rowValue, String idValue) throws RuntimeException {
		StringBuilder insertFieldSql = new StringBuilder("insert into " + data.getSaveDest() + "(" + data.getIdFieldName());
		StringBuilder insertValueSql = new StringBuilder("values(" + SysConfig.getParamPrefix() + data.getIdFieldName());
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put(data.getIdFieldName(), idValue);
		for (Object fieldNameObj : rowValue.keySet()) {
			String fieldName = (String) fieldNameObj;
			DataField dataField = data.getDataField(fieldName);
			if (dataField.getIsSave() && !dataField.getName().equals(data.getIdFieldName())) {
				Object fieldValue = ValueConverter.convertToDBObject(rowValue.getString(fieldName), dataField.getValueType());
				if (fieldValue != null) {
					insertFieldSql.append(", " + fieldName);
					insertValueSql.append(", " + SysConfig.getParamPrefix() + fieldName);
					p2vs.put(fieldName, fieldValue);
				}
			}
		}
		String insertSql = insertFieldSql + ") " + insertValueSql + ")";

		insert(session, insertSql.toString(), p2vs);
	}

	public String insertByData(Session session, Data data, HashMap<String, Object> fieldValues) throws RuntimeException {
		String newIdValue = this.getSequenceGenerator().getIdSequence(data.getName(), 1).get(0);
		insertByData(session, data, fieldValues, newIdValue);
		return newIdValue;
	}

	public String[] insertByData(Session session, Data data, List<HashMap<String, Object>> allFieldValues) throws RuntimeException {
		int count = allFieldValues.size();
		List<String> newIdValues = this.getSequenceGenerator().getIdSequence(data.getName(), count);
		String[] ids = new String[newIdValues.size()];
		for (int i = 0; i < count; i++) {
			String newIdValue = newIdValues.get(i);
			HashMap<String, Object> fieldValues = allFieldValues.get(i);
			insertByData(session, data, fieldValues, newIdValue);
			ids[i] = newIdValue;
		}
		return ids;
	}

	public void insertByData(Session session, Data data, HashMap<String, Object> fieldValues, String newIdValue) throws RuntimeException {
		StringBuilder insertFieldSql = new StringBuilder("insert into " + data.getSaveDest() + "(" + data.getIdFieldName());
		StringBuilder insertValueSql = new StringBuilder("values(" + SysConfig.getParamPrefix() + data.getIdFieldName());
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put(data.getIdFieldName(), newIdValue);

		for (String fieldName : fieldValues.keySet()) {
			DataField dataField = data.getDataField(fieldName);
			if(dataField == null){
				//增加字段是否存在的判断 added by ls 20230526
				throw new RuntimeException("Data模型" + data.getName() + "中不存在此字段: " + fieldName);
			}
			else{
				if (dataField.getIsSave() && !dataField.getName().equals(data.getIdFieldName())) {
					Object fieldValue = fieldValues.get(fieldName);
					insertFieldSql.append(", " + fieldName);
					insertValueSql.append(", " + SysConfig.getParamPrefix() + fieldName);
					p2vs.put(fieldName, fieldValue);
				}
			}
		}
		String insertSql = insertFieldSql + ") " + insertValueSql + ")";
		insert(session, insertSql.toString(), p2vs);
	}

	public DataTable getDtByFieldValue(Session session, Data data, String fieldName, String operateStr, Object fieldValue) throws RuntimeException {
		SelectSqlParser sqlParser = data.getDsSqlParser();
		String fieldExpInSql = sqlParser.getSelectExpMap(fieldName);
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put(fieldName, fieldValue);
		DataTable dt = this.getDtBySqlParser(session, sqlParser, -1, -1, p2vs, fieldExpInSql + " " + operateStr + " " + SysConfig.getParamPrefix() + fieldName, "");
		return dt;
	}

	public DataTable getDtById(Session session, Data data, String idValue) throws RuntimeException {
		SelectSqlParser sqlParser = data.getDsSqlParser();
		String idFieldExpInSql = sqlParser.getSelectExpMap(data.getIdFieldName());
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put(data.getIdFieldName(), idValue);
		DataTable dt = this.getDtBySqlParser(session, sqlParser, -1, -1, p2vs, idFieldExpInSql + " = " + SysConfig.getParamPrefix() + data.getIdFieldName(), "");
		return dt;
	}
	
	public DataTable getDt(Session session, Data data) throws RuntimeException {
		SelectSqlParser sqlParser = data.getDsSqlParser();
		DataTable dt = this.getDtBySqlParser(session, sqlParser, -1, -1, null, "", "");
		return dt;
	}

	public DataTable getDtByIds(Session session, Data data, List<String> ids) throws RuntimeException {
		SelectSqlParser sqlParser = data.getDsSqlParser();
		String idFieldExpInSql = sqlParser.getSelectExpMap(data.getIdFieldName());
		DataTable dt = null;
		int rowCount = ids.size();
		StringBuilder idsStr = new StringBuilder();
		for (int i = 0; i < rowCount; i++) {
			idsStr.append((idsStr.length() == 0 ? "'" : ", '") + ids.get(i) + "'");
			// 100个为一组，从数据库中取数
			if ((i != 0 && i % 100 == 0) || (i == rowCount - 1)) {
				String where = idFieldExpInSql + " in (" + idsStr.toString() + ")";
				DataTable partDt = this.getDtBySqlParser(session, sqlParser, -1, -1, null, where, "");
				if (dt == null) {
					dt = partDt;
				} else {
					for (DataRow row : partDt.getRows()) {
						dt.addRow(row);
					}
				}
				idsStr = new StringBuilder();
			}
		}
		return dt;
	}

	public Object getSingleValue(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException {
		try {
			Object resultValue = selectOne(session, sql, p2vs);
			return resultValue;
			/*
			 * NamedParameterSqlProcessor sqlProcessor = new
			 * NamedParameterSqlProcessor(sql, p2vs); Map<String, Object>
			 * results = JdbcUtils.getMap(sqlProcessor.getParsedSql(),
			 * sqlProcessor.getParams()); Object resultValue = results.size()
			 * ==0 ? null : results.values().toArray()[0]; return resultValue;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get single value.\r\n" + ex.getMessage());
		}
	}

	public int getRecordCountBySqlParser(Session session, SelectSqlParser sqlParser, HashMap<String, Object> p2vs, String where) throws RuntimeException {
		return this.getRecordCountBySqlParser(session, sqlParser, p2vs, "", where);
	}

	public int getRecordCountBySqlParser(Session session, SelectSqlParser sqlParser, HashMap<String, Object> p2vs, String joinClause, String where) throws RuntimeException {
		try {
			String sql = sqlParser.getCountSql(joinClause, where);
			int recordCount = Integer.parseInt(selectOne(session, sql, p2vs).toString());
			return recordCount;
			/*
			 * NamedParameterSqlProcessor sqlProcessor = new
			 * NamedParameterSqlProcessor(sqlParser.getCountSql(where), p2vs);
			 * int recordCount = JdbcUtils.count(sqlProcessor.getParsedSql(),
			 * sqlProcessor.getParams()).intValue(); return recordCount;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get datatable record count by SelectSqlParser.\r\n" + ex.getMessage());
		}
	}

	// 删除记录
	public void deleteByData(Session session, Data data, List<String> ids) throws RuntimeException {
		try {
			StringBuilder idsStr = new StringBuilder();
			int rowCount = ids.size();
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			for (int i = 0; i < rowCount; i++) {
				p2vs.put("id" + i, ids.get(i));
				idsStr.append((idsStr.length() == 0 ? "" : ", ") + SysConfig.getParamPrefix() + "id" + i);
				// 100个为一组，从数据库中取数
				if ((i != 0 && i % 100 == 0) || (i == rowCount - 1)) {
					String deleteSql = "delete from " + data.getSaveDest() + " where " + data.getIdFieldName() + " in (" + idsStr.toString() + ")";
					delete(session, deleteSql, p2vs);
					idsStr = new StringBuilder();
					p2vs = new HashMap<String, Object>();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not delete record. data = " + data.getName() + ". \r\n" + ex.getMessage());
		}
	}

	// 删除记录
	public void deleteByData(Session session, Data data, String fieldName, String operateStr, Object fieldValue) throws RuntimeException {
		try {
			String deleteSql = "delete from " + data.getSaveDest() + " where " + fieldName + " " + operateStr + SysConfig.getParamPrefix() + fieldName;
			// SQLQuery deleteQuery = session.createSQLQuery(deleteSql);
			// deleteQuery.setParameter(fieldName, fieldValue);
			// deleteQuery.executeUpdate();
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put(fieldName, fieldValue);
			delete(session, deleteSql, p2vs);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not delete record. data = " + data.getName() + ". \r\n" + ex.getMessage());
		}
	}

	// 删除记录
	public void deleteByData(Session session, Data data, String id) throws RuntimeException {
		try {
			String deleteSql = "delete from " + data.getSaveDest() + " where " + data.getIdFieldName() + " = " + SysConfig.getParamPrefix() + data.getIdFieldName();
			// SQLQuery deleteQuery = session.createSQLQuery(deleteSql);
			// deleteQuery.setParameter(data.getIdFieldName(), id);
			// deleteQuery.executeUpdate();
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put(data.getIdFieldName(), id);
			delete(session, deleteSql, p2vs);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not delete record. data = " + data.getName() + ". \r\n" + ex.getMessage());
		}
	}

	// 执行sql语句
	public void insert(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException {
		try {
			SQLQuery insertQuery = session.createSQLQuery(sql);
			if (p2vs != null) {
				for (String paramName : p2vs.keySet()) {
					insertQuery.setParameter(paramName, p2vs.get(paramName));
				}
			}
			insertQuery.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not insert. sql = " + sql + ". \r\n" + ex.getMessage(), ex);
		}
	}

	// 执行sql语句
	public void update(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException {
		
		if (p2vs != null) {
			Set set = p2vs.keySet();
			Iterator it = set.iterator();
			String s = "";
			while (it.hasNext()) {
				String str = (String) it.next();
				s += str + ",";
			}
			if (s.indexOf("title_rowindex") != -1) {
				if (p2vs.get("title_rowindex") == null) {
					p2vs.put("title_rowindex", "");
				}
			}
			if (s.indexOf("header_rows") != -1) {
				if (p2vs.get("header_rows") == null) {
					p2vs.put("header_rows", "");
				}
			}
		}

		try {
			SQLQuery updateQuery = session.createSQLQuery(sql.toString());
			if (p2vs != null) {
				for (String paramName : p2vs.keySet()) {
					updateQuery.setParameter(paramName, p2vs.get(paramName));
				}
			}
			updateQuery.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not update. sql = " + sql + ". \r\n" + ex.getMessage(), ex);
		}
	}

	// 执行sql语句
	public void delete(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException {
		try {
			SQLQuery deleteQuery = session.createSQLQuery(sql);
			if (p2vs != null) {
				for (String paramName : p2vs.keySet()) {
					deleteQuery.setParameter(paramName, p2vs.get(paramName));
				}
			}
			deleteQuery.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not delete. sql = " + sql + ". \r\n" + ex.getMessage(), ex);
		}
	}

	public List<String> getIds(Session session, String idFieldName, Data data, String fieldName, String operateStr, Object fieldValue) throws RuntimeException {
		try {

			String sql = "select " + idFieldName + " as id from " + data.getName() + " where " + fieldName + " " + operateStr + " " + SysConfig.getParamPrefix() + fieldName;
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put(fieldName, fieldValue);

			List<String> alias = new ArrayList<String>();
			alias.add("id");

			Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
			fieldValueTypes.put("id", ValueType.String);

			DataTable dt = selectList(session, sql, p2vs, alias, fieldValueTypes);
			List<String> ids = new ArrayList<String>();
			for (DataRow r : dt.getRows()) {
				ids.add(r.getStringValue("id"));
			}
			return ids;
			/*
			 * String sql = "select " + idFieldName + " from " + data.getName()
			 * + " where " + fieldName + " " + operateStr + " " +
			 * SysConfig.getParamPrefix() + fieldName; Map<String, Object> p2vs
			 * = new HashMap<String, Object>(); p2vs.put(fieldName, fieldValue);
			 * NamedParameterSqlProcessor sqlProcessor = new
			 * NamedParameterSqlProcessor(sql, p2vs); List<Map<String,Object>>
			 * results = JdbcUtils.queryMap(sqlProcessor.getParsedSql(),
			 * sqlProcessor.getParams()); List<String> ids = new
			 * ArrayList<String>(); for(Map<String,Object> r :results){
			 * ids.add((String)r.get(idFieldName)); } return ids;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get ids.\r\n" + ex.getMessage());
		}
	}

	// 根据select sql配置获取数据
	public DataTable getDtBySqlParser(Session session, SelectSqlParser sqlParser, int currentPage, int pageSize, HashMap<String, Object> p2vs, String where, String orderby) throws RuntimeException {
		return this.getDtBySqlParser(session, sqlParser, currentPage, pageSize, p2vs, "", where, orderby);
	}

	// 根据select sql配置获取数据
	public DataTable getDtBySqlParser(Session session, SelectSqlParser sqlParser, int currentPage, int pageSize, HashMap<String, Object> p2vs, String join, String where, String orderby) throws RuntimeException {
		try {
			String sql = sqlParser.getSql(join, where, orderby);
			int fromIndex = -1;
			int onePageRowCount = 0;
			if (currentPage > 0) {
				fromIndex = (currentPage - 1) * pageSize;
				onePageRowCount = pageSize;
			}
			List<String> alias = sqlParser.getSelectAlias();
			Map<String, ValueType> fieldValueTypes = sqlParser.getFieldTypeMaps();

			DataTable dt = selectList(session, sql, p2vs, alias, fieldValueTypes, fromIndex, onePageRowCount);

			return dt;
			/*
			 * String sql = sqlParser.getSql(where,orderby);
			 * NamedParameterSqlProcessor sqlProcessor = new
			 * NamedParameterSqlProcessor(sql, p2vs); List<Map<String, Object>>
			 * results = (pageSize == -1)
			 * ?JdbcUtils.queryMap(sqlProcessor.getParsedSql(),
			 * sqlProcessor.getParams())
			 * :JdbcUtils.query_limit_map(sqlProcessor.getParsedSql(), pageSize,
			 * currentPage, sqlProcessor.getParams()); List<String> alias =
			 * sqlParser.getSelectAlias();
			 * 
			 * DataTable dt = new DataTable(); for(String f : alias){
			 * dt.setField(f, sqlParser.getFieldTypeMap(f)); }
			 * 
			 * for(Map<String, Object> r :results){ DataRow row = new DataRow();
			 * for(int i=0;i<r.size();i++){ row.setValue(alias.get(i),
			 * r.get(alias.get(i))); } dt.addRow(row); } return dt;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get datatable by SelectSqlParser.\r\n" + ex.getMessage());
		}
	}

	// 根据select sql配置获取数据
	public List<Object[]> selectList(Session session, String sql, HashMap<String, Object> p2vs) throws RuntimeException {
		try {
			SQLQuery query = session.createSQLQuery(sql);
			if (p2vs != null) {
				for (String paramName : p2vs.keySet()) {
					query.setParameter(paramName, p2vs.get(paramName));
				}
			}
			List<Object[]> results = query.list();
			return results;
			/*
			 * NamedParameterSqlProcessor sqlPcr = new
			 * NamedParameterSqlProcessor(sql, p2vs); List<Map<String, Object>>
			 * results = JdbcUtils.queryMap(sqlPcr.getParsedSql(),
			 * sqlPcr.getParams()); return results;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get multi line values.\r\n" + ex.getMessage());
		}
	}

	// 根据select sql配置获取数据
	public List<Object[]> selectList(Session session, String sql) throws RuntimeException {
		try {
			SQLQuery query = session.createSQLQuery(sql);
			List<Object[]> results = query.list();
			return results;
			/*
			 * NamedParameterSqlProcessor sqlPcr = new
			 * NamedParameterSqlProcessor(sql, p2vs); List<Map<String, Object>>
			 * results = JdbcUtils.queryMap(sqlPcr.getParsedSql(),
			 * sqlPcr.getParams()); return results;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("can not get multi line values.\r\n" + ex.getMessage());
		}
	}

	public DataTable getMultiLineValues(Session session, String sql, HashMap<String, Object> p2vs, String[] fieldNames, ValueType[] fieldTypes) throws RuntimeException {

		List<String> alias = new ArrayList<String>();
		Map<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		for (int i = 0; i < fieldNames.length; i++) {
			alias.add(fieldNames[i]);
			fieldValueTypes.put(fieldNames[i], fieldTypes[i]);
		}
		fieldValueTypes.put("id", ValueType.String);

		DataTable dt = selectList(session, sql, p2vs, alias, fieldValueTypes);

		return dt;
		/*
		 * List<Map<String, Object>> results = getMultiLineValues(session, sql,
		 * p2vs);
		 * 
		 * DataTable dt = new DataTable(); for(int i=0;i<fieldNames.length;i++){
		 * dt.setField(fieldNames[i], fieldTypes[i]); }
		 * 
		 * for(Map<String, Object> r :results){ DataRow row = new DataRow();
		 * for(int i=0;i<r.size();i++){
		 * row.setValue(fieldNames[i],r.get(fieldNames[i])); } dt.addRow(row); }
		 * return dt;
		 */
	}

	public DataTable selectList(Session dbSession, String sql, HashMap<String, Object> p2vs, List<String> alias, Map<String, ValueType> fieldValueTypes) throws RuntimeException {
		SQLQuery query = dbSession.createSQLQuery(sql);

		if (p2vs != null) {
			for (String paramName : p2vs.keySet()) {
				query.setParameter(paramName, p2vs.get(paramName));
			}
		}

		DataTable dt = new DataTable();
		for (String f : alias) {
			dt.setField(f, fieldValueTypes.get(f));
		}

		List<Object[]> results = query.list();
		if (alias.size() == 1) {
			for (Object r : results) {
				DataRow row = new DataRow();
				row.setValue(alias.get(0), r);
				dt.addRow(row);
			}
		} else {
			for (Object[] r : results) {
				DataRow row = new DataRow();
				for (int i = 0; i < r.length; i++) {
					row.setValue(alias.get(i), r[i]);
				}
				dt.addRow(row);
			}
		}
		return dt;
	}

	public DataTable selectList(Session dbSession, String sql, HashMap<String, Object> p2vs, List<String> alias, Map<String, ValueType> fieldValueTypes, int fromIndex, int onePageRowCount) throws SQLException {
		SQLQuery query = dbSession.createSQLQuery(sql);

		if (fromIndex >= 0) {
			query.setFirstResult(fromIndex);
			query.setMaxResults(onePageRowCount);
		}

		if (p2vs != null) {
			for (String paramName : p2vs.keySet()) {
				query.setParameter(paramName, p2vs.get(paramName));
			}
		}

		DataTable dt = new DataTable();
		for (String f : alias) {
			dt.setField(f, fieldValueTypes.get(f));
		}

		List<Object[]> results = query.list();
		if (alias.size() == 1) {
			for (Object r : results) {
				DataRow row = new DataRow();
				row.setValue(alias.get(0), r);
				dt.addRow(row);
			}
		} else {
			for (Object[] r : results) {
				DataRow row = new DataRow();
				for (int i = 0; i < r.length && i < alias.size(); i++) {
					row.setValue(alias.get(i), r[i]);
				}
				dt.addRow(row);
			}
		}
		return dt;
	}

	public Object selectOne(Session dbSession, String sql) throws SQLException {
		SQLQuery query = dbSession.createSQLQuery(sql);
		List<Object> results = query.list();
		Object resultValue = results.size() == 0 ? null : results.get(0);
		return resultValue;
	}

	public Object selectOne(Session dbSession, String sql, HashMap<String, Object> p2vs) throws SQLException {

		SQLQuery query = dbSession.createSQLQuery(sql);
		if (p2vs != null) {
			for (String paramName : p2vs.keySet()) {
				query.setParameter(paramName, p2vs.get(paramName));
			}
		}
		List<Object> results = query.list();
		Object resultValue = results.size() == 0 ? null : results.get(0);
		return resultValue;
	}
}
