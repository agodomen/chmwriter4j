package chm.writer.entity;

import org.htmlparser.Node;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

/**
 * 条目,用于构建HHK文件
 * 
 * @author smilethat@qq.com
 */
public class Entry {

	private String name;
	private String completeName;
	private String path;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompleteName() {
		return completeName;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param name
	 *            例如"accumulate(String, double)"
	 * @return 例如"accumulate"
	 */
	private static String toName(String name) {
		// 去掉首尾空白字符
		String result = name.trim();
		// 截取"("前的子字符串
		int endIndex = result.indexOf("(");
		if (endIndex > -1) {
			result = result.substring(0, endIndex);
		}
		return result;
	}

	/**
	 * 由超链接生成全名
	 * 
	 * @param href
	 *            例如
	 *            "./net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
	 *            或
	 *            "../net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
	 * @return 例如"net.sf.json.JSONObject.accumulate(java.lang.String, double)"
	 */
	private static String toCompleteName(String href) {
		// 去掉首尾空白字符
		String result = href.trim();
		// 去掉打头的"./"或"../"
		if (result.startsWith("./")) {
			result = result.substring(2);
		} else if (result.startsWith("../")) {
			result = result.substring(3);
		}
		// "/"->"."
		result = result.replace("/", ".");
		// "#"->"."
		result = result.replace("#", ".");
		// 去掉".html"或".htm"
		result = result.replace(".html", "");
		result = result.replace(".htm", "");
		return result;
	}

	/**
	 * 由超链接生成路径
	 * 
	 * @param href
	 *            例如
	 *            "./net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
	 *            或
	 *            "../net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
	 * @param prefix
	 *            前缀,例如""
	 * @return 
	 *         例如"net\sf\json\JSONObject.html#accumulate(java.lang.String,double)
	 *         "
	 */
	private static String toPath(String href, String prefix) {
		// 去掉首尾空白字符
		String result = href.trim();
		// 去掉打头的"./"或"../"
		if (result.startsWith("./")) {
			result = result.substring(2);
		} else if (result.startsWith("../")) {
			result = result.substring(3);
		}
		// "/"->"\"
		result = result.replace("/", "\\");
		if (!prefix.isEmpty()) {
			result = prefix + "\\" + result;
		}
		return result;
	}

	/**
	 * 解析标签dt,返回Entry对象
	 * 
	 * @param dt
	 * @param prefix
	 *            前缀,例如"",用于获取Entry的path值
	 * @return dt对应的Entry对象,解析失败则返回null
	 */
	public static Entry parse(Node dt, String prefix) {
		Entry result = null;
		/*
		 * <DT> <A
		 * HREF="./net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
		 * > <B>accumulate(String, double)</B> </A> - Method in
		 * classnet.sf.json. <A
		 * HREF="./net/sf/json/JSONObject.html"title="class in net.sf.json">
		 * JSONObject </A> </DT>
		 */
		String pts = dt.toPlainTextString().toLowerCase();// 纯文本
		if (pts.indexOf("constructor for class") == -1
				&& pts.indexOf("构造方法") == -1) {// 排除掉构造函数
			NodeList list = dt.getChildren().extractAllNodesThatMatch(
					new TagNameFilter("a"), true);// <a>
			if (list.size() > 0) {
				Node node = list.elementAt(0);
				// <A
				// HREF="./net/sf/json/JSONObject.html#accumulate(java.lang.String, double)">
				// <B>accumulate(String, double)</B>
				// </A>
				LinkTag tag = (LinkTag) node;// <a>
				// href="./net/sf/json/JSONObject.html#accumulate(java.lang.String, double)"
				String href = tag.getAttribute("href");
				// tag.toPlainTextString()="accumulate(String, double)"
				String name = toName(tag.toPlainTextString());// accumulate
				String path = toPath(href, prefix);// "net\sf\json\JSONObject.html#accumulate(java.lang.String,double)"
				String completeName = toCompleteName(href);// "net.sf.json.JSONObject.accumulate(java.lang.String, double)"
				result = new Entry();
				result.setName(name);
				result.setCompleteName(completeName);
				result.setPath(path);
			}
		}
		return result;
	}

	public String toString() {
		return String.format("Entry name:%s,completeName:%s,path:%s", name,
				completeName, path);
	}
}
