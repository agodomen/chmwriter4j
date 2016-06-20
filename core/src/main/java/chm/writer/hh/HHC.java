package chm.writer.hh;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import chm.writer.Util;
import chm.writer.entity.FilePath;
import chm.writer.entity.IndexHtmlFile;

/**
 * 目录Contents文件
 * 
 * @author smilethat@qq.com
 */
public class HHC extends HH {
	private String docPath;
	/**
	 * 假设docPath="D:/jdocs"<br/>
	 * 则beginIndex=docPath.length()+1=9, 换算绝对路径时，
	 * 例如"D:/jdocs/index.html".substring(beginIndex)="index.html"
	 */
	private int beginIndex;

	/**
	 * 构造函数
	 * @param path
	 *            hhc文件路径
	 * @param docPath
	 *            Java Doc目录
	 */
	public HHC(String path, String docPath) {
		super(path);
		this.docPath = docPath;
		this.beginIndex = this.docPath.length() + 1;
	}

	/**
	 * @param file
	 *            当前考察的文件或目录
	 * @param isRoot
	 *            是否是JavaDoc的根目录
	 * @param session
	 *            Hibernate会话对象
	 * @throws IOException
	 * @throws SQLException 
	 */
	private void parse(File file, boolean isRoot)
			throws IOException, SQLException {
		if (file.isDirectory()) {// 目录
			if (isRoot) {// 根目录
				writeLn("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
				writeLn("<HTML><HEAD></HEAD><BODY>");
				writeLn("<OBJECT type=\"text/site properties\"><param name=\"Window Styles\" value=\"0x800025\"/></OBJECT>");
			} else {// 非根目录
				String name = file.getName();// 目录名,例如"org"
				if (!name.startsWith("chm_writer_tmp")) {// 非临时目录
					writeLn(String
							.format("<LI><OBJECT type=\"text/sitemap\"><param name=\"Name\" value=\"%s\"/></OBJECT></LI>",
									name));
				}
			}
			File[] files = file.listFiles();
			if (files.length > 0) {// 文件夹下有文件
				writeLn("<UL>");
				for (File subFile : files) {// 遍历目录下所有"文件/目录"
					parse(subFile, false);// 递归
				}
				writeLn("</UL>");
			}
			if (isRoot) {
				this.writeLn("</BODY></HTML>");
			}
		} else {// 文件
			String absolutePath = file.getAbsolutePath();// 文件绝对路径
			String fileName = file.getName();// 文件名
			// 判断是否是索引HTML文件
			if (fileName.startsWith("index-")) {// 文件名以"index-"打头
				// 将索引HTML文件路径保存到表index_html_file中,用于hhk文件的生成
				IndexHtmlFile ihf = new IndexHtmlFile();
				ihf.setPath(absolutePath);
				sqlMapClient.insert("insertIndexHtmlFile",ihf);
			}
			String relativePath = file.getAbsolutePath().substring(
					this.beginIndex);// 文件相对于JavaDoc目录的路径
			String fileExt = Util.getFileExt(fileName);// 文件后缀,例如".html"
			if (!relativePath.isEmpty() && !fileExt.equals(".hhc")
					&& !fileExt.equals(".hhk") && !fileExt.equals(".hhp")) {
				// 将文件路径保存到表file中，用于HHP的生成
				FilePath fp = new FilePath();
				fp.setPath(relativePath);
				sqlMapClient.insert("insertFilePath",fp);
				//
				String title = "";
				if (fileExt.equals(".html") || fileExt.equals(".htm")) {// html文件
					title = Util.getTitle(file, "gb2312");// 页面标题
					title = title.trim();
					title = title.replace("\"", "&quot;");// 转引号为"&quot;"
					title = title.replace("\r\n", " ");// 去掉回车换行符
				}
				if (title.isEmpty()) {
					title = fileName;// 文件名
				}
				if (!title.isEmpty()) {// 标题非空
					this.writeLn(String
							.format("<LI><OBJECT type=\"text/sitemap\"><param name=\"Name\" value=\"%s\"/><param name=\"Local\" value=\"%s\"/></OBJECT></LI>",
									title, relativePath));
				}
			}

		}
	}

	@Override
	public boolean generate() {
		boolean result = false;
		if (open()) {
			try {
				parse(new File(docPath), true);// IOException,sqlException
				result = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			close();
		}
		return result;
	}
}
