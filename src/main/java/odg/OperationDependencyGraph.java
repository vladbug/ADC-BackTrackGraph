package odg;

import odg.exceptions.VertexDoesNotExistException;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import parser_domain.Link;
import parser_domain.Operation;
import parser_domain.Response;

import java.util.*;

public class OperationDependencyGraph {

    // V - operations; E - links
    private Graph<Operation, Link> odg;

    /**
     * Creates an Operation Dependency Graph.
     * @param operations specification operations by id
     * @throws VertexDoesNotExistException
     */
    public OperationDependencyGraph (Map<String, Operation> operations) throws VertexDoesNotExistException {
        List<Response> responses;
        Operation op;

        odg = new DefaultDirectedGraph<>(Link.class);

        // Adding every operation as a vertex
        for (Map.Entry<String, Operation> entry: operations.entrySet()) {
            odg.addVertex(entry.getValue());
            Operation o = entry.getValue();
        }
    
        // Adding dependency edges
        for (Map.Entry<String, Operation> entry: operations.entrySet()) {
            op = entry.getValue();
            responses = op.getResponses();

            for (Response res: responses) {
                // if there are no links, there are no known dependencies
                if(res.hasLinks())
                    for(Link link: res.getLinks())
                        try {
                            odg.addEdge(op, operations.get(link.getOperationId()), link);
                        } catch (NullPointerException e) {
                            throw new VertexDoesNotExistException();
                        }
                else
                    break;
            }
        }
    }

    public List<List<String>> topologicalSorts(){
        // Only DAGs have a topological sort
        detectCycles();

        List<List<String>> sorts = new ArrayList<>();
        List<Comparator<Operation>> comparators = comparators();
        TopologicalOrderIterator<Operation, Link> iterator;
        List<String> sort;

        // Default topological sort
        iterator = new TopologicalOrderIterator<>(odg);
        sorts.add(topologicalSort(iterator));

        // Finding the different sorts based on the implemented custom comparators
        // for(Comparator<Operation> comparator : comparators){
        //     iterator = new TopologicalOrderIterator<>(odg, comparator);
        //     sort = topologicalSort(iterator);

        //     if(!sorts.contains(sort))
        //         sorts.add(sort);
        // }

        for(int i = 0; i < 500000; i++) {
            Comparator<Operation> c = getRandomComparator(comparators);
            iterator = new TopologicalOrderIterator<>(odg, c);
            sort = topologicalSort(iterator);
            sorts.add(sort);
            
        }

        return sorts;
    }


    private List<String> topologicalSort(TopologicalOrderIterator<Operation, Link> iterator){
        List<String> operationOrder = new ArrayList<>();

        while (iterator.hasNext()) {
            Operation op = iterator.next();
            operationOrder.add(op.getOperationID());
        }

        return operationOrder;
    }

    private Comparator<Operation> getRandomComparator(List<Comparator<Operation>> comparators) {
        List<Comparator<Operation>> comp = comparators();
        int rnd = new Random().nextInt(comp.size());
        int i = 0;
        for(Comparator<Operation> c : comparators) {
            if(i == rnd)
                return c;
            i++;
        }

        return null;
    }

    private List<Comparator<Operation>> comparators (){
        List<Comparator<Operation>> comparators = new ArrayList<>();

        Comparator<Operation> byId = Comparator.comparing(Operation::getOperationID);
        Comparator<Operation> byIdReversed = (o1, o2) -> -o1.getOperationID().compareTo(o2.getOperationID());

        comparators.add(byId);
        comparators.add(byIdReversed);

        Comparator<Operation> byURL = Comparator.comparing(Operation::getUrl);
        Comparator<Operation> byURLReversed = (o1, o2) -> -o1.getUrl().compareTo(o2.getUrl());

        comparators.add(byURL);
        comparators.add(byURLReversed);

        Comparator<Operation> byResponseNumber = Comparator.comparingInt((Operation op) -> op.getResponses().size());
        Comparator<Operation> byResponseNumberReversed = Comparator.comparingInt((Operation op) -> -op.getResponses().size());

        comparators.add(byResponseNumber);
        comparators.add(byResponseNumberReversed);

        Comparator<Operation> byParameterNumber = Comparator.comparingInt((Operation op) -> op.getPathParams().size());
        Comparator<Operation> byParameterNumberReversed = Comparator.comparingInt((Operation op) -> -op.getPathParams().size());

        comparators.add(byParameterNumber);
        comparators.add(byParameterNumberReversed);

       return comparators;
    }

    /**
     * Detects and removes cycles in the ODG.
     */
    private void detectCycles() {
        CycleDetector<Operation, Link> cycleDetector = new CycleDetector<>(odg);

        if (cycleDetector.detectCycles()) {
            System.err.println("The specification contains cyclic dependencies. Breaking the cycles... ");

            Set<Link> dependencies = odg.edgeSet();
            int nrDependencies = dependencies.size() - 1;
            int random = new Random().nextInt(nrDependencies);

            Object[] dependencyArray = dependencies.toArray();

            for(int i = 0; i < dependencies.size(); i++) {
                Link dependency = (Link) dependencyArray[i];
                if(i == random) {
                    odg.removeEdge(dependency);
                    System.err.println("Removed dependency: " + dependency);
                    break;
                }
            }
        }
    }

}
