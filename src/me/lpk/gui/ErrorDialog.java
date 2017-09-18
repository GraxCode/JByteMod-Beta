package me.lpk.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class ErrorDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JLabel title;
	private final JTextArea data = new JTextArea("");

	public ErrorDialog() {
		title = new JLabel("", JLabel.CENTER);
		title.setFont(new Font(title.getFont().getName(), Font.BOLD, 22));
		data.setBackground(title.getBackground());
		data.setEditable(false);
		setLayout(new BorderLayout());
		add(title, BorderLayout.NORTH);
		add(data, BorderLayout.CENTER);
		setPreferredSize(new Dimension(600, 500));
		setMinimumSize(getPreferredSize());
	}

	public void setTitlee(String txt) {
		title.setText(txt);
	}

	public void setData(String txt) {
		data.setText(txt);
	}

	public static void show(Exception e) {
		ErrorDialog dialog = new ErrorDialog();
		dialog.setTitle("Error");
		dialog.setTitlee(e.getClass().getSimpleName());
		String pre = "";
		if (e.getMessage() != null){
			pre = e.getMessage() + "\n---------------------------------\n";
		}
		dialog.setData(pre + getLoc(e));
		dialog.setVisible(true);
	}

	private static String getLoc(Exception e) {
		String s =  e.getStackTrace()[0] + "\n";
		for (int i = 1; i < e.getStackTrace().length; i++){
			s += "    " + e.getStackTrace()[i] + "\n";
		}
		return s;
	}
}
