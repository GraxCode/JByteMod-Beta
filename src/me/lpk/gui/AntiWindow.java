package me.lpk.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.objectweb.asm.tree.ClassNode;

import me.lpk.antis.AntiBase;
import me.lpk.antis.impl.AntiAllatori;
import me.lpk.antis.impl.AntiDashO;
import me.lpk.antis.impl.AntiStringer;
import me.lpk.antis.impl.AntiVertex;
import me.lpk.antis.impl.AntiZKM5;
import me.lpk.antis.impl.AntiZKM8;
import me.lpk.log.Logger;
import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappingProcessor;
import me.lpk.util.JarUtils;
import me.lpk.util.Setup;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AntiWindow {

	private JFrame frame;
	private JList<String> list;
	private JComboBox<String> comboObfuscator;
	private Set<File> libraries = new HashSet<File>();
	private final static String OB_ZKM5 = "ZKM-5", OB_ZKM8 = "ZKM-8", OB_STRING = "Stringer", OB_STRING_P = "Stringer (Call Proxy x2)", OB_DASH = "DashO", OB_ALLA = "Allatori",
			OB_ALLA_P = "Allatori (Call Proxy x1)", OB_VERTEX = "VertexCode";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AntiWindow window = new AntiWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AntiWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		comboObfuscator = new JComboBox<String>();
		// OB_ALLA, OB_ALLA_P, OB_DASH, OB_STRING, OB_STRING_P, OB_ZKM5, OB_ZKM8
		comboObfuscator.setModel(new DefaultComboBoxModel<String>(new String[] {  OB_ALLA, OB_ALLA_P, OB_DASH, OB_STRING, OB_STRING_P, OB_ZKM5, OB_ZKM8, OB_VERTEX }));
		panel_1.add(comboObfuscator);

		JLabel lblObfuscatorUsed = new JLabel("Obfuscator Used:  ");
		panel_1.add(lblObfuscatorUsed, BorderLayout.WEST);

		JPanel pnlDropAreas = new JPanel();
		frame.getContentPane().add(pnlDropAreas, BorderLayout.CENTER);
		pnlDropAreas.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setBackground(SystemColor.controlShadow);
		pnlDropAreas.add(splitPane);

		JLabel lblLoadTarget = new JLabel("Deobfuscate Jar");
		lblLoadTarget.setBackground(SystemColor.controlShadow);
		lblLoadTarget.setHorizontalAlignment(SwingConstants.CENTER);
		splitPane.setLeftComponent(lblLoadTarget);

		JPanel pnlLoadingLib = new JPanel();
		pnlLoadingLib.setBackground(SystemColor.controlShadow);
		splitPane.setRightComponent(pnlLoadingLib);
		pnlLoadingLib.setLayout(new BorderLayout(0, 0));

		JPanel pnlLibs = new JPanel();
		pnlLibs.setBackground(SystemColor.window);
		pnlLoadingLib.add(pnlLibs, BorderLayout.EAST);

		list = new JList<String>();
		DefaultListModel<String> model = new DefaultListModel<String>();
		model.addElement("  Loaded Libraries ");
		list.setModel(model);
		TransferHandler handler = new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferHandler.TransferSupport info) {
				return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean importData(TransferHandler.TransferSupport info) {
				if (!info.isDrop())
					return false;
				Transferable t = info.getTransferable();
				List<File> data = null;
				try {
					data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				} catch (Exception e) {
					return false;
				}
				frame.setTitle("Deobfuscating...");
				for (File jar : data) {
					if (jar.getName().toLowerCase().endsWith(".jar")) {
						try {
							runAnti(jar);
						} catch (Exception e) {
							ErrorDialog.show(e);
						}
					}
				}
				System.out.println("Done!");
				return true;
			}
		};
		lblLoadTarget.setTransferHandler(handler);
		TransferHandler handler2 = new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferHandler.TransferSupport info) {
				return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean importData(TransferHandler.TransferSupport info) {
				if (!info.isDrop())
					return false;
				Transferable t = info.getTransferable();
				List<File> data = null;
				try {
					data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				} catch (Exception e) {
					return false;
				}
				for (File jar : data)
					if (jar.getName().toLowerCase().endsWith(".jar"))
						addLibrary(jar);
				return true;
			}

		};
		pnlLibs.setLayout(new BorderLayout(0, 0));
		pnlLibs.add(list);

		JLabel lblLoadLib = new JLabel("Load Library");
		lblLoadLib.setBackground(SystemColor.controlShadow);
		lblLoadLib.setHorizontalAlignment(SwingConstants.CENTER);
		lblLoadLib.setTransferHandler(handler2);

		pnlLoadingLib.add(lblLoadLib, BorderLayout.CENTER);
		splitPane.setDividerLocation((frame.getWidth() / 6) * 2);
	}

	private void runAnti(File jar) throws Exception {
		Setup.setBypassSetup();
		Setup lsm = Setup.get(jar.getAbsolutePath(), true);
		for (String className : lsm.getNodes().keySet()) {
			AntiBase anti = makeAnti(lsm.getNodes(), jar);
			ClassNode node = lsm.getNodes().get(className);
			lsm.getNodes().put(className, anti.scan(node));
		}
		Map<String, byte[]> out = MappingProcessor.process(lsm.getNodes(), new HashMap<String, MappedClass>(), true);
		out.putAll(JarUtils.loadNonClassEntries(jar));
		Logger.logLow("Saving...");
		JarUtils.saveAsJar(out, jar.getName() + "-re.jar");
	}

	private AntiBase makeAnti(Map<String, ClassNode> nodes, File jar) {
		switch (comboObfuscator.getSelectedItem().toString()) {
		case OB_ZKM5:
			return new AntiZKM5();
		case OB_ZKM8:
			return new AntiZKM8(jar);
		case OB_STRING_P:
			return new AntiStringer(nodes, true);
		case OB_STRING:
			return new AntiStringer(nodes, false);
		case OB_DASH:
			return new AntiDashO(nodes);
		case OB_ALLA_P:
			return new AntiAllatori(nodes, true);
		case OB_ALLA:
			return new AntiAllatori(nodes, false);
		case OB_VERTEX:
			return new AntiVertex(nodes);
		}
		return null;
	}

	private void addLibrary(File jar) {
		libraries.add(jar);
		DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
		model.addElement("  " + jar.getName() + "  ");
		list.setModel(model);
		list.repaint();
		list.invalidate();
	}
}
