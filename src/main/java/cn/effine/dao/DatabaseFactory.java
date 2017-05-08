package cn.effine.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.effine.model.Column;
import cn.effine.model.Table;
import cn.effine.utils.TypeConvertUtil;

/**
 * 数据库操作工厂
 */
public class DatabaseFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseFactory.class);

	public static Connection connection = null;
	public static Statement statement = null;

	/**
	 * 获取数据库连接
	 *
	 * @param type
	 *            数据库类型
	 * @param url
	 *            数据库地址URL
	 * @param port
	 *            数据库端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 */
	private static void getConnection(String type, String url, Integer port, String username, String password, String dbname) {
		// 驱动程序名
		String driver = null;
		StringBuilder urlstart = new StringBuilder();

		// mysql数据库
		if (type.equals("mysql")) {
			driver = "com.mysql.jdbc.Driver";
			urlstart.append("jdbc:mysql://");
			urlstart.append(url);
			urlstart.append(":");
			urlstart.append(port);
			urlstart.append("/");
			urlstart.append(dbname);
			urlstart.append("?characterEncoding=UTF-8");
		}
		try {
			Class.forName(driver); // 加载驱动程序
			connection = DriverManager.getConnection(urlstart.toString(), username, password); // 连续数据库
			if (!connection.isClosed()) {
				statement = connection.createStatement(); // statement用来执行SQL语句
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取数据库所有表名列表
	 *
	 * @param dbname
	 *            数据库名
	 * @return 数据库表名列表
	 */
	public static List<String> getTableList(String dbname) {
		getConnection("mysql", "202.103.25.41", 3306, "root", "zencat", dbname);
		String tableName = null;
		ResultSet resultSet = null;
		List<String> tableList = new ArrayList<String>();
		try {
			statement.executeQuery("use " + dbname);
			resultSet = statement.executeQuery("show tables");
			while (resultSet.next()) {
				// TODO 获取表的默认字符集
				// String defaultChartset = resultSet.getNString("charsetName");
				tableName = resultSet.getNString(1);
				tableName = new String(tableName.getBytes("ISO-8859-1"), "utf-8");
				tableList.add(tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != resultSet) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return tableList;
	}

	/**
	 * 获取数据库表的列
	 *
	 * @param tableNameList
	 *            数据库表名列表
	 * @return
	 */
	public static List<Table> getColumnByTableList(List<String> tableNameList) {
		List<Table> tableList = new ArrayList<Table>(tableNameList.size());
		for (String tableName : tableNameList) {
			tableList.add(getColumnByOneTable(tableName));
		}
		return tableList;
	}

	// public static List<String> getDatabase() {
	// List<String> list = new ArrayList<String>();
	// try {
	// String sql = "SHOW databases";
	// ResultSet rs = statement.executeQuery(sql);
	// String name = null;
	// int order = 1;
	// while (rs.next()) {
	// name = rs.getNString(order);
	// name = new String(name.getBytes("ISO-8859-1"), "utf-8");
	// list.add(name);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return list;
	// }

	@SuppressWarnings("unused")
	public static Table getColumnByOneTable(String tableName) {
		getConnection("mysql", "202.103.25.41", 3306, "root", "zencat", "checkin");
		Table table = null;
		try {
			String sql = "show full columns from " + tableName;
			ResultSet rs = statement.executeQuery(sql);
			int length = 0;
			table = getTableName(tableName);

			List<Column> list = new ArrayList<Column>();
			while (rs.next()) {
				String columnField = null;
				String columnType = null;
				columnField = rs.getString("field");
				columnType = rs.getString("type");
				String pk = rs.getString("key");
				length = getLength(length, columnType);
				columnField = new String(columnField.getBytes("ISO-8859-1"), "GB2312");
				columnType = new String(columnType.getBytes("ISO-8859-1"), "GB2312");
				Column column = new Column();
				column.setType(TypeConvertUtil.getType(columnType));
				column.setName(getCloumnName(columnField));
				column.setColumn(getCloumnName(columnField));
				column.setLength(length);
				column.setRemark(rs.getString("comment"));
				list.add(column);
			}
			table.setPropertyList(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private static int getLength(int length, String column_type) {
		if (column_type.contains(",")) {
			length = Integer.parseInt(column_type.substring(column_type.indexOf("(") + 1, column_type.indexOf(",")));
		} else if (column_type.contains("(")) {
			length = Integer.parseInt(column_type.substring(column_type.indexOf("(") + 1, column_type.indexOf(")")));
		}
		return length;
	}

	public static Table getTableName(String dbTableName) {
		Table table = new Table();
		String[] spTbName = dbTableName.split("_");
		table.setTableName(dbTableName);
		table.setPackageName(spTbName[0]);
		table.setModelName(getModelName(dbTableName));
		return table;
	}

	/**
	 * 将名称首字符大写
	 *
	 * @param name
	 * @return
	 */
	private static String getModelName(String name) {
		return getCamelName(name, 0);
	}

	/**
	 * 将名称首字符大写
	 *
	 * @param name
	 * @return
	 */
	private static String getCloumnName(String name) {
		return getCamelName(name, 1);
	}

	/**
	 * 按照驼峰命名法获取类名或者字段名称
	 *
	 * @param name
	 *            类名或者字段名
	 * @param type
	 *            (0:类名 1:字段)
	 * @return
	 */
	private static String getCamelName(String name, int type) {
		StringBuilder buffer = new StringBuilder("");
		for (int index = 0; index < name.length(); index++) {
			String charStr = name.substring(index, index + 1);
			if (index == 0 && type == 0) {
				charStr = charStr.toUpperCase();
			}
			if ("_".equals(charStr)) {
				charStr = name.substring((++index), index + 1).toUpperCase();
			}
			buffer.append(charStr);
		}
		return buffer.toString();
	}
}
