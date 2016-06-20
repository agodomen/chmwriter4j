package chm.writer;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

public class MainForm extends JFrame {
	private static final long serialVersionUID = 8915585356058354140L;
	private Timer timer;// 定时器,用于控制进度条

	public MainForm() {
		initComponents();
		// 新建一个DropTargetListener对象(匿名类),用于拖放文件/文件夹
		DropTargetListener dtl = new DropTargetListener() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				Transferable transferable = dtde.getTransferable();
				for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
					if (flavor.equals(DataFlavor.javaFileListFlavor)) {
						try {
							@SuppressWarnings("unchecked")
							List<File> fileList = (List<File>) transferable
									.getTransferData(flavor);
							if (fileList.size() > 0) {
								// 取第一个
								File file = fileList.get(0);
								if (file.isDirectory()) {// 目录
									tfDocPath.setText(file.getAbsolutePath());
								} else {// 文件
									tfDocPath.setText(file.getParent());
								}
							}
						} catch (UnsupportedFlavorException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				dtde.dropComplete(true);
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}
		};
		// 让主窗体支持文件拖放
		new DropTarget(this, dtl);
		// 让文本域tfDocPath支持文件拖放
		new DropTarget(this.tfDocPath, dtl);
		// 设计定时器,控制进度条
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JProgressBar pb = MainForm.this.pb;
				pb.setValue((pb.getValue() + 1) % pb.getMaximum());
			}
		};
		timer = new Timer(1000, listener);// 1秒
	}

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		// 设置swing皮肤,Nimbus(光轮)
		UIManager
				.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		MainForm form = new MainForm();
		form.setVisible(true);
	}

	/**
	 * 按钮"生成"点击时
	 * @param e
	 */
	private void btnGenerateActionPerformed(ActionEvent e) {
		// SwingWorker,Swing下的多线程
		new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				// 确保选择了Java Doc目录
				String docPath = MainForm.this.tfDocPath.getText().trim();
				if (docPath.isEmpty()) {
					JOptionPane.showMessageDialog(MainForm.this,
							"您还未选择Java Doc目录,例如d:\\dom4j-1.6.1\\docs");
					MainForm.this.tfDocPath.grabFocus();
					return null;
				}
				// 确保输入了chm文件名
				String chmFileName = MainForm.this.tfCHMFileName.getText()
						.trim();
				if (chmFileName.isEmpty()) {
					JOptionPane.showMessageDialog(MainForm.this, "chm文件名不能为空");
					MainForm.this.tfCHMFileName.grabFocus();
					return null;
				}
				chmFileName += ".chm";// 加文件后缀
				String chmTitle = MainForm.this.tfCHMTitle.getText();
				//
				MainForm.this.btnGenerate.setEnabled(false);
				MainForm.this.btnSelectDocPath.setEnabled(false);
				MainForm.this.tfCHMFileName.setEnabled(false);
				MainForm.this.tfCHMTitle.setEnabled(false);
				MainForm.this.tfDocPath.setEnabled(false);
				//
				MainForm.this.timer.start();
				Generator generator = new Generator(docPath, chmFileName,
						chmTitle);
				String result = generator.execute();
				MainForm.this.timer.stop();
				MainForm.this.pb.setValue(0);
				//
				MainForm.this.btnGenerate.setEnabled(true);
				MainForm.this.btnSelectDocPath.setEnabled(true);
				MainForm.this.tfCHMFileName.setEnabled(true);
				MainForm.this.tfCHMTitle.setEnabled(true);
				MainForm.this.tfDocPath.setEnabled(true);
				//
				JOptionPane.showMessageDialog(MainForm.this, result);
				return null;
			}
		}.execute();
	}

	/**
	 * 当tfCHMFileName文本框中内容发生变化时
	 * 
	 * @param e
	 */
	private void tfCHMFileNameCaretUpdate(CaretEvent e) {
		// tfCHMTitle中文本与tfCHMFileName一致
		this.tfCHMTitle.setText(tfCHMFileName.getText());
	}

	/**
	 * 按钮"选择"点击时
	 * 
	 * @param e
	 */
	private void btnSelectDocPathActionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();// 文件选择器
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设置只选择目录
		chooser.setDialogTitle("选择Java Doc目录");// 设置对话框标题
		// 弹出对话框,让用户选择Java Doc目录
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();// 绝对路径
			this.tfDocPath.setText(path);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		label1 = new JLabel();
		tfDocPath = new JTextField();
		btnSelectDocPath = new JButton();
		label2 = new JLabel();
		tfCHMFileName = new JTextField();
		label3 = new JLabel();
		label4 = new JLabel();
		tfCHMTitle = new JTextField();
		btnGenerate = new JButton();
		pb = new JProgressBar();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();

		//======== this ========
		setTitle("CHMWriter 3.0 -by smilethat@qq.com");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setIconImage(null);
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		//---- label1 ----
		label1.setText("Java Doc\u76ee\u5f55");
		contentPane.add(label1);
		label1.setBounds(new Rectangle(new Point(10, 15), label1.getPreferredSize()));

		//---- tfDocPath ----
		tfDocPath.setForeground(Color.blue);
		contentPane.add(tfDocPath);
		tfDocPath.setBounds(106, 9, 359, tfDocPath.getPreferredSize().height);

		//---- btnSelectDocPath ----
		btnSelectDocPath.setText("\u9009\u62e9");
		btnSelectDocPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnSelectDocPathActionPerformed(e);
			}
		});
		contentPane.add(btnSelectDocPath);
		btnSelectDocPath.setBounds(new Rectangle(new Point(470, 9), btnSelectDocPath.getPreferredSize()));

		//---- label2 ----
		label2.setText("chm\u6587\u4ef6\u540d");
		contentPane.add(label2);
		label2.setBounds(new Rectangle(new Point(10, 52), label2.getPreferredSize()));

		//---- tfCHMFileName ----
		tfCHMFileName.setForeground(Color.blue);
		tfCHMFileName.setText("API");
		tfCHMFileName.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				tfCHMFileNameCaretUpdate(e);
			}
		});
		contentPane.add(tfCHMFileName);
		tfCHMFileName.setBounds(106, 46, 190, tfCHMFileName.getPreferredSize().height);

		//---- label3 ----
		label3.setText(".chm");
		label3.setForeground(Color.blue);
		contentPane.add(label3);
		label3.setBounds(new Rectangle(new Point(300, 55), label3.getPreferredSize()));

		//---- label4 ----
		label4.setText("chm\u6587\u4ef6\u6807\u9898");
		contentPane.add(label4);
		label4.setBounds(new Rectangle(new Point(10, 89), label4.getPreferredSize()));

		//---- tfCHMTitle ----
		tfCHMTitle.setForeground(Color.blue);
		tfCHMTitle.setText("API");
		contentPane.add(tfCHMTitle);
		tfCHMTitle.setBounds(106, 83, 190, tfCHMTitle.getPreferredSize().height);

		//---- btnGenerate ----
		btnGenerate.setText("\u751f\u6210");
		btnGenerate.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 30));
		btnGenerate.setForeground(Color.red);
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnGenerateActionPerformed(e);
			}
		});
		contentPane.add(btnGenerate);
		btnGenerate.setBounds(330, 45, 192, 65);
		contentPane.add(pb);
		pb.setBounds(0, 120, 522, pb.getPreferredSize().height);

		//======== scrollPane1 ========
		{

			//---- textArea1 ----
			textArea1.setText("1.\u5de5\u5177\u53ea\u80fd\u8dd1\u5728Windows\u7cfb\u5217\u64cd\u4f5c\u7cfb\u7edf\u4e0a(\u56e0\u4e3a\u8981\u8c03\u7528hhc.exe\u8fd9\u4e2achm\u7f16\u8bd1\u7a0b\u5e8f).\n2.\u5728\u751f\u6210chm\u6587\u4ef6\u65f6,\u5de5\u5177\u4f1a\u5728Java Doc\u76ee\u5f55\u4e0b\u4ea7\u751f\u4e00\u4e9b\u4e34\u65f6\u6587\u4ef6,\u8fd0\u884c\u5b8c\u6bd5\u540e\u4f1a\u81ea\u52a8\u5220\u9664.\n3.\u6700\u540e\u751f\u6210\u7684chm\u6587\u4ef6\u5c06\u5b58\u50a8\u5728Java Doc\u76ee\u5f55\u4e0b.\n4.\u5de5\u5177\u652f\u6301\u6587\u4ef6/\u6587\u4ef6\u5939\u62d6\u653e.\n5.Java Doc\u76ee\u5f55\u8f83\u5e9e\u5927\u65f6,\u4f1a\u9700\u8981\u4e00\u6bb5\u8f83\u957f\u7684\u65f6\u95f4,\u8bf7\u8010\u5fc3\u7b49\u5f85.\n6.\u5de5\u5177\u4e0d\u4ec5\u652f\u6301jdk\u7684doc,\u5176\u4ed6Java\u9879\u76ee\u751f\u6210\u7684doc\u6587\u6863\u90fd\u80fd\u5f88\u597d\u7684\u652f\u6301.");
			textArea1.setForeground(new Color(51, 102, 0));
			textArea1.setEditable(false);
			textArea1.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 12));
			scrollPane1.setViewportView(textArea1);
		}
		contentPane.add(scrollPane1);
		scrollPane1.setBounds(0, 145, 522, 115);

		{ // compute preferred size
			Dimension preferredSize = new Dimension();
			for(int i = 0; i < contentPane.getComponentCount(); i++) {
				Rectangle bounds = contentPane.getComponent(i).getBounds();
				preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
				preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
			}
			Insets insets = contentPane.getInsets();
			preferredSize.width += insets.right;
			preferredSize.height += insets.bottom;
			contentPane.setMinimumSize(preferredSize);
			contentPane.setPreferredSize(preferredSize);
		}
		setSize(530, 300);
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel label1;
	private JTextField tfDocPath;
	private JButton btnSelectDocPath;
	private JLabel label2;
	private JTextField tfCHMFileName;
	private JLabel label3;
	private JLabel label4;
	private JTextField tfCHMTitle;
	private JButton btnGenerate;
	private JProgressBar pb;
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
