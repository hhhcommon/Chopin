package com.woting.passport.login.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class GetDataFromRequest {

	public Map<String, Object> getDataFromRequest(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			InputStream in = request.getInputStream();
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] bytes = new byte[1024 * 1024];
			int length = 0;
			String instr = "";
			while ((length = buf.read(bytes)) != -1) {
				instr += new String(bytes, 0, length);
			}
			String[] strs = instr.split("&");
			for (String string : strs) {
				String[] strings = string.split("=");
				if (strings.length == 2) {
					map.put(strings[0], strings[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}