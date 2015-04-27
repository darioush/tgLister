package edu.washington.cs.tgs;

import java.lang.reflect.Field;
import java.util.List;

public class Utils {

	@SuppressWarnings("unchecked")
	public
	static <T> T getPrivate(Object obj, String field) {
		try {
			Field f = obj.getClass().getDeclaredField(field);
			f.setAccessible(true);
			return (T) f.get(obj);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	public static String join(String string, List<String> pkg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pkg.size(); ++i) {
			sb.append(pkg.get(i));
			if (i + 1 < pkg.size()) {
				sb.append(string);
			}
		}
		return sb.toString();
	}

}
