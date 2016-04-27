package cs.technion.ac.il.sd.library;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * GraphUtils library
 */
public class GraphUtils {

    /**
     * Performs topological sort on DirectedGraph
     *
     * @param graph graph on which topological sort will be applied
     * @return Optional Iterator whose order of iteration is graph's topological sort
     *  OR Optional.empty if there is no valid sort
     */

    public static <V,E> Optional<Iterator<V>> toposort(DirectedGraph<V, E> graph)  {
        if (hasCycle(graph))
            return Optional.empty();
        return Optional.of(new TopologicalOrderIterator<>(graph));
    }

    /**
     * Checks whether a given {@link DirectedGraph} has a cycle
     *
     * @param graph graph
     * @return true iff graph has cycle
     */
    public static <V,E> boolean hasCycle(DirectedGraph<V,E> graph) {
        return new CycleDetector<>(graph).detectCycles();
    }

    /**
     * Get all sources of a {@link DirectedGraph} - all vertices with no incoming edges
     * @param graph - graph to search
     * @return - set of sources in the received graph
     */
    public static <V,E> Set<V> getSourcesVertices(DirectedGraph<V,E> graph)
    {
        Set<V> vertexSet = graph.vertexSet();
        Set<V> sources = new HashSet<V>(vertexSet.size()*2);
        vertexSet.stream().forEach(v -> {
            if(graph.incomingEdgesOf(v).isEmpty())
            {
                sources.add(v);
            }
        });
        return sources;
    }

    /**
     * Perform Depth-first iteration on the specified graph and apply visit method on each visited vertex.
     * Iteration will start at the specified start vertex and will be limited to the connected component that includes that vertex.
     * If the specified start vertex is null, iteration will start at an arbitrary vertex and will not be limited, that is,
     * will be able to traverse all the graph.
     *
     * @param graph - graph to search
     * @param startVertex - vertex to start DFS iteration
     * @param visitor - {@link DFSVisitor}, implements visit method to be applied on visited vertices
     */
    public static <V,E> void DFSTraverse(DirectedGraph<V,E> graph, V startVertex, DFSVisitor<V> visitor, TraversalListener<V,E> listener)
    {
        GraphIterator<V, E> iterator = new DepthFirstIterator<V, E>(graph, startVertex);
//        iterator.setCrossComponentTraversal(true); // enables to cross component with specifying start vertex
        iterator.addTraversalListener(listener);
        iterator.forEachRemaining(v -> visitor.visit(v));

    }

    public static void main(String[] args) {
        DirectedGraph<Integer, DefaultEdge> g = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addEdge(1,2);
        g.addEdge(1,3);
        g.addEdge(2,5);
        g.addEdge(3,4);
        g.addEdge(4,5);
        System.out.println(g);
        DFSTraverse(g, 1, new DFSVisitor<Integer>() {
            @Override
            public void visit(Integer vertex) {
                System.out.println("visit" + vertex);
            }
        }, new TraversalListener<Integer, DefaultEdge>() {
            @Override
            public void connectedComponentFinished(ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {

            }

            @Override
            public void connectedComponentStarted(ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {

            }

            @Override
            public void edgeTraversed(EdgeTraversalEvent<Integer, DefaultEdge> edgeTraversalEvent) {

            }

            @Override
            public void vertexTraversed(VertexTraversalEvent<Integer> vertexTraversalEvent) {
                System.out.println("vertexTraversed" + vertexTraversalEvent.getVertex());
            }

            @Override
            public void vertexFinished(VertexTraversalEvent<Integer> vertexTraversalEvent) {
                System.out.println("vertexFinished" + vertexTraversalEvent.getVertex());
            }
        });
    }

}
