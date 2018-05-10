package me.grax.jbytemod.analysis.utils;

import java.util.ArrayList;

import me.grax.jbytemod.analysis.block.Block;

public class BlockUtils {
  public static boolean doesMerge(Block block, Block into) {
    return doesMerge(new ArrayList<>(), block, into);
  }

  private static boolean doesMerge(ArrayList<Block> visited, Block block, Block into) {
    if (visited.contains(block)) {
      return false;
    }
    visited.add(block);
    if (block == into) {
      return true;
    }
    for (Block output : block.getOutput()) {
      if (doesMerge(visited, output, into)) {
        return true;
      }
    }
    return false;
  }
}
