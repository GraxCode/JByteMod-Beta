package me.grax.jbytemod.ui;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.analysis.obfuscation.ObfuscationAnalyzer;
import me.grax.jbytemod.analysis.obfuscation.enums.NameObfType;
import me.grax.jbytemod.analysis.obfuscation.result.NamesResult;

public abstract class JObfAnalysis extends JFrame {
  protected ObfuscationAnalyzer analyzer;

  public JObfAnalysis(Map<String, ClassNode> nodes) {
    setBounds(100, 100, 800, 800);
    setResizable(false);
    setTitle("Obfuscation Analysis");
    JPanel cp = new JPanel();
    cp.setLayout(new BorderLayout());
    this.analyzer = new ObfuscationAnalyzer(nodes);
    cp.add(new ChartPanel(createChart(analyze(nodes))), BorderLayout.CENTER);
    this.add(cp);

  }

  protected abstract CategoryDataset analyze(Map<String, ClassNode> nodes);

  protected abstract String[] getDescriptors();

  private JFreeChart createChart(CategoryDataset categoryDataset) {
    String[] desc = getDescriptors();
    JFreeChart chart = ChartFactory.createBarChart(desc[0], desc[1], desc[2], categoryDataset);
    return chart;
  }

}
