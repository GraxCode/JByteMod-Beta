package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.asm.Opcodes;

public class JAccessHelper extends JFrame {
	private JTextField jtf;

	public JAccessHelper() {
		setBounds(100, 100, 420, 220);
		setResizable(false);
		setTitle("Access Helper");
		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout(10, 10));
		JPanel access = new JPanel();
		access.setLayout(new GridLayout(5, 4, 5, 5));
		try {
			for (Field d : Opcodes.class.getDeclaredFields()) {
				if (d.getName().startsWith("ACC_")) {
					int acc = d.getInt(null);

					JCheckBox jcb = new JAccCheckBox(d.getName().substring(4).toLowerCase(), acc);
					access.add(jcb);
					jcb.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int acc = 0;
							for (Component c : access.getComponents()) {
								JAccCheckBox jacb = (JAccCheckBox) c;
								if (jacb.isSelected())
									acc |= jacb.getAccess();
							}
							jtf.setText("" + acc);
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cp.add(access, BorderLayout.CENTER);
		jtf = new JTextField();
		jtf.setEditable(false);
		jtf.setHorizontalAlignment(JTextField.CENTER);
		cp.add(jtf, BorderLayout.SOUTH);
		this.add(cp);
	}

	public static void main(String[] args) {
		new JAccessHelper().setVisible(true);
	}

	private class JAccCheckBox extends JCheckBox {
		private int access;

		public JAccCheckBox(String name, int access) {
			super(name);
			this.access = access;
		}

		public int getAccess() {
			return access;
		}

	}
}
