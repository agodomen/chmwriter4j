package chm.writer.hh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;

import chm.writer.Util;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * 类HHC,HHK以及HHP的抽象父类<br/>
 * 表示一文本文件,提供写,删除文件的操作<br/>
 * 
 * @author smilethat@qq.com
 */
public abstract class HH {
	/**
	 * 文件路径
	 */
	private String path;
	/**
	 * 文件输出流,用于往文件中写内容
	 */
	private FileOutputStream out;

	protected static SqlMapClient sqlMapClient;

	static {
		try {
			Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            文件路径
	 */
	public HH(String path) {
		this.path = path;

	}

	/**
	 * 抽象方法,生成文件
	 * 
	 * @return 生成是否成功
	 */
	public abstract boolean generate();

	/**
	 * 往文件中写入一字符串,并换行
	 * 
	 * @param text
	 *            待写入的字符串
	 */
	protected void writeLn(String text) {
		if (out != null) {
			text += "\n";// 换行
			try {
				out.write(text.getBytes("gb2312"));// ANSI
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 打开文件,准备写入文本
	 * 
	 * @return 打开是否成功
	 */
	protected boolean open() {
		boolean result = false;
		try {
			out = new FileOutputStream(new File(path));
			result = true;
		} catch (IOException e) {
		}
		return result;
	}

	/**
	 * 关闭文件
	 */
	protected void close() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 删除对应的文件
	 */
	public void clean() {
		Util.delete(path);
	}
}
