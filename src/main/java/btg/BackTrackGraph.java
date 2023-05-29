package btg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import parser_domain.Operation;

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

    // This map will map a number to the operation, this will be used 


    
    public BackTrackGraph(Map<String, Operation> operations) {

        btg = new DefaultDirectedGraph<>(null, null, false);
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
        
        operationsURLs.forEach((k,v) -> {
            //System.out.println("Key" + k);
            //System.out.println("Value" + v.getUrl()); 
        }
        );

        //amIcrazy();
        createRequiresConnections();
        inferLinks_v3();

        for(int j = 0; j < 50; j++) {

            System.out.println("This is a new test sequence!");
            List<String> response = generateSequence();
            for(String s : response) {
                System.out.println(s);
            }
            System.out.println("------------------------------");

        }
    
    }


    public void amIcrazy() { // i am not
        Set<Operation> s = btg.vertexSet();

        for(Operation o : s) {
            System.out.println(o.getOperationID());
            List<String> req = o.getRequires();
            for(String r : req) {
                System.out.println(r);
            }
        }


    }

    // This method will create the edges of the requires operation for our graph
    private void createRequiresConnections() { // esta a dar merda aqui e nao sei porque
        Set<Operation> s = btg.vertexSet();
       

        for(Operation o : s) {
            List<String> requires_list = o.getRequires();

            List<String> parsed_list = parseRequires(requires_list);
            for(String pre : parsed_list) {
                //System.out.println(o.getOperationID());
                //System.out.println("In our list" + pre);
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
                    //System.out.println(o.getOperationID());
                    //System.out.println(remove_request_body[0]);
                    //Check if it is a self-getter or no! In this scenario it is always the
                    // post of something so we want to know if there is already a post of that
                    if(o.getVerb().equals("POST") && new_pre.contains(o.getUrl())) {
                        //System.out.println(o.getUrl());
                        //System.out.println(new_pre);

                        btg.addEdge(o,operationsURLs.get(new_pre) , new SelfEdge());
                    }

                    btg.addEdge(o,operationsURLs.get(new_pre) , new RequiresEdge());

                }
                else {
                    // DAR FIX NAQUELES QUE TÊM UM REQUEST_BODY, POIS ELE NAO ESTA NO MAPA
                    // JÁ ESTÁ FIXED
                    // estamos em deletePlayer, (GET /players/{playerNIF}) é o requires
                    // System.out.println(o.getOperationID());
                    // System.out.println(pre);
                    // System.out.println(operationsURLs.get(pre).getOperationID());
                    //btg.addEdge(o, operationsURLs.get(pre),null);
                    // Check if it is a self_getter or no! In this scenario it is always
                    // a delete of something so it check if it exists. So maybe it is
                    // not a "self getter node"
                    btg.addEdge(o,operationsURLs.get(pre) ,new RequiresEdge());

                }
             
            }
        }
        
    }

    // This method will infer the links in our graph
    /**
     * The rule here will be : everyone with a requires over getTournament/getPlayer
     * will have a link with the postTournament/postPlay (including getTournamet)
     */
    private void inferLinks() {

        // Teve ficar desta forma por causa do erro : ConcurrentModificationException
        // Now that we have the requires edges let's infer the links
        // The links should be a different type of edge
        // For that I created two label classes
        // And we can use the instance of to differenciate them
        
        Set<Operation> set = new HashSet<>(btg.vertexSet());
        Set<Operation> operationsToAdd = new HashSet<>();

        for(Operation o : set) {
            System.out.println("This is the operation in question " + o.getOperationID());
            Set<DefaultEdge> edge_set = btg.incomingEdgesOf(o);
            for(DefaultEdge e : edge_set) {
                Operation o_target = btg.getEdgeTarget(e);
                System.out.println("This is the source: " + btg.getEdgeSource(e).getOperationID() + " and this is the target: " + o_target.getOperationID());
                String operation_url_changed = "(" + o_target.getVerb() + " " + o_target.getUrl() + ")";
                String operation_id = operationsURLs.get(operation_url_changed).getOperationID();

                if(operation_id.equals("getTournament")) {
                    String[] to_link_url = operation_url_changed.split("\\/\\{");
                    String create_connection = to_link_url[0].replaceAll("GET", "POST") + ")";
                    // So now we should have : POST /tournaments
                   
                    Operation o_to_link = operationsURLs.get(create_connection);
                 
                    //btg.addEdge(o, o_to_link, new LinkEdge());
                    operationsToAdd.add(o_to_link);
                   
                }
               
                if(operation_id.equals("getPlayer")) {
                   
                    String[] to_link_url = operation_url_changed.split("\\/\\{");
                    String create_connection = to_link_url[0].replaceAll("GET", "POST") + ")";
                    Operation o_to_link = operationsURLs.get(create_connection);
                    //btg.addEdge(o, o_to_link, new LinkEdge());
                    operationsToAdd.add(o_to_link);
 
                }

            }

            for(Operation o_add : operationsToAdd) {
                System.out.println("I will add the followind edge: " + o.getOperationID() + "-->" + o_add.getOperationID());
                btg.addEdge(o, o_add, new LinkEdge());
            }
           
        }

    }

    private void inferLinks_v2() {



        // Teve ficar desta forma por causa do erro : ConcurrentModificationException
        // Now that we have the requires edges let's infer the links
        // The links should be a different type of edge
        // For that I created two label classes
        // And we can use the instance of to differenciate them
        
        Set<Operation> set = new HashSet<>(btg.vertexSet());
        Set<Operation> operationsToAdd = new HashSet<>();

        for(Operation o : set) {
            //System.out.println("This is the operation in question " + o.getOperationID());
            Set<DefaultEdge> edge_set = btg.incomingEdgesOf(o);
            for(DefaultEdge e : edge_set) {
                // getPlayer : o
                // a -> getPlayer
                // b -> getPlayer
                // c -> getPlayer

                Operation o_source = btg.getEdgeSource(e);
                //System.out.println("I am this operation: " + o.getOperationID());
                //System.out.println("This is the source:" + btg.getEdgeSource(e).getOperationID());
                //System.out.println("This is the target:" + btg.getEdgeTarget(e).getOperationID());
                //System.out.println("This is the source: " + btg.getEdgeSource(e).getOperationID() + " and this is the target: " + btg.getEdgeTarget(e).getOperationID());
                String operation_url_changed = "(" + btg.getEdgeTarget(e).getVerb() + " " + btg.getEdgeTarget(e).getUrl() + ")";
                String operation_id = operationsURLs.get(operation_url_changed).getOperationID();
                //System.out.println("I am the operation: " + operation_id); // should print getTournament for example
                

                // No de incidencia e o que incide dele não é apenas self getter edges
                // tambem temos requires edges nele entao entramos aqui. Porque repara
                // No checkEnrollment ha incidentes mas sao todos de self getters.
                
                if(operation_id.equals("getTournament")) {
                    //System.out.println(operation_url_changed);
                    String[] to_link_url = operation_url_changed.split("\\/\\{");
                    //System.out.println(to_link_url[0]);
                    //System.out.println(btg.getEdgeTarget(e).getUrl());
                    String create_connection = to_link_url[0].replaceAll("GET", "POST") + ")";
                    // So now we should have : POST /tournaments
                   
                    Operation o_to_link = operationsURLs.get(create_connection);
                 
                    btg.addEdge(o, o_to_link, new LinkEdge());
                    //System.out.println("I am adding the edge from: " + o.getOperationID() + " -----> " + o_to_link.getOperationID());
                    break;
                    //operationsToAdd.add(o_to_link);
                   
                }

                else if(e instanceof SelfEdge) {
                    //btg.addEdge(btg.getEdgeTarget(e), o, new LinkEdge());
                }
                
               
                
                if(operation_id.equals("getPlayer")) {
                   
                    String[] to_link_url = operation_url_changed.split("\\/\\{");
                    String create_connection = to_link_url[0].replaceAll("GET", "POST") + ")";
                    Operation o_to_link = operationsURLs.get(create_connection);
                    btg.addEdge(o, o_to_link, new LinkEdge());
                    //System.out.println("I am adding the edge from: " + o.getOperationID() + " -----> " + o_to_link.getOperationID());
                    break;
                    //operationsToAdd.add(o_to_link);
                }

                else if(e instanceof SelfEdge) {
                    //btg.addEdge(btg.getEdgeTarget(e), o, new LinkEdge());
                }


                

            }


            /** 
            for(Operation o_add : operationsToAdd) {
                System.out.println("I will add the followind edge: " + o.getOperationID() + "-->" + o_add.getOperationID());
                btg.addEdge(o_add, o, new LinkEdge());
            }
            */
            
           
        }
    }
    
    
    private void inferLinks_v3() {

        // Teve ficar desta forma por causa do erro : ConcurrentModificationException
        // Now that we have the requires edges let's infer the links
        // The links should be a different type of edge
        // For that I created two label classes
        // And we can use the instance of to differenciate them
        
        Set<Operation> set = new HashSet<>(btg.vertexSet());
        Set<Operation> operationsToAdd = new HashSet<>();

        for(Operation o : set) {
            //System.out.println("This is the operation in question " + o.getOperationID());
            Set<DefaultEdge> edge_set = btg.incomingEdgesOf(o);
            for(DefaultEdge e : edge_set) {
                // getPlayer : o
                // a -> getPlayer
                // b -> getPlayer
                // c -> getPlayer
                if(e instanceof SelfEdge) {
                    //btg.addEdge(btg.getEdgeTarget(e), o, new LinkEdge());
                    operationsToAdd.add(btg.getEdgeSource(e));
                }
            
            }

            for(Operation o_add : operationsToAdd) {
                btg.addEdge(o,o_add, new LinkEdge());
            }

            operationsToAdd = new HashSet<>();
               
        }
    }
    public void iterateAllEdges() {
        Set<DefaultEdge> set = btg.edgeSet();
        for(DefaultEdge e : set) {
            Operation o_source = btg.getEdgeSource(e);
            Operation o_target = btg.getEdgeTarget(e);
            //System.out.println(btg.getEdge(o_source, o_target)); // esta existe 
            //System.out.println(btg.getEdge(o_target, o_source)); // esta nao existe e ele devolve null, é o correto
            
            if(e instanceof SelfEdge)  {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID() + " and I am self edged");

            }
            else {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID());

            }
        }
    }

    // This method will generate one sequence
    private List<String> generateSequence() {
        // This will generate a random number
        // between 0 and the bag.lenght - 1
        // With this we will be able to get
        // a random element from the array
        // Random rnd = new Random();
        Stack<Operation> stack_return = new Stack<>();
        Stack<Operation> stack_control = new Stack<>();
        Set<Operation> set_control = new HashSet<>();
        // int rndNumber = rnd.nextInt(bag.length);
        Operation o = getRandomOperation();
        stack_return.add(o);
        stack_control.add(o);
        set_control.add(o);

        //sSystem.out.println("I am entering the first while");

        while(!stack_control.isEmpty()) {
            Operation to_process = stack_control.pop();
            //System.out.println(to_process.getOperationID());
            // Now given that operation we will backtrack
            Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(to_process);

            // Having the bag $ and the sequence $ , two different things
            // In the sequence it is to differentiante resources
            for(DefaultEdge e : edge_set) {
                Operation op = btg.getEdgeTarget(e);
                //System.out.println("This is my target: " + op.getOperationID());
                if(!(e instanceof SelfEdge) && !set_control.contains(op)) {
                    stack_return.add(op);
                    stack_control.add(op);
                    set_control.add(op);
                }
               
            
            }

        }


        List<String> sequence = new LinkedList<>();
        //System.out.println("I will be stuck here");
        while(!stack_return.isEmpty()) {
            sequence.add(stack_return.pop().getOperationID());
        }
        

        return sequence;


    }

    private List<String> generateSequence(Operation o) {
         // This will generate a random number
        // between 0 and the bag.lenght - 1
        // With this we will be able to get
        // a random element from the array
        // Random rnd = new Random();
        Stack<Operation> stack_return = new Stack<>();
        Stack<Operation> stack_control = new Stack<>();
        Set<Operation> set_control = new HashSet<>();
        // int rndNumber = rnd.nextInt(bag.length);
        stack_return.add(o);
        stack_control.add(o);
        set_control.add(o);
        System.out.println(set_control.contains(o));

        System.out.println("I am entering the first while");

        while(!stack_control.isEmpty()) {
            Operation to_process = stack_control.pop();
            System.out.println(to_process.getOperationID());
            // Now given that operation we will backtrack
            Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(to_process);
          

            // Having the bag $ and the sequence $ , two different things
            // In the sequence it is to differentiante resources
            for(DefaultEdge e : edge_set) {
                Operation op = btg.getEdgeTarget(e);
                System.out.println("This is my target: " + op.getOperationID());
                System.out.println(set_control.contains(op));
                if(!(e instanceof SelfEdge) && !set_control.contains(op)) {
                    stack_return.add(op);
                    stack_control.add(op);
                    set_control.add(op);
                    System.out.println("I contatin now this operation: " + op.getOperationID() + " " + set_control.contains(o));

                }
               
            
            }

            for(Operation operacao : stack_control) {
                System.out.println("This is the information in the stack control");
                System.out.println(operacao.getOperationID());
            }

            for(Operation s : set_control) {
                System.out.println("This is the information in the set control");
                System.out.println(s.getOperationID());
            }
    



        }

      

        List<String> sequence = new LinkedList<>();
        System.out.println("I will be stuck here");
        while(!stack_return.isEmpty()) {
            sequence.add(stack_return.pop().getOperationID());
        }
        

        return sequence;
    }

    // It is better to have an array, the time complexity will be better
    // we won't need to iterate every single vertex in the graph
    // We just do a contasnt access to the array
    private Operation getRandomOperation() {

        Set<Operation> operations = btg.vertexSet();
        int size = operations.size();
        int rnd = new Random().nextInt(size);
        int i = 0;
        for(Operation o : operations) {
            if(i == rnd)
                return o;
            i++;
        }

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


}
