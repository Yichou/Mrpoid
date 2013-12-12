package com.mrpoid.mrplist.utils;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class MrpUtils {
	public static String readMrpAppName(String path) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			byte[] buf = new byte[32];
			
			raf.seek(28);
			raf.read(buf, 0, 24);
			buf[24] = 0;
			
			return new String(buf, 0, 24, Charset.forName("GB2312"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (Exception e) {
			}
		}
		
		return null;
	}
}
