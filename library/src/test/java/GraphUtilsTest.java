import com.google.inject.matcher.Matchers;
import cs.technion.ac.il.sd.library.GraphUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.inject.matcher.Matchers.identicalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Test file for {@link GraphUtils#toposort(DirectedGraph)}
 */
public class GraphUtilsTest {

    private static final boolean PRINT_ON_FAIL = true;

    private static final DirectedAcyclicGraph<Integer, DefaultEdge> smallGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private static final DirectedAcyclicGraph<Integer, DefaultEdge> emptyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private static final DirectedAcyclicGraph<Integer, DefaultEdge> complexGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

    private static final DirectedGraph<Integer, DefaultEdge> binaryTree = new DefaultDirectedGraph<>(DefaultEdge.class);

    private static TraversalListener<Integer, DefaultEdge> traversalListenerMock;
    private <V, E> Optional<Iterator<V>> toposort(DirectedGraph<V, E> graph) {
        return GraphUtils.toposort(graph);
    }

    public <V, E> boolean toposortInvariant(DirectedGraph<V, E> graph, Iterator<V> toposort) {

        Map<V, Integer> topoOrder = new HashMap<>();
        int order = 0;
        while (toposort.hasNext()) {
            topoOrder.put(toposort.next(), ++order);
        }
        if (topoOrder.keySet().size() != graph.vertexSet().size()) {
            return false;
        }

        for (E edge : graph.edgeSet()) {

            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);

            if (topoOrder.get(source) > topoOrder.get(target)) {
                if (PRINT_ON_FAIL) {
                    System.out.println("toposort indexOf " + source + " = " + topoOrder.get(source));
                    System.out.println("toposort indexOf " + target + " = " + topoOrder.get(target));
                    System.out.println("graph = " + graph);
                    System.out.println("toposort = " + toposort);
                }
                return false;
            }
        }
        return true;
    }
    @Before
    public void initTraverseListenerMock()
    {
        traversalListenerMock = Mockito.mock(TraversalListener.class);
    }
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupSmall() {
        smallGraph.addVertex(1);
        smallGraph.addVertex(2);
        smallGraph.addVertex(3);
        smallGraph.addVertex(4);
        smallGraph.addEdge(1, 2);
        smallGraph.addEdge(1, 3);
        smallGraph.addEdge(3, 4);
        System.out.println("@setupSmall");
    }


    @BeforeClass
    public static void setupComplex() {
        complexGraph.addVertex(5);
        complexGraph.addVertex(7);
        complexGraph.addVertex(3);
        complexGraph.addVertex(11);
        complexGraph.addVertex(8);
        complexGraph.addVertex(2);
        complexGraph.addVertex(9);
        complexGraph.addVertex(10);
        complexGraph.addEdge(5, 11);
        complexGraph.addEdge(11, 2);
        complexGraph.addEdge(11, 9);
        complexGraph.addEdge(11, 10);
        complexGraph.addEdge(7, 11);
        complexGraph.addEdge(7, 8);
        complexGraph.addEdge(8, 9);
        complexGraph.addEdge(3, 8);
        complexGraph.addEdge(3, 10);
        System.out.println("@setupComplex");
    }

    @BeforeClass
    public static void setupBinaryTree() {
        int root = -1;
        binaryTree.addVertex(root);
        for(int i=0; i<10; i++)
        {
            binaryTree.addVertex(i);
        }
        binaryTree.addEdge(root, 0);
        binaryTree.addEdge(root, 1);
        for(int i=2; i<10; i++)
        {
            binaryTree.addEdge(i-2, i);
        }


    }

    @Test
    public void nullGraphThrows() {
        thrown.expect(IllegalArgumentException.class);
        toposort(null);
    }

    @Test
    public void cyclicToposortFails() {
        DirectedGraph<Integer, DefaultEdge> cyclicGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        cyclicGraph.addVertex(1);
        cyclicGraph.addVertex(2);
        cyclicGraph.addVertex(3);
        cyclicGraph.addVertex(4);
        cyclicGraph.addEdge(1, 2);
        cyclicGraph.addEdge(1, 3);
        cyclicGraph.addEdge(3, 4);
        cyclicGraph.addEdge(4, 1);
        Assert.assertEquals(true, GraphUtils.hasCycle(cyclicGraph));
        Assert.assertEquals(Optional.empty(), toposort(cyclicGraph));
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);


    @Test
    public void smallGraphReturnsValidSort() {

        List<Integer> topoSort = new LinkedList<>();
        toposort(smallGraph).get().forEachRemaining(topoSort::add);
        boolean sortValid = topoSort.equals(Arrays.asList(1, 2, 3, 4))
                || topoSort.equals(Arrays.asList(1, 3, 2, 4));

        Assert.assertTrue(toposortInvariant(smallGraph, toposort(smallGraph).get()));
        Assert.assertTrue(sortValid);
    }


    @Test
    public void emptyGraphReturnsEmptySort() {
        Iterator<Integer> topoSort = toposort(emptyGraph).get();
        Assert.assertEquals(false, topoSort.hasNext());
    }


    @Test
    public void complexGraphPreservesInvariant() {
        Iterator<Integer> topoSort = toposort(complexGraph).get();
        Assert.assertTrue(toposortInvariant(complexGraph, topoSort));
    }
    @Test
    public void sourcesSmallGraphAreValid()
    {
        Set<Integer> expectedSources = new HashSet<>();
        expectedSources.add(1);
        Assert.assertEquals(GraphUtils.getSourcesVertices(smallGraph), expectedSources);
    }


    @Test
    public void sourcesComplexGraphAreValid()
    {
        Set<Integer> expectedSources = new HashSet<>();
        expectedSources.addAll(Arrays.asList(5,3,7));
        Assert.assertEquals(GraphUtils.getSourcesVertices(complexGraph), expectedSources);
    }
    @Test
    public void dfsIterationFromRootOnBinaryTreeIsCorrect()
    {
        ArrayList<Integer> verteicesList = new ArrayList<>();
        GraphUtils.DFSTraverseSingleComponent(binaryTree, -1, traversalListenerMock).forEachRemaining(v -> {verteicesList.add(v);});
        boolean dfsIterationCorrect = verteicesList.equals(Arrays.asList(-1,0,2,4,6,8,1, 3, 5, 7, 9))
                                        || verteicesList.equals(Arrays.asList(-1,1, 3, 5, 7, 9, 0,2,4,6,8));
        Assert.assertTrue(dfsIterationCorrect);
        Mockito.verify(traversalListenerMock, times(verteicesList.size())).vertexTraversed(anyObject());
        Mockito.verify(traversalListenerMock, times(verteicesList.size())).vertexFinished(anyObject());
    }

    @Test
    public void singleComponentDfsIterationOnBinaryTreeIsCorrect()
    {
        ArrayList<Integer> singleComponentverteicesList = new ArrayList<>();
        GraphUtils.DFSTraverseSingleComponent(binaryTree, 2, traversalListenerMock).forEachRemaining(v -> {singleComponentverteicesList.add(v);});
        Assert.assertEquals(singleComponentverteicesList,Arrays.asList(2,4,6,8));
        singleComponentverteicesList.clear();

        GraphUtils.DFSTraverseSingleComponent(binaryTree, 1, traversalListenerMock).forEachRemaining(v -> {singleComponentverteicesList.add(v);});
        Assert.assertEquals(singleComponentverteicesList,Arrays.asList(1, 3, 5, 7, 9));
    }


    /**
     * NOTE: this test is implementation dependent and may false-fail if DepthSearchIterator implementation changes
     */
    @Test
    public void crossComponentDfsIterationOnBinaryTreeIsCorrect()
    {
        ArrayList<Integer> crossComponentverteicesList = new ArrayList<>();
        GraphUtils.DFSTraverseCrossComponent(binaryTree, 2, traversalListenerMock).forEachRemaining(v -> {crossComponentverteicesList.add(v);});
        boolean crossComponentIterationCorrect = crossComponentverteicesList.equals(Arrays.asList(2,4,6,8,-1,0,1, 3, 5, 7, 9))
                || crossComponentverteicesList.equals(Arrays.asList(2, 4, 6, 8, -1, 1, 3, 5, 7, 9, 0)); //there are more valid options that are not tested here
        Assert.assertTrue(crossComponentIterationCorrect);
    }






}
