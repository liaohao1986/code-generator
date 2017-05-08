package cn.effine.utils;

public class TypeConvertUtil {

	public static String getType(String type) {
		type = type.toLowerCase();
		if (type.contains("date"))
			return "Date";
		else if (type.contains("text") || type.contains("varchar") || type.contains("longtext"))
			return "String";
		else if (type.contains("decimal"))
			return "BigDecimal";
		else if (type.contains("datetime") || type.contains("date") || type.contains("timestamp"))
			return "Date";
		else if (type.contains("blob"))
			return "BinaryStream";
		else if (type.contains("int"))
			return "Integer";
		else
			return null;
	}
}
