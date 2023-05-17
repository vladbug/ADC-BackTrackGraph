package run;

import odg.OperationDependencyGraph;
import odg.exceptions.VertexDoesNotExistException;
import parser.Parser;
import parser_domain.Specification;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import btg.BackTrackGraph;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, VertexDoesNotExistException {
        String file_loc = "src/main/resources/tournaments-magmact-extended.json";
        Specification spec = Parser.parse(file_loc);
        
        // Creating standard operation dependency graph
        OperationDependencyGraph odg = new OperationDependencyGraph(spec.getOperations());
        BackTrackGraph btg = new BackTrackGraph(spec.getOperations());
        btg.iterateAllEdges();
        //btg.inferAllLinks();
        
        // // Generating all topological sorts used for nominal test cases
        // System.out.println("\n********* SORTS FOR NOMINAL TESTS *********\n");
        // List<List<String>> sorts = odg.topologicalSorts();
        // print_sorts(sorts);

        // // Generating all topological sorts used for error test cases
        // System.out.println("\n********* SORTS FOR ERROR TESTS *********\n");
        // for(List<String> sort : sorts) {
        //     Collections.reverse(sort);
        // }
        // print_sorts(sorts);

        // // Generating all topological sorts used for random test cases
        // System.out.println("\n********* SORTS FOR RANDOM TESTS *********\n");
        // int randoms = 10;
        // List<List<String>> shuffled = new ArrayList<>();

        // while(randoms != 0) {
        //     // New sort with all operations' ids by no particular order
        //     List<String> sort = new ArrayList<>(spec.getOperations().keySet());
        //     Collections.shuffle(sort);
        //     print_sort(sort);
        //     randoms--;
        // }

    }

    /**
     * Prints all sorts in the parameter collection.
     * @param sorts sorts to be printed.
     */
    private static void print_sorts(List<List<String>> sorts) {
        for(List<String> sort: sorts)
            print_sort(sort);

    }

    /**
     * Prints a single sort.
     * @param sort sort to be printed.
     */
    private static void print_sort(List<String> sort) {
        String ss = "{";

        for(String s : sort)
            ss = ss.concat(s + ", " );

        ss = ss.substring(0, ss.length() - 2);
        ss = ss.concat("}");

        System.out.println(ss);
    }
}
