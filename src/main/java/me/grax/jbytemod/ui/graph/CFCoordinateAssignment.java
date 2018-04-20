package me.grax.jbytemod.ui.graph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.hierarchical.stage.mxCoordinateAssignment;

public class CFCoordinateAssignment extends mxCoordinateAssignment {

  public CFCoordinateAssignment(mxHierarchicalLayout arg0, double arg1, double arg2, int arg3, double arg4, double arg5) {
    super(arg0, arg1, arg2, arg3, arg4, arg5);
    this.minEdgeJetty = 18;
    this.vertexConnectionBuffer  = 8;
  }

}
