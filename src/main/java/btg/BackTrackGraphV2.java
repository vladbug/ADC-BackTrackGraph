package btg;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import parser_domain.*;
import parser.Parser;

public class BackTrackGraphV2 {

    /*
     * This is the main graph, it will use our operations as
     * nodes and we will have multiple types of edges
     * but we could just use the DefaultEdge from the
     * jgrapht library to represent all of them
     */
    private Graph<Operation,DefaultEdge> btg;

    /**
     * This is map stores the operations based on their URLs
     * so for an URL we will have an operation associated to it
     */
    private Map<String,Operation> operationsURLs; 


    /**
     * This map will be responsible to assign the operation to it's requires list
    */
    private Map<Operation,List<String>> operationsRequires;

    /**
     * This map will be responsible to store the operations according to their operationIDs
    */

    private Map<String,Operation> operationIDS;

    /**
     * This map will map the VERB of the operation to the operation
     * 
     */

    private Map<Operation,String> operationVerbs;

    /**
     * This Bag will store the operations that are the "creators"
     * of things
     */
    private Map<Operation,Integer> postBag;

    /**
     * This is the new version of the postBag
     * It is more advanced and has a lot more information
     * I'm gonna name it : history
     * 
     * It will map an operationID into another map 
     * with cardinalities and statusses of things
     * This map will only cover ther POST
     * operations as it's keys!
    */

    private Map<String,List<Information>> history;


    /**
     * This will store the deceased operations
     * It is of out interest to know the ones
     * that got eliminated
     */
    private List<Operation> tombstone;

    /**
     * DEPRICATED
     * This data structure will be responsible to manage 
     * the compound posts. They are very tricky to handle
     * In here we only have one compound post but there 
     * might be more.
     * 
     * key : operationID
     * value : List<Argument> which are just a combinatorial calculus
     */
    //private Map<String, 

    /**
     * This datastructure will be responsible to store all the possible combinations
     * and associate a boolean to it in order to know if we can use it or no.
     */
    private Map<String,List<Pair>> compoundData; // i dont like this , cause the backtrack will have a complex logic with it



    // This will map , postTournamente$1 into the returnalInformation of the same
    private Map<String,ReturnInfo> returnalInformation;

    private Specification spec;

    public BackTrackGraphV2(Map<String, Operation> operations) {

        btg = new DefaultDirectedGraph<>(null, null, false);
        operationsURLs = new HashMap<>(100);
        operationsRequires = new HashMap<>(100);
        postBag = new HashMap<>(100);
        returnalInformation = new HashMap<>(100);
        operationIDS = new HashMap<>(100);
        operationVerbs = new HashMap<>(100);
        history = new HashMap<>(100);
        tombstone = new LinkedList<>();
        compoundData = new HashMap<>(100);

        // And now for each one we will the create the map within itself
        // Maybe not now and do it somewhere in front of the code
        

        // Adding every operation as a vertex
        for (Map.Entry<String, Operation> entry: operations.entrySet()) {
            Operation o = entry.getValue();
            // Constructing the graph
            btg.addVertex(o);
            // Will be stored like (POST /tournaments)
            operationsURLs.put("(" + o.getVerb() + " " + o.getUrl() + ")",o);
            // Will be store like postTournament Operation : postTournament
            operationIDS.put(o.getOperationID(),o);
            // Will be store Operation and the VERB associated
            operationVerbs.put(o,o.getVerb());
        }

        // If in the end we won't use this then we can remote it
        try {
            String file_loc = "src/main/resources/tournaments-magmact-extended.json";
            spec = Parser.parse(file_loc);
        } catch (Exception e) {
            // TODO: handle exception
        }
        

       
    
        createRequiresConnections();
        inferLinks_v3();
       
        
       
        System.out.println("This is a new test sequence!");
        List<ReturnInfo> response = aa(30);
        System.out.println("And these are my arguments: ");
        for(ReturnInfo s : response) {
            System.out.println("------------------------------");
            System.out.println(s.getOperation().getOperationID() + " " + "$"+s.getOperationCardinality());
            Map<Operation,Integer> map = s.getCardinalities();
            System.out.println("These are the arguments for: " + s.getOperation().getOperationID());
            map.forEach((key,value) ->  {
                System.out.println(key.getOperationID() + " " + "$"+value);
            });
            System.out.println("------------------------------");
        }
        System.out.println("------------------------------");

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
    private void createRequiresConnections() { 
        Set<Operation> s = btg.vertexSet();
       

        for(Operation o : s) {
            List<String> requires_list = o.getRequires();
            RequestBodySchema aaa = o.getRequestBody();
            System.out.println("I am this operation " + o.getOperationID() + " "  + "and these are my requestBody parameters: ");
            if(aaa != null) {
                Schema esquima = null;
                //esquima = spec.dereferenceSchema((ReferencedBodySchema) o.getRequestBody());
                esquima = spec.dereferenceSchema(((ReferencedBodySchema) o.getRequestBody()).getName()); 
                
                System.out.println("Name : " +  esquima.getName() + " Type: " + esquima.getType());
                
            }

            System.out.println("------------------------------");
           
            // if(esquima != null) {
            //     System.out.println("Name: " + esquima.getName() + " Type" + esquima.getType());
            // }
            List<String> parsed_list = parseRequires(requires_list);
            operationsRequires.put(o,parsed_list);
            for(String pre : parsed_list) {
                if(pre.equals("T")) {
                    //System.out.println("T!"); // orange nodes
                }
                else if(pre.contains("request_body")) {
                    // we need to change the way that the pre-conditions with the request_body word
                    // GET /players/request_body(this){playerNIF}
                    String[] remove_request_body = pre.split("request_body\\(this\\)");
                    // GET /players/{playerNIF}
                    String new_pre = remove_request_body[0] + remove_request_body[1];
                    //Check if it is a self-getter or no! In this scenario it is always the
                    // post of something so we want to know if there is already a post of that
                    if(o.getVerb().equals("POST") && new_pre.contains(o.getUrl())) {

                        btg.addEdge(o,operationsURLs.get(new_pre) , new SelfEdge());

                        history.put(o.getOperationID(),new LinkedList<>());

                    }

                    btg.addEdge(o,operationsURLs.get(new_pre) , new RequiresEdge());

                }
                else {
                    
                    btg.addEdge(o,operationsURLs.get(pre) ,new RequiresEdge());

                }
             
            }
        }
        
    }

    private void printPostBag() {
        postBag.forEach((key,value) -> {
            System.out.println(key.getOperationID() + value);
            
        }
        );
    }

   
    
    
    private void inferLinks_v3() {

        // Teve ficar desta forma por causa do erro : ConcurrentModificationException
        // Now that we have the requires edges let's infer the links
        // The links should be a different type of edge
        // For that I created two label classes
        // And we can use the instance of to differenciate them
        
        Set<Operation> set = new HashSet<>(btg.vertexSet());
        Set<Operation> operationsToAdd = new HashSet<>();
        Set<Operation> operationsForTime = new HashSet<>();

        for(Operation o : set) {
            //System.out.println("This is the operation in question " + o.getOperationID());
            Set<DefaultEdge> edge_set = btg.incomingEdgesOf(o);
            System.out.println("I am: " + o.getOperationID());
            for(DefaultEdge e : edge_set) {
                // getPlayer : o
                // a -> getPlayer
                // b -> getPlayer
                // c -> getPlayer
                if(e instanceof SelfEdge) {
                    //btg.addEdge(btg.getEdgeTarget(e), o, new LinkEdge());
                    operationsToAdd.add(btg.getEdgeSource(e));
                }

                if(e instanceof RequiresEdge) {
                    System.out.println("I am adding a link with" + btg.getEdgeSource(e).getOperationID());
                    operationsForTime.add(btg.getEdgeSource(e));
                }
                
            
            }

            for(Operation o_add : operationsToAdd) {
                btg.addEdge(o,o_add, new LinkEdge());
                //btg.addEdge(o,o_add, new TimeEdge());
                //Porque é um grafo com apenas arcos singulares para o mesmo node
                System.out.println(btg.addEdge(o,o_add, new TimeEdge())); // we won't use this
                for(Operation o_connect : operationsForTime) {
                    btg.addEdge(o_connect, o_add, new TimeEdge());
                    System.out.println("This is the edge that I added: " + o_connect.getOperationID() + " ---> " + o_add.getOperationID());
                    
                }
            }

           
            operationsForTime = new HashSet<>();
            operationsToAdd = new HashSet<>();
               
        }
    }

    public void iterateAllEdges() {
        Set<DefaultEdge> set = btg.edgeSet();
        for(DefaultEdge e : set) {
            Operation o_source = btg.getEdgeSource(e);
            Operation o_target = btg.getEdgeTarget(e);
            // É um grafo direcionado
            //System.out.println(btg.getEdge(o_source, o_target)); // esta existe 
            //System.out.println(btg.getEdge(o_target, o_source)); // esta nao existe e ele devolve null, é o correto
            
            if(e instanceof SelfEdge)  {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID() + " and I am self edged");

            }

            else if(e instanceof LinkEdge) {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID() + " and I am linked edged");

            }

            else if(e instanceof TimeEdge) {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID() + " and I am timed edged");
            }
            else {
                System.out.println("This is the edge from: " + o_source.getOperationID() + " ----> " + o_target.getOperationID() + " and I am a requires edge");

            }
        
        }
    }

    public List<Information> generateSequence(int nr_iterations) {

        List<Information> sequence = new LinkedList<>();
        for(int i = 0; i < nr_iterations; i++) {
            Operation o = getRandomOperation();
            resolve(o,sequence);
        }

   


        return null;
    }

    /**
     * 
     * @param o - Operation received
     * In this method we will just decide what to do with 
     * the choices that we have. We will detect wich
     * operation it is and perform logic into it
     * in order to append to the returnal list
     */
    private List<Information> resolve(Operation o, List<Information> toReturn) {

        switch(o.getVerb()) {

            case "POST":
            copeWithPost(o,toReturn);
            break;

            case "DELETE":
            copeWithDelete();
            break;

            case "GET":
            copeWithGet();
            break;

            case "PUT":
            copeWithPut();
            break;

            default:
            System.out.println("I did not match any of the operation verbs available");
            
        }
    }

    private List<Information> copeWithPost(Operation o, List<Information> seq) {

        Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);
        boolean simple = true;
        for(DefaultEdge e : edge_set) {
            if(e instanceof TimeEdge) {
                simple = false;
                break;
            }
        }

        // POST'S simples
        if(simple) {
            return copeWithSimplesPost(o,seq);
        }
        // POST'S compostos -> usam outros POSTs para a sua existencia
        else {
            return copeWithCompoundPost(o,seq);
        }
    }

    private List<Information> copeWithSimplesPost(Operation o, List<Information> seq) {

        List<Information> list = history.get(o.getOperationID());

        Status status = Status.AVAILABLE;

        if(list.isEmpty()) {
            // Primeira entrada de todas no algortimo
            int cardinality = 1;
            Information i = new Information(o, status, cardinality);
            list.add(i);
            List<Information> toReturn = List.of(i);
            return toReturn;
        } else {
            int index = list.size();
            Information last_info = list.get(index);
            Information i = new Information(o,status,last_info.getCardinality());
            list.add(i);
            List<Information> toReturn = List.of(i);
            return toReturn;
        }
    }

    private List<Information> copeWithCompoundPost(Operation o, List<Information> seq) {

        // This one will be difficult
        List<Information> list = history.get(o.getOperationID());

        if(list.isEmpty()) {
            // Primeira entrada de todas no algoritmo
            // int cardinality = 1;
            // Information i = new Information(o,status,cardinality);
            List<Information> result = processPost(o);
            return result;
        } else {
            // Precisa de logica adicional aqui sequer?
            List<Information> result = processPost(o);
            return result;
        }

    }

    private List<Information> getValidArguments(Operation o) {
        // We are getting the operations needed for this execution to proceed
        Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);

        List<Operation> needed = new LinkedList<>();
        for(DefaultEdge e : edge_set) {
            if(e instanceof TimeEdge) {
                Operation op = btg.getEdgeTarget(e);
                needed.add(op);
            }
        }
        

        // Now by having them let's run our new method that creates the combinatory for use
        List<Information> possibility = getPossibility(needed,o);

        // This should return already a possibility that we want!
       return possibility;
    }

    private List<Information> getPossibility(List<Operation> needed,Operation o) {
        // We cannot assume that it's just two for loops inside one another
        // We must be generic ! There might be 3 , 4 or even 5 enchanced loops

        List<List<Information>> generated_possibilities = new LinkedList<>();
        for(Operation n : needed) {
            List<Information> info = history.get(n.getOperationID());
            generated_possibilities.add(info);
        }

        List<List<Information>> result = generateCombinations(generated_possibilities);

        // Since we are selecting all of them now we need to extra check if they all are available
        // Em principio nunca estarao aqui unavailable porque eu mandarei eles pra tomb stone
        // Portanto vamos ignorar este passo

        // Given the combinatory let's check if there isn't already an enrollment with
        // that combination 
        List<Information> my_list = history.get(o.getOperationID());

        for(List<Information> list : result) {
            int counter = 0;
            for(Information i : my_list) {

                if(!i.hasTheSameArguments(list)) {
                    counter++;
                }
            }
            if(counter == my_list.size()) {
                // This means the combination is applicable, is a valid one to use
                return list;
            }
        }

        return null;
    }

    // CHATGPT
   public static List<List<Information>> generateCombinations(List<List<Information>> lists) {
        List<List<Information>> combinations = new ArrayList<>();
        int[] indices = new int[lists.size()];

        while (true) {
            List<Information> combination = new ArrayList<>();
            for (int i = 0; i < lists.size(); i++) {
                List<Information> currentList = lists.get(i);
                int currentIndex = indices[i];
                combination.add(currentList.get(currentIndex));
            }
            combinations.add(combination);

            int listIndex = lists.size() - 1;
            while (listIndex >= 0 && indices[listIndex] == lists.get(listIndex).size() - 1) {
                indices[listIndex] = 0;
                listIndex--;
            }

            if (listIndex < 0) {
                break;
            }

            indices[listIndex]++;
        }

        return combinations;
    }

    private List<Information> processPost(Operation o) {

        // What if we already have access to the ones we need with an additional data stucture?

        // In order to obtain (the 'arguments' ) this we will have to follow the red links in the graph
        Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);

        List<Operation> needed = new LinkedList<>();
        for(DefaultEdge e : edge_set) {
            if(e instanceof TimeEdge) {
                Operation op = btg.getEdgeTarget(e);
                needed.add(op);
            }
        }

        List<Information> compareTo = new LinkedList<>();
        // Now we have the arguments needed , we obtained that from the graph
        // We need to check if there are operations available to use
        // In this case when we speak about availability
        // It must exist and I isn't used in any other 'enrollment'
        // The 'enrollment' bit is part of the things that we know
        // about this specification

        // FIRST POSSIBILITY : Both are available!
        // LOGIC: if I can't get all the arguments needed then create
        // all new ones, not sure if this is the best thing to do but
        boolean found = false;
        while(!found) {
            for(Operation op_needed : needed) {
                Information info = getMeOne(op_needed,o);
                if(info != null) 
                    compareTo.add(info);
            }
        }

        // This means that there is at least one that must
        // be created - (back tracked)
        if(compareTo.size() != needed.size()) {
            List<Information> backTrackList = backTrackPost(o,needed);
            return backTrackList; // return the whole backtrack thing
        } else {

            // Here we will have to check if there isn't an enrollment with 
            // these available arguments in use already
            // (this is an expensive operation :/, try to find a better way)
            List<Information> listToCheck = history.get(o.getOperationID());

            

            // This means that all of them are available let's just use them
            List<Information> list_root = history.get(o.getOperationID());
            Information i_root = new Information(o, Status.AVAILABLE, list_root.size()+1);
            for(Information o_needed: compareTo) {
                i_root.addArgument(o_needed);
            }
            list_root.add(i_root);
            List<Information> list = List.of(i_root);
            return list; // just return the POST since we can use the ones available
                        
        }

    }

    /**
     * 
     * @param o - Given the operation get one in the
     * history
     * @return - the information about the operation in our
     * history
     */
    private Information getMeOne(Operation o_get, Operation my_op) {

        List<Information> list = history.get(o_get.getOperationID());

        // We must be carefull cause we need a AVAILABLE one
        // and one that is not being used by another enrollment

        // ORDER : get me the first one that is available and not in use
        for(Information i : list) {
            if(i.getStatus() == Status.AVAILABLE && !i.checkUse(my_op)) { // this must have a different logic
                // !! this probably won't work
                // we need a more refined way to discover the checkUse
                // !! use addChildren method
                return i;
            }
        }
        //If we cannot get one then just return null, this might mean
        // that there is no available one or the available ones
        // are in use
        return null;
    }

    private List<Information> backTrackPost(Operation root,List<Operation> needed) {
        // We already know the operations that we have to back-track
        // Important for the future : we probably will need the
        // method that counts the outgoing edges to know the order
        // of certain posts

        // Let's set the cardinality of the arguments before-hand
        // since we will know it
        List<Information> toReturn = new LinkedList<>();
        Stack<Information> stack = new Stack<>();
        List<Information> list_root = history.get(root.getOperationID());
        Information i_root = new Information(root, Status.AVAILABLE, list_root.size()+1);
        for(Operation o : needed) {
            Information i_father = new Information(o,Status.AVAILABLE,history.get(o.getOperationID()).size()+1);
            i_root.addArgument(i_father);
        }
        stack.push(i_root);

        // Now let's put into the stack the operations that it needs
        for(Operation o : needed) {
            List<Information> list_op = history.get(o.getOperationID());
            Information i = new Information(o,Status.AVAILABLE,list_op.size()+1);
            list_op.add(i);
            stack.push(i);
        }

        while(!stack.isEmpty()) {
            toReturn.add(stack.pop());
        }

        return toReturn;
    }




    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }
    
    public void countTest() {

        Operation o4 = operationsURLs.get("(DELETE /tournaments/{tournamentId}/enrollments/{playerNIF})");
        countOutGoingEdges(btg.outgoingEdgesOf(o4));


    }

    // We need this function to make sure that some postOperations come before others!
    private Map<Operation,Integer> countOutGoingEdges(Set<DefaultEdge> set) {

        Map<Operation,Integer> order = new HashMap<>();

        for(DefaultEdge e : set) {
            if(e instanceof TimeEdge || e instanceof LinkEdge) {
                Operation op = btg.getEdgeTarget(e);
                //System.out.println(op.getOperationID());
                int neighbour_edge_degree = btg.outDegreeOf(op);
                order.put(op,neighbour_edge_degree);

            }
        }

        // Necessário para a ordem dos posts ser a correta
        Map<Operation,Integer> treeMap = sortByValue(order);

        treeMap.forEach((key,value) -> {
            //System.out.println(key.getOperationID() + " " + value);
        }
        );

        return treeMap;

    }

    private List<ReturnInfo> generateUpgradedSequence(Operation o) {

        Stack<ReturnInfo> returnal = new Stack<>();

        Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);

            for(DefaultEdge e : edge_set) {
                if(e instanceof TimeEdge || e instanceof SelfEdge) {
                    Operation op = btg.getEdgeTarget(e);
                    
                    int neighbour_edge_degree = btg.outDegreeOf(op);

                    if(neighbour_edge_degree > 1) {
                        // We are dealing with a non canon creator let's name it like that
                        // We wan't this one to appear first
                        returnal.push(null);

                    }
                    
                }
            }



        return null;
    }

    // This method will generate one sequence
    private List<ReturnInfo> aa(int limit) {
        List<ReturnInfo> toReturn = new LinkedList<>();
        Stack<Operation> stack_return = new Stack<>();
        Stack<Operation> stack_control = new Stack<>();
        Set<Operation> set_control = new HashSet<>(); // este é capaz de ter que estar dentro do for mas ya
        for(int i = 0; i < limit;i++) {
        //List<ReturnInfo> toReturn = new LinkedList<>();
        // This will generate a random number
        // between 0 and the bag.lenght - 1
        // With this we will be able to get
        // a random element from the array
        // Random rnd = new Random();
        
        //Stack<ReturnInfo> stack_information = new Stack<>();
        // int rndNumber = rnd.nextInt(bag.length);
        Operation o = getRandomOperation();
        stack_return.add(o);
        stack_control.add(o);
        set_control.add(o);

        //sSystem.out.println("I am entering the first while");

        while(!stack_control.isEmpty()) {
            Operation to_process = stack_control.pop();
            //ReturnInfo information = generateMultipleSequenceV2(to_process);
            //System.out.println(to_process.getOperationID());
            // Now given that operation we will backtrack
            Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(to_process);
            Map<Operation,Integer> to_iterate = countOutGoingEdges(edge_set);
            to_iterate.forEach((key,value) -> {

                if(!set_control.contains(key)) {
                    stack_return.add(key);
                    stack_control.add(key);
                    set_control.add(key);
                }
                
            });
        }


        List<Operation> sequence = new LinkedList<>();
        
        //System.out.println("I will be stuck here");
        while(!stack_return.isEmpty()) {
            sequence.add(stack_return.pop());
        }

        // while(!stack_information.isEmpty()) {
        //     toReturn.add(stack_information.pop());
        // }
        
        for(Operation op : sequence) {
            ReturnInfo r = generateMultipleSequenceV2(op);
            toReturn.add(r);
        }

        //returnalInformation = new HashMap<>();
        //postBag = new HashMap<>();
        }

        return toReturn;
    }

    public void littleTest() {
        // (POST /tournaments)
        // (POST /players)
        // (POST /tournaments/{tournamentId}/enrollments)
        Operation o1 = operationsURLs.get("(POST /tournaments)");
        Operation o11 = operationsURLs.get("(POST /tournaments)");
        Operation o2 = operationsURLs.get("(POST /players)");
        Operation o22 = operationsURLs.get("(POST /players)");
        Operation o3 = operationsURLs.get("(POST /tournaments/{tournamentId}/enrollments)");
        Operation o33 = operationsURLs.get("(POST /tournaments/{tournamentId}/enrollments)");
        Operation o4 = operationsURLs.get("(DELETE /tournaments/{tournamentId}/enrollments)");


        List<ReturnInfo> list = new LinkedList<>();

        list.add(generateMultipleSequence(o1));
        list.add(generateMultipleSequence(o11));
        list.add(generateMultipleSequence(o2));
        list.add(generateMultipleSequence(o22));
        list.add(generateMultipleSequence(o3));
        list.add(generateMultipleSequence(o33));

        for(ReturnInfo i : list) {
            System.out.println("Printing...");
            Operation to_print = i.getOperation();
            Map<Operation,Integer> m = i.getCardinalities();
            System.out.println("This is the operation: " + to_print.getOperationID());
            System.out.println("And these are my arguments: ");
            m.forEach((key,value) -> {
                System.out.println(key.getOperationID() + " $" + value);
            });
        }

    }

    public void littleTest2() {
        List<ReturnInfo> toReturn = new LinkedList<>();
        Operation o1 = operationsURLs.get("(POST /tournaments)");
        Operation o11 = operationsURLs.get("(POST /tournaments)");
        Operation o111 = operationsURLs.get("(POST /tournaments)");
        Operation o1111 = operationsURLs.get("(POST /tournaments)");
        Operation o2 = operationsURLs.get("(POST /players)");
        Operation o22 = operationsURLs.get("(POST /players)");
        Operation o3 = operationsURLs.get("(POST /tournaments/{tournamentId}/enrollments)");
        Operation o33 = operationsURLs.get("(POST /tournaments/{tournamentId}/enrollments)");
        Operation o4 = operationsURLs.get("(DELETE /tournaments/{tournamentId}/enrollments/{playerNIF})");
        toReturn.add(generateMultipleSequenceV2(o1));
        // toReturn.add(generateMultipleSequenceV2(o11));
        // toReturn.add(generateMultipleSequenceV2(o111));
        // toReturn.add(generateMultipleSequenceV2(o1111));
        // toReturn.add(generateMultipleSequenceV2(o2));
        // toReturn.add(generateMultipleSequenceV2(o22));
        // toReturn.add(generateMultipleSequenceV2(o3));
        toReturn.add(generateMultipleSequenceV2(o4));
        

        for(ReturnInfo i : toReturn) {
            System.out.println(i.getOperation().getOperationID() + i.getOperationCardinality());
            System.out.println("And these are my arguments");
            Map<Operation,Integer> options = i.getCardinalities();

            options.forEach((key,value) -> {
                System.out.println(key.getOperationID() + " " + value);
            }
            );

        }
    }

    private void setNewCardinality(Operation o, ReturnInfo r) {

        // This means that we are dealing with a "creator"
        if(postBag.containsKey(o)) {
            int cardinality = postBag.get(o);
            // We want to increase the cardinality
            cardinality++;
            // Update it in the global data structure
            postBag.put(o,cardinality);
            // Set the own operation cardinality
            // Only the POST operations will be creating these new cardinalities
            // PS: but if we are back tracking we need to give attention to this
            r.setOperationCardinality(cardinality);

            Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);

            for(DefaultEdge e : edge_set) {
                
                Operation op = btg.getEdgeTarget(e);
                if(!(e instanceof SelfEdge) && !(e instanceof RequiresEdge)) {
                    // In this example only postEnrollment will enter here
                    int c = postBag.get(op);
                    if(c == 0) {
                        // It doesn't exist so we will have to back track, if we got here
                        // This means that we have one of the three scenarios on the paper
                        // We will have to back-track but at the same time give the
                        // cardinality to our postEnrollment method
                        r.addCardinality(op,c+1);
                        postBag.put(op,c+1); // talvez nao metia ja e ele tratava disso no backtrack?
                    } else {
                        // This means that there is a resource somewhere, let's just pick one
                        int rnd = new Random().nextInt(c) + 1;
                        r.addCardinality(op, rnd);
                    }

                }
                
            }

            // In the end of this we will have a postEnrollmentReturnInfo with
            // postEnrollment $1 , postPlayer $1, postTournament $1 but the tournament is yet to be created
            // I think that this won't be a problem because the backtrack will eventually get there and create a post

            // IMPORTANT : when generating another postEnrollment we must be sure that the arguments
            // are different from an already existing postEnrollment! Also another thing that came to my mind
            // if we are developing a big sequence and we delete a resource it should be possible after
            // the deletion to create and post of that, we will have to update the postBag but how?
            // we cannot just decrement the variable we must know specificaly what it is
        } else {
            // If we are here this means that this is a deletion, update or getter
            Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);
            // We should accept the links also (they are the base getters)
            boolean timed = false;
            Operation timed_operation = null;

            for(DefaultEdge e : edge_set) {
                if(e instanceof TimeEdge) {
                    Operation op = btg.getEdgeTarget(e);
                    
                    Set<DefaultEdge> neighbour_edge_set = btg.outgoingEdgesOf(op); // TODO: maybe the degree might work just fine
                    for(DefaultEdge ee : neighbour_edge_set) {
                        if(ee instanceof TimeEdge){
                            timed = true;
                            timed_operation = op;
                            break;
                        }
                    }
                }

                if(timed) break;
            }

            if(timed) {
                int c = postBag.get(timed_operation);
                //System.out.println(timed_operation.getOperationID()+"$"+c);
                ReturnInfo timed_r = returnalInformation.get(timed_operation.getOperationID()+"$"+c); // aqui ate pode ser um c aleatorio!!!

                // The problem here is if we feed this with an delete/update/get
                // before having a POST this will just explode because it is not stored into the datastructure
                if(timed_r == null) {
                    // This means that there is not a corresponding post for this operations
                    // But here lies another problem
                    // If we are doing a delete i will seek for a postEnrollment but that postEnrollment must
                    // seek for a postTournament and a postPlayer
                    // note : create a "seek" operation
                    // If it is null this means that we are in the beginning of everything!
                    //int c_of_timed = postBag.get(timed_operation);
                    //r.addCardinality(o, c_of_timed++);
                    System.out.println("The timed operation does not exist yet");
                }

                r.setOperationCardinality(c);
                r.addCardinality(timed_operation,c);
                Map<Operation,Integer> options = timed_r.getCardinalities();
                options.forEach((key,value) -> {
                    r.addCardinality(key, value);
                }
                );  
            } else {
                for(DefaultEdge e : edge_set) {
                    if(!(e instanceof SelfEdge) && !(e instanceof RequiresEdge)) {
                        Operation op = btg.getEdgeTarget(e);
                        int c = postBag.get(op);
                        r.setOperationCardinality(c);
                        r.addCardinality(op,c);

                    }
                }
            }



        }
    }

    private ReturnInfo generateMultipleSequenceV2(Operation o) {
        ReturnInfo information = new ReturnInfo(o);
        setNewCardinality(o, information);
        // we will store postEnrollment$1 in here, will be used for deletions,getters and updates
        returnalInformation.put(o.getOperationID()+"$"+information.getOperationCardinality(),information);
        return information;
    }

    // Maybe this should just work for the POST operations
    // This has no back track involved to it yet
    private ReturnInfo generateMultipleSequence(Operation o) {
        // I want to brute test this sequence
        // 1_P : postPlayer
        // 2_P : postPlayer
        // 1_T : postTournament
        // 1_E : postEnrollment
        //Operation o = getRandomOperation();

        // If the operation is in the bag it means
        // that it is a "creator" operation
        // aka POST

        ReturnInfo information = new ReturnInfo(o);
        //System.out.println(o.getOperationID());
        if(postBag.containsKey(o)) {
            int cardinality = postBag.get(o);
            cardinality++;
            postBag.put(o, cardinality);
            information.addCardinality(o,cardinality);
        }
       
        // Now check if we have red links "this means we have arguments to feed our operation"
        Set<DefaultEdge> edge_set = btg.outgoingEdgesOf(o);
        
        Map<Operation,Integer> options = new HashMap<>();
      
        for(DefaultEdge e : edge_set) {
            Operation op = btg.getEdgeTarget(e);
            //System.out.println("This is my target: " + op.getOperationID());
         
            if(!(e instanceof SelfEdge) && !(e instanceof RequiresEdge)) {
                
                int c = postBag.get(op);
                if(!(postBag.containsKey(op))) { // caso nao queiramos criar novos cardinais!
                    c++; // ele no postEnrollment incrementa os valores no postPlayer e postTournament
                    // isto é aquele problema do arcos vermelhos que eu nao queria aqui!  
                }

                options.put(op,c);
                
            }
            
        }


        // Now for each argument we want to generate a valid random number
        
        options.forEach((key,value) -> {
       
            // se for zero entao o unico valor possivel será 1
            // entre 0 e value - 1 original
            // com +1 será entre 1 e value! é o que queremos
            int rnd = new Random().nextInt(value) + 1;
            System.out.println("This is the random number generated: " + rnd);
            information.addCardinality(key, rnd);
           
            
        }
        );

        return information;


        // This should produce something like : postEnrollment 1 2

    }

    // It is better to have an array, the time complexity will be better
    // we won't need to iterate every single vertex in the graph
    // We just do a contasnt access to the array
    private Operation getRandomOperation() {

        Set<Operation> operations = btg.vertexSet();
        int size = operations.size();
        // Devolve numero aleatorio entre 0 e size - 1
        int rnd = new Random().nextInt(size);
        int i = 0;
        for(Operation o : operations) {
            if(i == rnd)
                return o;
            i++;
        }

        return null;
    }

    // Is this even generical enough?
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

