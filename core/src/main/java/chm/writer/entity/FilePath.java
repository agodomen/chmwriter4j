package chm.writer.entity;

/**
 * 文件路径,用于构建HHP文件
 * 
 * @author smilethat@qq.com
 */
public class FilePath {
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String toString() {
		return String.format("FilePath path:%s", path);
	}
}
