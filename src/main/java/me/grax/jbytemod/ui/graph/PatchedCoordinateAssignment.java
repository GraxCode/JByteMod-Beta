// Copyright GFI 2017 - Data Systemizer
package me.grax.jbytemod.ui.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.hierarchical.model.mxGraphAbstractHierarchyCell;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyEdge;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyModel;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyNode;
import com.mxgraph.layout.hierarchical.stage.mxCoordinateAssignment;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

/**
 * Patched hierarchical layout to route directly cross-group edges
 * 
 * @author Loison
 *
 */
public class PatchedCoordinateAssignment extends mxCoordinateAssignment {

  /**
   * Constructor
   * 
   * @param layout
   * @param intraCellSpacing
   * @param interRankCellSpacing
   * @param orientation
   * @param initialX
   * @param parallelEdgeSpacing
   */
  public PatchedCoordinateAssignment(mxHierarchicalLayout layout, double intraCellSpacing, double interRankCellSpacing, int orientation,
      double initialX, double parallelEdgeSpacing) {
    super(layout, intraCellSpacing, interRankCellSpacing, orientation, initialX, parallelEdgeSpacing);
  }

  /**
   * Sets the cell locations in the facade to those stored after this layout
   * processing step has completed.
   * 
   * @param graph
   *          the facade describing the input graph
   * @param model
   *          an internal model of the hierarchical layout
   */
  @Override
  protected void setCellLocations(mxGraph graph, mxGraphHierarchyModel model) {
    rankTopY = new double[model.ranks.size()];
    rankBottomY = new double[model.ranks.size()];

    for (int i = 0; i < model.ranks.size(); i++) {
      rankTopY[i] = Double.MAX_VALUE;
      rankBottomY[i] = -Double.MAX_VALUE;
    }

    Set<Object> parentsChanged = null;

    if (layout.isResizeParent()) {
      parentsChanged = new HashSet<Object>();
    }

    Map<Object, mxGraphHierarchyEdge> edges = model.getEdgeMapper();
    Map<Object, mxGraphHierarchyNode> vertices = model.getVertexMapper();

    // Process vertices all first, since they define the lower and 
    // limits of each rank. Between these limits lie the channels
    // where the edges can be routed across the graph

    for (mxGraphHierarchyNode cell : vertices.values()) {
      setVertexLocation(cell);

      if (layout.isResizeParent()) {
        parentsChanged.add(graph.getModel().getParent(cell.cell));
      }
    }

    if (layout.isResizeParent()) {
      adjustParents(parentsChanged);
    }

    // MODIF FLO : enum is not visible

    // Post process edge styles. Needs the vertex locations set for initial
    // values of the top and bottoms of each rank
    //if (this.edgeStyle == HierarchicalEdgeStyle.ORTHOGONAL
    //        || this.edgeStyle == HierarchicalEdgeStyle.POLYLINE)
    //{
    localEdgeProcessing(model);
    //}

    // MODIF FLO : remove jetty and ranks for cross-groups edges : they are garbled

    for (mxGraphAbstractHierarchyCell cell : edges.values()) {
      mxGraphHierarchyEdge edge = (mxGraphHierarchyEdge) cell;

      // Cross group edge?
      boolean isCrossGroupEdge = isCrossGroupEdge(edge);

      if (isCrossGroupEdge) {

        // Clear jettys
        this.jettyPositions.remove(edge);

        // Clear min and max ranks
        edge.minRank = -1;
        edge.maxRank = -1;

      }

    }
    // end MODIF FLO
    for (mxGraphAbstractHierarchyCell cell : edges.values()) {
      setEdgePosition(cell);
    }
  }

  public boolean isCrossGroupEdge(mxGraphHierarchyEdge edge) {

    // Cross group edge?
    boolean isCrossGroupEdge = false;

    for (Object objCell : edge.edges) {

      mxCell edgeCell = (mxCell) objCell;

      // Edge parent same as source parent?
      isCrossGroupEdge = isCrossGroupEdge || (!edgeCell.getParent().equals(edgeCell.getSource().getParent()));
      // Edge parent same as target parent?
      isCrossGroupEdge = isCrossGroupEdge || (!edgeCell.getParent().equals(edgeCell.getTarget().getParent()));

      if (isCrossGroupEdge) {
        // Finished
        break;
      }

    }

    return isCrossGroupEdge;
  }

  @Override
  protected void setVertexLocation(mxGraphAbstractHierarchyCell cell) {
    super.setVertexLocation(cell);
    //    mxGraphHierarchyNode node = (mxGraphHierarchyNode) cell;
    //    mxCell realCell = (mxCell) node.cell;
    //    double positionX = realCell.getGeometry().getX();
    //    double positionY = realCell.getGeometry().getY();
    //    rankTopY[cell.minRank] = Math.min(rankTopY[cell.minRank], positionY);
    //    rankBottomY[cell.minRank] = Math.max(rankBottomY[cell.minRank], positionY + node.height);
    //
    //    if (orientation == SwingConstants.NORTH || orientation == SwingConstants.SOUTH) {
    //      layout.setVertexLocation(realCell, positionX, positionY);
    //    } else {
    //      layout.setVertexLocation(realCell, positionY, positionX);
    //    }
    //
    //    limitX = Math.max(limitX, positionX + node.width);
  }

  @Override
  protected void minPath(mxGraphHierarchyModel model) {
    // Work down and up each edge with at least 2 control points
    // trying to straighten each one out. If the same number of
    // straight segments are formed in both directions, the 
    // preferred direction used is the one where the final
    // control points have the least offset from the connectable 
    // region of the terminating vertices
    Map<Object, mxGraphHierarchyEdge> edges = model.getEdgeMapper();

    for (mxGraphAbstractHierarchyCell cell : edges.values()) {
      if (cell.maxRank > cell.minRank + 2) {
        int numEdgeLayers = cell.maxRank - cell.minRank - 1;
        // At least two virtual nodes in the edge
        // Check first whether the edge is already straight
        int referenceX = cell.getGeneralPurposeVariable(cell.minRank + 1);
        boolean edgeStraight = true;
        int refSegCount = 0;

        for (int i = cell.minRank + 2; i < cell.maxRank; i++) {
          int x = cell.getGeneralPurposeVariable(i);

          if (referenceX != x) {
            edgeStraight = false;
            referenceX = x;
          } else {
            refSegCount++;
          }
        }

        if (edgeStraight) {
          continue;
        }

        int upSegCount = 0;
        int downSegCount = 0;
        double upXPositions[] = new double[numEdgeLayers - 1];
        double downXPositions[] = new double[numEdgeLayers - 1];

        double currentX = cell.getX(cell.minRank + 1);

        for (int i = cell.minRank + 1; i < cell.maxRank - 1; i++) {
          // Attempt to straight out the control point on the
          // next segment up with the current control point.
          double nextX = cell.getX(i + 1);

          if (currentX == nextX) {
            upXPositions[i - cell.minRank - 1] = currentX;
            upSegCount++;
          } else if (repositionValid(model, cell, i + 1, currentX)) {
            upXPositions[i - cell.minRank - 1] = currentX;
            upSegCount++;
            // Leave currentX at same value
          } else {
            upXPositions[i - cell.minRank - 1] = nextX;
            currentX = nextX;
          }
        }

        currentX = cell.getX(cell.maxRank - 1);

        for (int i = cell.maxRank - 1; i > cell.minRank + 1; i--) {
          // Attempt to straight out the control point on the
          // next segment down with the current control point.
          double nextX = cell.getX(i - 1);

          if (currentX == nextX) {
            downXPositions[i - cell.minRank - 2] = currentX;
            downSegCount++;
          } else if (repositionValid(model, cell, i - 1, currentX)) {
            downXPositions[i - cell.minRank - 2] = currentX;
            downSegCount++;
            // Leave currentX at same value
          } else {
            downXPositions[i - cell.minRank - 2] = cell.getX(i - 1);
            currentX = nextX;
          }
        }

        if (downSegCount <= refSegCount && upSegCount <= refSegCount) {
          // Neither of the new calculation provide a straighter edge
//          continue;
        }

        if (downSegCount >= upSegCount) {
          // Apply down calculation values
          for (int i = cell.maxRank - 2; i > cell.minRank; i--) {
            cell.setX(i, (int) downXPositions[i - cell.minRank - 1]);
          }
        } else if (upSegCount > downSegCount) {
          // Apply up calculation values
          for (int i = cell.minRank + 2; i < cell.maxRank; i++) {
            cell.setX(i, (int) upXPositions[i - cell.minRank - 2]);
          }
        } else {
          // Neither direction provided a favourable result
          // But both calculations are better than the
          // existing solution, so apply the one with minimal
          // offset to attached vertices at either end.

        }
      }
    }
  }
}
