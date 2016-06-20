package chm.writer.hh;

import java.sql.SQLException;
import java.util.List;

import chm.writer.entity.FilePath;

/**
 * 项目Project文件
 * 
 * @author smilethat@qq.com
 */
public class HHP extends HH {
	private String chmPath;
	private String title;

	/**
	 * @param path
	 *            hhp文件路径
	 * @param chmPath
	 *            待生成的chm文件的路径
	 * @param title
	 *            待生成的chm文件的标题
	 */
	public HHP(String path, String chmPath, String title) {
		super(path);
		this.chmPath = chmPath;
		this.title = title;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean generate() {
		boolean result = false;
		if (open()) {
			try {
				// 写[OPTIONS]
				this.writeLn("[OPTIONS]");
				this.writeLn("Compatibility=1.1 or later");// 兼容性：1.1及后继版本
				this.writeLn("Language=0x804 中文(中国)");// 语言:简体中文
				this.writeLn(String.format("Compiled file=%s", chmPath));// 待生成的chm文件的路径
				this.writeLn("Default topic=index.html");// 首页
				this.writeLn(String.format("Title=%s", title));// 标题
				this.writeLn("Contents file=tmp.hhc");// 目录文件
				this.writeLn("Index file=tmp.hhk");// 索引文件
				// 写[FILES]
				this.writeLn("[FILES]");
				// 从数据库的表file中去取
				List<FilePath> files = sqlMapClient
						.queryForList("selectAllFilePaths");
				for (FilePath file : files) {
					this.writeLn(file.getPath());
				}
				close();
				result = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void clean() {
		super.clean();
		// 清空表pair,file_path,index_html_file以及entry,删表的原因在于,同个程序实例,可能要生成多个CHM文档
		try {
			sqlMapClient.delete("deleteAllPairs");
			sqlMapClient.delete("deleteAllFilePaths");
			sqlMapClient.delete("deleteAllIndexHtmlFiles");
			sqlMapClient.delete("deleteAllEntries");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
