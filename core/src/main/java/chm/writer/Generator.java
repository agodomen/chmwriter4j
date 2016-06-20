package chm.writer;

import java.io.File;
import java.io.InputStream;
import chm.writer.hh.HHC;
import chm.writer.hh.HHK;
import chm.writer.hh.HHP;

/**
 * chm文件生成器
 * 
 * @author smilethat@qq.com
 * 
 */
public class Generator {
	private String docPath;
	private String chmFileName;
	private String chmTitle;

	/**
	 * @param docPath
	 *            例如"D:\JavaLib\json-lib\doc"
	 * @param chmFileName
	 *            例如"json-lib.chm"
	 * @param chmTitle
	 *            例如"JSON-lib的API文档"
	 */
	public Generator(String docPath, String chmFileName, String chmTitle) {
		this.docPath = new File(Util.adjustDirectoryPath(docPath))
				.getAbsolutePath();
		this.chmFileName = chmFileName;
		this.chmTitle = chmTitle;
	}

	/**
	 * 调用hhc\hhc.exe
	 * 
	 * @param hhpPath
	 * @return 调用是否成功
	 */
	private boolean callHhcExe(String hhpPath) {
		boolean result = false;
		InputStream in = null;
		if (new File("hhc/hhc.exe").exists()) {// 存在hhc.exe
			try {
				// 调用hhc.exe,命令格式 (hhc\hhc.exe "hhp文件绝对路径")
				// 转Windows路径标识符,/转\
				String command = String.format("hhc\\hhc.exe \"%s\"",
						hhpPath.replace("/", "\\"));
				Process process = Runtime.getRuntime().exec(command);
				in = process.getInputStream();
				while (true) {// 等待hhc.exe进程的结束
					if (in.read() == -1) {
						break;
					}
				}
				result = true;
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
		}
		return result;
	}

	/**
	 * 执行
	 * 
	 * @return 结果,例如"索引文件创建失败","chm文件生成完毕"等等
	 */
	public String execute() {
		if (!Util.validateJavaDocPath(docPath)) {
			return String.format("%s不是一个有效的Java Doc目录", docPath);
		}
		/*
		 * hhk,hhc以及hhp三个文件的生成顺序,必须是: hhc->hhk->hhp, 不能颠倒
		 */
		HHC hhc = null;
		HHK hhk = null;
		HHP hhp = null;
		try {
			// 三个文件的路径
			String hhcPath = String.format("%s/tmp.hhc", docPath);
			String hhkPath = String.format("%s/tmp.hhk", docPath);
			String hhpPath = String.format("%s/tmp.hhp", docPath);
			// hhc
			hhc = new HHC(hhcPath, docPath);
			if (!hhc.generate()) {
				return "hhc文件创建失败!";
			}
			// hhk
			hhk = new HHK(hhkPath, docPath);
			if (!hhk.generate()) {
				return "hhk文件创建失败!";
			}
			// hhp
			hhp = new HHP(hhpPath, chmFileName, chmTitle);
			if (!hhp.generate()) {
				return "hhp文件创建失败!";
			}
			// 调用hhc.exe
			if (!callHhcExe(hhpPath)) {
				return "调用hhc.exe失败!";
			}
			return "chm文件生成完毕!";
		} finally {
			// 清理
			if (hhp != null) {
				hhp.clean();
			}
			if (hhc != null) {
				hhc.clean();
			}
			if (hhk != null) {
				hhk.clean();
			}
		}
	}
}
