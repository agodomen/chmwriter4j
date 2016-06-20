package chm.writer.hh;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import chm.writer.Util;
import chm.writer.entity.*;

/**
 * 索引Keys文件
 * 
 * @author smilethat@qq.com
 */
public class HHK extends HH {
	private String docPath;
	private String tmpFolderName;// 例如"chm_writer_tmp_0.00345"
	/**
	 * 假设docPath="D:/jdocs" beginIndex=docPath.length()+1=9, 换算绝对路径时，
	 * 例如"D:/jdocs/index.html".substring(beginIndex)="index.html"
	 */
	private int beginIndex;

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            hhk文件路径
	 * @param docPath
	 *            Java Doc目录路径
	 */
	public HHK(String path, String docPath) {
		super(path);
		this.docPath = docPath;
		this.tmpFolderName = String.format("chm_writer_tmp_%f", Math.random());
		this.beginIndex = docPath.length() + 1;
		// 创建临时目录
		new File(String.format("%s/%s", this.docPath, this.tmpFolderName))
				.mkdir();
	}

	/**
	 * 解析索引HTML文件,创建entry表,继而创建pair表
	 * 
	 * @return 解析成功返回true,否则返回false
	 * @throws SQLException
	 */
	private boolean parseIndexHtmlFiles() throws SQLException {
		boolean result = false;
		// 从表index_html_file中取索引HTML文件的绝对地址
		@SuppressWarnings("unchecked")
		List<IndexHtmlFile> list = sqlMapClient.queryForList("selectAllIndexHtmlFiles");
		if (list.size() > 0) {
			for (IndexHtmlFile ihf : list) {// 对于每个索引HTML文件
				createEntries(ihf.getPath());// 创建entry表
			}
			createPairs();// 创建pair表
			result = true;
		}
		return result;
	}

	/**
	 * 创建entry表,该方法仅供parseIndexHtmlFiles调用
	 * 
	 * @param indexHtmlFilePath
	 *            索引HTML文件路径
	 */
	private void createEntries(String indexHtmlFilePath) {
		String prefix = "";
		File file = new File(indexHtmlFilePath);
		if (file.getName().equals("index-all.html")) {
			prefix = Util.substring(file.getParent(), beginIndex);
		} else {// index-files/index-1.html
			prefix = Util.substring(file.getParentFile().getParent(),
					beginIndex);
		}
		try {
			Parser parser = new Parser(indexHtmlFilePath);
			parser.setEncoding("gb2312");// 防止中文乱码
			NodeList dts = parser.parse(new TagNameFilter("dt"));// 解析出所有dt标签
			for (int i = 0; i < dts.size(); i++) {
				Entry entry = Entry.parse(dts.elementAt(i), prefix);
				if (entry != null) {
					sqlMapClient.insert("insertEntry", entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建创建pair表,该方法仅供parseIndexHtmlFiles调用
	 * 
	 * @throws SQLException
	 */
	private void createPairs() throws SQLException {
		@SuppressWarnings("unchecked")
		List<Entry> entries = sqlMapClient.queryForList("selectAllEntries");// 排序,直接影响索引的排序
		String name = "";
		List<Entry> list = new ArrayList<Entry>();
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			if (!entry.getName().equals(name)) {
				if (i != 0) {// 不是第一个
					// 可以生成pair了
					sqlMapClient.insert("insertPair", createPair(list));
				}
				// 记录下新的name,并清空list
				name = entry.getName();
				list.clear();
			}
			// 将当前entry加入到list中
			list.add(entry);
			// 最后一个
			if (i == entries.size() - 1) {
				// 补上pair
				sqlMapClient.insert("insertPair", createPair(list));
			}
		}
	}

	/**
	 * 创建一个Pair对象,该方法仅供createPairs调用
	 * 
	 * @param entries
	 *            Entry对象链表
	 * @return 根据Entry对象链表生成的Pair对象
	 * @throws SQLException
	 */
	private Pair createPair(List<Entry> entries) throws SQLException {
		if (entries.size() == 0) {// 链表为空
			return null;
		} else {
			Pair result = new Pair();
			String name = entries.get(0).getName();
			result.setKey(name);// 设置键
			// 设置值
			if (entries.size() == 1) {// 单个Entry
				result.setValue(entries.get(0).getPath());// 设置值
			} else {// 多个Entry,需生成临时html文件
				// 临时html文件的内容content
				StringBuilder content = new StringBuilder();
				content.append(String
						.format("<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/><title>Occurences of %s</title></head>",
								name));
				content.append("<body style='{font-family:Verdana,Arial; font-size:10pt; }'>");
				content.append(String.format("<dl><dt><b>%s</b>:", name));
				for (Entry entry : entries) {
					content.append(String.format(
							"<dd><a href='../%s'>%s</a></dd>", entry.getPath(),
							entry.getCompleteName()));
				}
				content.append("</dl></body></html>");
				// 将内容保存为html文件
				String tmpHtmlFileName = String.format(
						"%s_%f.chm.writer.tmp.html", name, Math.random());// 临时html文件名
				String tmpHtmlFileAbsolutePath = String.format("%s/%s/%s",
						docPath, tmpFolderName, tmpHtmlFileName,
						tmpHtmlFileName);// 临时html文件的绝对路径
				Util.save(content.toString(), tmpHtmlFileAbsolutePath);
				// 新建FilePath对象
				FilePath fp = new FilePath();
				fp.setPath(tmpHtmlFileAbsolutePath.substring(beginIndex));// 临时html文件的绝对路径->相对JavaDoc目录路径
				// 入库
				sqlMapClient.insert("insertFilePath", fp);
				// 设置值
				result.setValue(String.format("%s\\%s", tmpFolderName,
						tmpHtmlFileName));// 绝对路径
			}
			return result;
		}
	}

	@Override
	public boolean generate() {
		boolean result = false;
		if (open()) {
			try {
				if (parseIndexHtmlFiles()) {// 解析索引HTML文件,生成pair表
					// pair表->hhk文件
					@SuppressWarnings("unchecked")
					List<Pair> pairs = sqlMapClient
							.queryForList("selectAllPairs");// 遍历pair表
					// 写入hhk文件内容
					writeLn("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
					writeLn("<HTML><HEAD></HEAD><BODY><UL>");
					// 写入每条键值对
					for (Pair pair : pairs) {
						this.writeLn("<LI><OBJECT type=\"text/sitemap\">");
						this.writeLn(String.format(
								"<param name=\"Name\" value=\"%s\">",
								pair.getKey()));// 键
						this.writeLn(String.format(
								"<param name=\"Local\" value=\"%s\">",
								pair.getValue()));// 值
						this.writeLn("</OBJECT>");
					}
					writeLn("</UL></BODY></HTML>");
					result = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		close();
		return result;
	}

	public void clean() {
		super.clean();
		// 删除临时文件
		Util.delete(String.format("%s/%s", docPath, tmpFolderName));
	}
}
