package me.lpk.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.objectweb.asm.tree.ClassNode;

import me.lpk.CorrelationMapper;
import me.lpk.mapping.MappedClass;
import me.lpk.mapping.MappingFactory;
import me.lpk.mapping.MappingProcessor;
import me.lpk.mapping.loaders.EnigmaLoader;
import me.lpk.mapping.loaders.MappingLoader;
import me.lpk.mapping.loaders.ProguardLoader;
import me.lpk.mapping.loaders.SRGLoader;
import me.lpk.mapping.remap.impl.ModeNone;
import me.lpk.util.JarUtils;
import me.lpk.util.Setup;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

/**
 * I merged a bunch of things here and for that I am truly sorry for this code's
 * legibility. I'll eventually revamp the whole system anyways and this will
 * disappear.
 */
public class MappingWindow {
	private JFileChooser chooser;
	private JFrame frame;
	private JTextField txtCorrelateTargetJar;
	private JTextField txtCorrelateCleanJar;
	private JTextArea txtrCleanNames;
	private JTextArea txtrTargetNames;
	private JTabbedPane tabbedPane;
	private JPanel pnlProcessing;
	private JPanel pnlConversion;
	//
	private JTextField txtJarLoc;
	private JTextField txtMapLoc;
	private JTextPane txtLog;
	private JButton btnUndo;
	private ActionListener currentAction;
	private File jarProc, mapProc;
	//
	private JButton btnConvert;
	private MappingLoader loader;
	private JTextPane txtOutput;
	private File mapConv;
	private Map<String, MappedClass> mappings = new HashMap<String, MappedClass>();
	private JTextField txtKillMe;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		Setup.setBypassSetup();		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MappingWindow window = new MappingWindow();
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
	public MappingWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Mapping Utilitiy");
		frame.setBounds(100, 100, 787, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		pnlProcessing = new JPanel();
		tabbedPane.addTab("Processing", null, pnlProcessing, null);

		pnlConversion = new JPanel();
		tabbedPane.addTab("Conversion", null, pnlConversion, null);

		JPanel pnlCorrelate = new JPanel();
		tabbedPane.addTab("Correlation", null, pnlCorrelate, null);
		pnlCorrelate.setLayout(null);
		JButton btnCorrelateLoadTarget = new JButton("Load Target");
		btnCorrelateLoadTarget.setBounds(10, 40, 102, 23);
		pnlCorrelate.add(btnCorrelateLoadTarget);
		JButton btnCorrelateLoadClean = new JButton("Load Clean");
		btnCorrelateLoadClean.setBounds(10, 11, 102, 23);
		pnlCorrelate.add(btnCorrelateLoadClean);
		txtCorrelateTargetJar = new JTextField();
		txtCorrelateTargetJar.setBounds(122, 41, 644, 20);
		pnlCorrelate.add(txtCorrelateTargetJar);
		txtCorrelateTargetJar.setColumns(10);
		txtCorrelateCleanJar = new JTextField();
		txtCorrelateCleanJar.setBounds(122, 12, 644, 20);
		pnlCorrelate.add(txtCorrelateCleanJar);
		txtCorrelateCleanJar.setColumns(10);
		JButton btnCorrelate = new JButton("Correlate");
		btnCorrelate.setBounds(10, 74, 102, 23);
		pnlCorrelate.add(btnCorrelate);
		JSplitPane splitCorrelate = new JSplitPane();
		splitCorrelate.setBounds(10, 109, 756, 284);
		pnlCorrelate.add(splitCorrelate);
		txtrCleanNames = new JTextArea();
		txtrTargetNames = new JTextArea();
		splitCorrelate.setDividerLocation(frame.getWidth() / 2);
		splitCorrelate.setLeftComponent(txtrCleanNames);
		splitCorrelate.setRightComponent(txtrTargetNames);
		txtrCleanNames.setText("CleanNames");
		txtrTargetNames.setText("TargetNames");
		btnCorrelate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					go(txtCorrelateTargetJar.getText(), txtCorrelateCleanJar.getText());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnCorrelateLoadClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser();
				int val = fc.showOpenDialog(null);
				if (val == JFileChooser.APPROVE_OPTION) {
					File jar = fc.getSelectedFile();
					txtCorrelateCleanJar.setText(jar.getAbsolutePath());
				}
			}
		});

		btnCorrelateLoadTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser();
				int val = fc.showOpenDialog(null);
				if (val == JFileChooser.APPROVE_OPTION) {
					File jar = fc.getSelectedFile();
					txtCorrelateTargetJar.setText(jar.getAbsolutePath());
				}
			}
		});
		pnlProcessing.setLayout(null);

		txtJarLoc = new JTextField();
		txtJarLoc.setBounds(140, 11, 626, 23);
		pnlProcessing.add(txtJarLoc);
		txtJarLoc.setColumns(10);

		JButton btnLoadJar = new JButton("Load Jar");
		btnLoadJar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser();
				int val = fc.showOpenDialog(null);
				if (val == JFileChooser.APPROVE_OPTION) {
					jarProc = fc.getSelectedFile();
					txtJarLoc.setText(jarProc.getAbsolutePath());
					if (mapProc != null) {
						btnUndo.setEnabled(true);
					}
				}
			}
		});
		btnLoadJar.setBounds(10, 11, 120, 23);
		pnlProcessing.add(btnLoadJar);

		JButton btnLoadMappings = new JButton("Load Mappings");
		btnLoadMappings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = getFileChooser();
				int val = fc.showOpenDialog(null);
				if (val == JFileChooser.APPROVE_OPTION) {
					mapProc = fc.getSelectedFile();
					txtMapLoc.setText(mapProc.getAbsolutePath());
					if (jarProc != null) {
						btnUndo.setEnabled(true);
					}
				}
			}
		});
		btnLoadMappings.setBounds(10, 45, 120, 23);
		pnlProcessing.add(btnLoadMappings);

		txtMapLoc = new JTextField();
		txtMapLoc.setColumns(10);
		txtMapLoc.setBounds(140, 45, 626, 23);
		pnlProcessing.add(txtMapLoc);

		btnUndo = new JButton("Reverse");
		currentAction = new ProguardAction();
		btnUndo.addActionListener(currentAction);
		btnUndo.setBounds(10, 79, 120, 23);
		btnUndo.setEnabled(false);
		pnlProcessing.add(btnUndo);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setBounds(10, 116, 756, 266);
		pnlProcessing.add(scrollPane);
		txtLog = new JTextPane();
		scrollPane.setViewportView(txtLog);
		JComboBox<String> combo = new JComboBox<String>(new String[] { "Proguard", "Enigma", "SRG" });
		// JComboBox combo = new JComboBox();
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = combo.getSelectedItem().toString();
				btnUndo.removeActionListener(currentAction);
				if (s.equals("Proguard")) {
					currentAction = new ProguardAction();
					log("Switching to Proguard mapping processor.");
				} else if (s.equals("Enigma")) {
					currentAction = new EnigmaAction();
					log("Switching to Enigma mapping processor.");
				} else if (s.equals("SRG")) {
					currentAction = new SRGAction();
					log("Switching to SRG mapping processor.");
				}
				btnUndo.addActionListener(currentAction);
			}
		});

		combo.setBounds(140, 79, 169, 23);
		pnlProcessing.add(combo);

		JButton btnLoadMap = new JButton("Load Map");
		btnLoadMap.setLocation(10, 10);
		btnLoadMap.setSize(120, 23);
		btnLoadMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int val = getFileChooser().showOpenDialog(null);
				if (val == JFileChooser.APPROVE_OPTION) {
					mapConv = chooser.getSelectedFile();
					txtKillMe.setText(mapConv.getAbsolutePath());
					load();
				}
				btnConvert.setEnabled(true);
			}

		});
		pnlConversion.add(btnLoadMap);

		btnConvert = new JButton("Convert");
		btnConvert.setLocation(10, 39);
		btnConvert.setSize(120, 23);
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				convert();
			}

		});
		btnConvert.setEnabled(false);
		pnlConversion.add(btnConvert);

		JComboBox<String> cmboMappingTypes = new JComboBox<String>(new String[] { "Proguard", "Enigma", "SRG" });
		// JComboBox cmboMappingTypes = new JComboBox();
		cmboMappingTypes.setBounds(140, 40, 626, 20);
		cmboMappingTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = cmboMappingTypes.getSelectedItem().toString();
				if (s.equals("Proguard")) {
					loader = new ProguardLoader();
				} else if (s.equals("Enigma")) {
					loader = new EnigmaLoader();
				} else if (s.equals("SRG")) {
					loader = new SRGLoader();
				}
			}
		});
		pnlConversion.setLayout(null);
		pnlConversion.add(cmboMappingTypes);

		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setBounds(10, 73, 756, 309);
		pnlConversion.add(scrollPane2);

		txtOutput = new JTextPane();
		scrollPane2.setViewportView(txtOutput);

		txtKillMe = new JTextField();
		txtKillMe.setBounds(140, 11, 626, 20);
		pnlConversion.add(txtKillMe);
		txtKillMe.setColumns(10);
	}

	private void convert() {
		txtOutput.setText(txtOutput.getText() + "Converting '" + mapConv.getName() + "' via: " + loader.getClass().getSimpleName() + "\n");
		loader.save(mappings, new File(mapConv.getName() + "-re.map"));
		txtOutput.setText(txtOutput.getText() + "Finished!" + "\n");
	}

	private void load() {
		txtOutput.setText(txtOutput.getText() + "Loading from: '" + mapConv.getName() + "'\n");
		mappings.clear();
		try (InputStream fis = new FileInputStream(mapConv);
				InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
				BufferedReader br = new BufferedReader(isr);) {
			br.mark(0);
			String line = br.readLine();
			br.reset();
			if (line.startsWith("CLASS ")) {
				txtOutput.setText(txtOutput.getText() + "Detected Enigma format.\n");
				mappings.putAll(new EnigmaLoader().read(br));
			} else if (line.startsWith("PK:") || line.startsWith("CL:")) {
				txtOutput.setText(txtOutput.getText() + "Detected SRG format.\n");
				mappings.putAll(new SRGLoader().read(br));
			} else if (line.contains(" -> ")) {
				txtOutput.setText(txtOutput.getText() + "Detected Proguard format.\n");
				mappings.putAll(new ProguardLoader().read(br));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void go(String pathTarget, String pathClean) throws Exception {
		// Loading
		File targetJar = new File(pathTarget);
		File cleanJar = new File(pathClean);
		Setup.setBypassSetup();
		Setup targ = Setup.get(targetJar.getAbsolutePath(), false);
		Setup clen = Setup.get(cleanJar.getAbsolutePath(), false);
		// Classpather.addFile(targetJar);

		System.out.println("Loading classes...");
		Map<String, ClassNode> targetNodes = targ.getNodes();
		Map<String, ClassNode> baseNodes = clen.getNodes();
		// Making maps
		System.out.println("Generating mappings");
		Map<String, MappedClass> targetMappings = MappingFactory.mappingsFromNodes(targetNodes);
		for (MappedClass mappedClass : targetMappings.values()) {
			targetMappings = MappingFactory.linkMappings(mappedClass, targetMappings);
		}
		Map<String, MappedClass> cleanMappings = MappingFactory.mappingsFromNodes(baseNodes);
		// Linking
		System.out.println("Linking correlating sources...");
		targetMappings = resetRemapped(targetMappings);
		correlate(targetMappings, cleanMappings);
		// Filling in the gaps
		System.out.println("Filling in missing classes...");
		targetMappings = CorrelationMapper.fillInTheGaps(targetMappings, new ModeNone());
		// Processing
		System.out.println("Processing output jar...");
		saveJar(targetJar, targetNodes, targetMappings);
		saveMappings(targetMappings, targ.getJarName() + ".enigma.map");
		System.out.println("Done!");
	}

	private void correlate(Map<String, MappedClass> mappings, Map<String, MappedClass> baseClasses) {
		HashMap<String, String> h = new HashMap<String, String>();
		String[] clean = txtrCleanNames.getText().split("\n");
		String[] target = txtrTargetNames.getText().split("\n");
		for (int i = 0; i < Math.min(clean.length, target.length); i++) {
			h.put(target[i], clean[i]);
		}
		for (String obfu : h.keySet()) {
			MappedClass targetClass = mappings.get(obfu);
			MappedClass cleanClass = baseClasses.get(h.get(obfu));
			if (targetClass == null) {
				System.err.println("NULL 1: " + obfu + ":" + h.get(obfu));

				if (cleanClass == null) {
					System.err.println("NULL 2: " + obfu + ":" + h.get(obfu));
					continue;
				}
			}
			mappings = CorrelationMapper.correlate(targetClass, cleanClass, mappings, baseClasses);
		}
	}

	private static void saveMappings(Map<String, MappedClass> mappings, String string) {
		EnigmaLoader enigma = new EnigmaLoader();
		enigma.save(mappings, new File(string));
	}

	private static Map<String, MappedClass> resetRemapped(Map<String, MappedClass> mappings) {
		for (String name : mappings.keySet()) {
			MappedClass mc = mappings.get(name);
			mc.setRenamedOverride(false);
			mappings.put(name, mc);
		}
		return mappings;
	}

	private static void saveJar(File nonEntriesJar, Map<String, ClassNode> nodes, Map<String, MappedClass> mappings) {
		Map<String, byte[]> out = null;
		out = MappingProcessor.process(nodes, mappings, true);
		try {
			out.putAll(JarUtils.loadNonClassEntries(nonEntriesJar));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int renamed = 0;
		for (MappedClass mc : mappings.values()) {
			if (mc.isTruelyRenamed()) {
				renamed++;
			}
		}
		System.out.println("Saving...  [Ranemed " + renamed + " classes]");
		JarUtils.saveAsJar(out, nonEntriesJar.getName() + "_correlated.jar");
	}

	private JFileChooser getFileChooser() {
		if (chooser == null) {
			chooser = new JFileChooser();
			final String dir = System.getProperty("user.dir");
			final File fileDir = new File(dir);
			chooser.setCurrentDirectory(fileDir);
		}
		return chooser;
	}

	class EnigmaAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Map<String, ClassNode> nodes = JarUtils.loadClasses(jarProc);
				log("Loaded nodes from jar: " + jarProc.getAbsolutePath());
				Map<String, MappedClass> mappedClasses = MappingFactory.mappingsFromEnigma(mapProc, nodes);
				log("Loaded mappings from engima mappings: " + mapProc.getAbsolutePath());
				saveJar(jarProc, nodes, mappedClasses);
				log("Saved modified file!");

			} catch (IOException e1) {
				log(e1.getMessage());
			}
		}
	}

	class SRGAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Map<String, ClassNode> nodes = JarUtils.loadClasses(jarProc);
				log("Loaded nodes from jar: " + jarProc.getAbsolutePath());
				Map<String, MappedClass> mappedClasses = MappingFactory.mappingsFromSRG(mapProc, nodes);
				log("Loaded mappings from engima mappings: " + mapProc.getAbsolutePath());
				saveJar(jarProc, nodes, mappedClasses);
				log("Saved modified file!");

			} catch (IOException e1) {
				log(e1.getMessage());
			}
		}
	}

	class ProguardAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Map<String, ClassNode> nodes = JarUtils.loadClasses(jarProc);
				log("Loaded nodes from jar: " + jarProc.getAbsolutePath());
				Map<String, MappedClass> mappedClasses = MappingFactory.mappingsFromProguard(mapProc, nodes);
				log("Loaded mappings from proguard mappings: " + mapProc.getAbsolutePath());
				saveJar(jarProc, nodes, mappedClasses);
				log("Saved modified file!");

			} catch (IOException e1) {
				log(e1.getMessage());
			}
		}
	}

	private void log(String s) {
		txtLog.setText(txtLog.getText() + "\n" + s);
	}
}
