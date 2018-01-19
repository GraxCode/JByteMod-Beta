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
import me.grax.jbytemod.analysis.obfuscation.enums.MethodObfType;
import me.grax.jbytemod.analysis.obfuscation.enums.NameObfType;
import me.grax.jbytemod.analysis.obfuscation.result.NamesResult;

public class JNameObfAnalysis extends JObfAnalysis {

  public JNameObfAnalysis(Map<String, ClassNode> nodes) {
    super(nodes);
  }

  protected CategoryDataset analyze(Map<String, ClassNode> nodes) {
    NamesResult nr = analyzer.analyzeNames();
    final String clazzes = "Classes";
    final String methods = "Methods";
    final String fields = "Fields";
    
    final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for(NameObfType t : NameObfType.values()) {
      if(t == NameObfType.NONE)
        continue;
      int i = 0;
      for(NameObfType not : nr.cnames) {
        if(not == t) {
          i++;
        }
      }
      dataset.addValue((double) (i / (double)nr.cnames.size()) * 100d, clazzes, t.getType());
      i = 0;
      for(NameObfType not : nr.mnames) {
        if(not == t) {
          i++;
        }
      }
      dataset.addValue((double) (i / (double)nr.mnames.size())* 100d, methods, t.getType());
      i = 0;
      for(NameObfType not : nr.fnames) {
        if(not == t) {
          i++;
        }
      }
      dataset.addValue((double) (i / (double)nr.fnames.size()) * 100d, fields, t.getType());
    }
    return dataset;
  }

  @Override
  protected String[] getDescriptors() {
    return new String[] { "Name Obfuscation", "Categories", "Percent" };
  }

}
