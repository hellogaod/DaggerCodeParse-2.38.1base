package dagger.internal.codegen.extension;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.SuccessorsFunction;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.google.common.collect.Sets.difference;
import static com.google.common.graph.Graphs.reachableNodes;

/**
 * Utility methods for {@link com.google.common.graph} types.
 */
public final class DaggerGraphs {
    /**
     * Returns a shortest path from {@code nodeU} to {@code nodeV} in {@code graph} as a list of the
     * nodes visited in sequence, including both {@code nodeU} and {@code nodeV}. (Note that there may
     * be many possible shortest paths.)
     *
     * <p>If {@code nodeV} is not {@link
     * com.google.common.graph.Graphs#reachableNodes(com.google.common.graph.Graph, Object) reachable}
     * from {@code nodeU}, the list returned is empty.
     *
     * @throws IllegalArgumentException if {@code nodeU} or {@code nodeV} is not present in {@code
     *                                  graph}
     */
    public static <N> ImmutableList<N> shortestPath(SuccessorsFunction<N> graph, N nodeU, N nodeV) {
        if (nodeU.equals(nodeV)) {
            return ImmutableList.of(nodeU);
        }
        Set<N> successors = ImmutableSet.copyOf(graph.successors(nodeU));
        if (successors.contains(nodeV)) {
            return ImmutableList.of(nodeU, nodeV);
        }

        Map<N, N> visitedNodeToPathPredecessor = new HashMap<>(); // encodes shortest path tree
        for (N node : successors) {
            visitedNodeToPathPredecessor.put(node, nodeU);
        }
        Queue<N> currentNodes = new ArrayDeque<N>(successors);
        Queue<N> nextNodes = new ArrayDeque<N>();

        // Perform a breadth-first traversal starting with the successors of nodeU.
        while (!currentNodes.isEmpty()) {
            while (!currentNodes.isEmpty()) {
                N currentNode = currentNodes.remove();
                for (N nextNode : graph.successors(currentNode)) {
                    if (visitedNodeToPathPredecessor.containsKey(nextNode)) {
                        continue; // we already have a shortest path to nextNode
                    }
                    visitedNodeToPathPredecessor.put(nextNode, currentNode);
                    if (nextNode.equals(nodeV)) {
                        ImmutableList.Builder<N> builder = ImmutableList.builder();
                        N node = nodeV;
                        builder.add(node);
                        while (!node.equals(nodeU)) {
                            node = visitedNodeToPathPredecessor.get(node);
                            builder.add(node);
                        }
                        return builder.build().reverse();
                    }
                    nextNodes.add(nextNode);
                }
            }
            Queue<N> emptyQueue = currentNodes;
            currentNodes = nextNodes;
            nextNodes = emptyQueue; // reusing empty queue faster than allocating new one
        }

        return ImmutableList.of();
    }

    /**
     * Returns the nodes in a graph that are not reachable from a node.
     *
     * 返回graph图中无法从node节点访问的节点。
     */
    public static <N> ImmutableSet<N> unreachableNodes(Graph<N> graph, N node) {
        return ImmutableSet.copyOf(difference(graph.nodes(), reachableNodes(graph, node)));
    }

    private DaggerGraphs() {
    }
}
