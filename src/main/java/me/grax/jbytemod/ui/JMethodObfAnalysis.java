package me.grax.jbytemod.ui;

import java.util.Map;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.objectweb.asm.tree.ClassNode;

import me.grax.jbytemod.analysis.obfuscation.enums.MethodObfType;
import me.grax.jbytemod.analysis.obfuscation.result.MethodResult;

public class JMethodObfAnalysis extends JObfAnalysis {

  public JMethodObfAnalysis(Map<String, ClassNode> nodes) {
    super(nodes);
  }

  protected CategoryDataset analyze(Map<String, ClassNode> nodes) {
    MethodResult mr = analyzer.analyzeMethod();
    final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (MethodObfType t : MethodObfType.values()) {
      if (t == MethodObfType.NONE)
        continue;
      int i = 0;
      for (MethodObfType not : mr.mobf) {
        if (not == t) {
          i++;
        }
      }
      dataset.addValue((double) (i / (double) mr.mobf.size()) * 100d, "", t.getType());
    }
    return dataset;
  }

  @Override
  protected String[] getDescriptors() {
    return new String[] { "Method Obfuscation", "Categories", "Percent" };
  }

}
