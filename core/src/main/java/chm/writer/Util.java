package chm.writer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 * @author smilethat@qq.com
 */
public class Util {
	/**
	 * 获取指定html文件的页面标题
	 * @param file html文件
	 * @param charsetName 字符编码
	 * @return 页面标题
	 */
	public static String getTitle(File file, String charsetName) {
		String result = "";
		BufferedInputStream in = null;// 文件输入流
		try {
			in = new BufferedInputStream(new FileInputStream(file));// 文件->流
			byte[] bytes = new byte[512];
			in.read(bytes);// 流->byte[],只读取前512个字节
			// byte[]->String
			String txt = new String(bytes, charsetName);
			/*
			 * 利用正则表达式查找页面标题(位于<title>标签内) 
			 * 例如,对于<TITLE>JSONString (Overview(json-lib jdk 5 API))</TITLE>, 
			 * 则title="JSONString"
			 */
			Pattern pattern = Pattern.compile("(<title>)([^<(]*)",
					Pattern.CASE_INSENSITIVE);// 不区分大小写
			Matcher matcher = pattern.matcher(txt);// 进行匹配
			if (matcher.find()) {// 找到
				result = matcher.group(2).trim();// group(1)的值为"<title>",group(2)的值为标题,标题要去掉左右空白字符(包括回车换行符)
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * 获取文件后缀名
	 * @param path 文件路径,例如"c:\test.html"
	 * @return 文件后缀(小写),例如".html"
	 */
	public static String getFileExt(String path) {
		int beginIndex = path.lastIndexOf(".");
		if (beginIndex == -1) {// 找不到"."
			return "";
		} else {
			return path.substring(beginIndex).toLowerCase();
		}
	}

	/**
	 * 将字符串保存为文本文件
	 * @param content 内容
	 * @param path 文本文件路径
	 * @return 保存是否成功
	 */
	public static boolean save(String content, String path) {
		boolean result = false;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			out.write(content.getBytes());
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * 校正给定目录路径,去掉尾部的路径分隔符
	 * @param path 待清理的目录路径,例如"d:/doc/"
	 * @return 清理后的目录路径,例如"d:/doc"
	 */
	public static String adjustDirectoryPath(String path) {
		String result = path.trim();
		while (true) {
			if (result.endsWith("/")) {
				result = result.substring(0, result.length() - 1);
				continue;
			} else if (result.endsWith("\\")) {
				result = result.substring(0, result.length() - 1);
				continue;
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * 验证给定的JavaDoc目录是否有效
	 * @param docPath JavaDoc目录,例如"d:/doc"
	 * @return 有效返回true,否则返回false
	 */
	public static boolean validateJavaDocPath(String docPath) {
		boolean result = false;
		File file = new File(docPath);
		if (file.exists() && file.isDirectory()) {// 路径存在并且对应于一个文件夹
			result=true;
		}
		return result;
	}
	
	/**
	 * 删除指定文件或文件夹
	 * @param path 待删除的文件或文件夹的路径
	 */
	public static void delete(String path){
		File file=new File(path);//path->File
		if(file.exists()){//路径有效
			if(file.isDirectory()){//文件夹
				for(File subFile:file.listFiles()){//遍历
					delete(subFile.getAbsolutePath());//递归调用delte
				}
				file.delete();//删除空文件夹
			}else{
				file.delete();//删除文件
			}
		}
	}
	
	/**
	 * 获取子串
	 * @param source 源字符串
	 * @param beginIndex 子串在源字符串中的位置(从0开始)
	 * @return 子串,若beginIndex小于0或大于源字符串长度则返回空串
	 */
	public static String substring(String source,int beginIndex){
		if (beginIndex < 0 || beginIndex > source.length()) {
			return "";
		} else {
			return source.substring(beginIndex);
		}
	}
}
