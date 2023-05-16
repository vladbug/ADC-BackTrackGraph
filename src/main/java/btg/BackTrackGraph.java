package btg;

import odg.exceptions.VertexDoesNotExistException;
import org.jgrapht.Graph;
import org.jgrapht.*;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import parser_domain.Link;
import parser_domain.Operation;
import parser_domain.RequestBodySchema;
import parser_domain.Response;

import java.util.*;

public class BackTrackGraph {

    // I need more information on the node, how do I do that?
    // I still need the information in the operation but I need more than that
    private Graph<Operation,DefaultEdge> btg;

    // This will be responsible to remove one random operation for a test sequence
    // We could acctualy use the bag that I found in the
    // other day , because the Set does not have
    // a "random get" method
    private Operation[] bag;

    private Set<String> cache; // set com operações POST apenas para os multiple tests

    // Maybe instead of having a node we could have
    // a data structure that associates the operations
    // to the $? I think that would cost a lot tho


    // This map will map the operations URL to the corresponding IDs
    // This will be usefull to help to generate the requires connections
    private Map<String,Operation> operationsURLs; 


    
    public BackTrackGraph(Map<String, Operation> operations) {

        btg = new DefaultUndirectedGraph<>(null, null, false);
        operationsURLs = new HashMap<>(100);
        int number_of_operations = 0;

        // Adding every operation as a vertex
        for (Map.Entry<String, Operation> entry: operations.entrySet()) {
            Operation o = entry.getValue();
            btg.addVertex(o);
            // Will be stored like (POST /tournaments)
            operationsURLs.put("(" + o.getVerb() + " " + o.getUrl() + ")",o);
            number_of_operations++;
        }
        
        bag = new Operation[number_of_operations];
        int i = 0;
        for (Map.Entry<String, Operation> entry: operations.entrySet()) {
            bag[i] = entry.getValue();
            i++;
        }

        for(int j = 0; j < bag.length; j++) {

            
            
            
            // System.out.println(bag[j].getOperationID() + " " + bag[j].getVerb());
            // System.out.println("----");
            // System.out.println(bag[j].getUrl());
            // System.out.println("----");
            List<String> parsed = parseRequires(bag[j].getRequires());
            for(String s : parsed) {
                //System.out.println(s);
            }
            //System.out.println(bag[j].getRequires());
            // System.out.println("----");
            // System.out.println(bag[j].getRequestBody()); // how do I extract info from this?
            // System.out.println("----");
            bag[j].getVerb();

        }

        createRequiresConnections();
        inferLinks();

    }

    // This method will create the edges of the requires operation for our graph
    private void createRequiresConnections() { // esta a dar merda aqui e nao sei porque
        Set<Operation> s = btg.vertexSet();

        for(Operation o : s) {
            List<String> requires_list = o.getRequires();
            List<String> parsed_list = parseRequires(requires_list);
            for(String pre : parsed_list) {
                
                if(pre.equals("T")) {
                    //System.out.println("T!"); // orange nodes
                }
                else if(pre.contains("request_body")) {
                    // we need to change the way that the pre-conditions with the request_body work
                    // GET /players/request_body(this){playerNIF}
    
                    String[] remove_request_body = pre.split("request_body\\(this\\)");
              
                    // GET /players/{playerNIF}
                     String new_pre = remove_request_body[0] + remove_request_body[1];
                    // System.out.println("This is the new operation without the request body : " + new_pre);
                    // System.out.println(operationsURLs.get(new_pre).getOperationID());
                    btg.addEdge(o,operationsURLs.get(new_pre) , new DefaultEdge());
                }
                else {
                    // DAR FIX NAQUELES QUE TÊM UM REQUEST_BODY, POIS ELE NAO ESTA NO MAPA
                    // JÁ ESTÁ FIXED
                    // estamos em deletePlayer, (GET /players/{playerNIF}) é o requires
                    // System.out.println(o.getOperationID());
                    // System.out.println(pre);
                    // System.out.println(operationsURLs.get(pre).getOperationID());
                    //btg.addEdge(o, operationsURLs.get(pre),null);
                    btg.addEdge(o,operationsURLs.get(pre) ,new DefaultEdge());

                }
             
            }
        }
        
    }

    // This method will infer the links in our graph
    private void inferLinks() {

    }

    public void iterateAllEdges() {
        Set<DefaultEdge> set = btg.edgeSet();
        for(DefaultEdge e : set) {
            Operation o_source = btg.getEdgeSource(e);
            Operation o_target = btg.getEdgeTarget(e);
            System.out.println("This is the edge from: " + o_source.getOperationID() + " to " + o_target.getOperationID());
        }
    }

    // This method will generate one sequence
    private List<String> generateSequence() {
        // This will generate a random number
        // between 0 and the bag.lenght - 1
        // With this we will be able to get
        // a random element from the array
        Random rnd = new Random();
        int rndNumber = rnd.nextInt(bag.length);
        return null;
    }

    private List<String> parseRequires(List<String> requires) {
        List<String> parsed_requires = new LinkedList<>();
        // [response_code(GET /tournaments/{tournamentId}) == 200]
        for(String s : requires) {
            if(!s.equals("T")) {
                // Every single time that we have a requires
                // we have the response_code wich is always the same
                // for every one, maybe I should use the split in the whole
                // String with the information of "response_code" but
                // for now it will be like this
                String sub_string = s.substring(13); // magic number
                
                // I want to remove the information in the String until ==
                String[] raw = sub_string.split("==");
                parsed_requires.add(raw[0].trim());
            } else {
                parsed_requires.add(s);
            }
        }
       return parsed_requires;
    }

    private boolean equals(String one, String two) {
        // (GET /tournaments/{tournamentId})
        // (GET /tournaments/{tournamentId}) == 200


        return false;
    }


}
