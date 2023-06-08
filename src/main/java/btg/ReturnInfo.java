package btg;

import java.util.LinkedList;
import java.util.*;

import parser_domain.Operation;

public class ReturnInfo {

    private Operation operation; // the father operation

    private Map<Operation,Integer> cardinalities;

    public ReturnInfo(Operation operation) {
        this.operation = operation;
        cardinalities = new HashMap<>();
    }

    public Operation getOperation() {
        return operation;
    }

    public Map<Operation,Integer> getCardinalities() {
        return cardinalities;
    }

    public void addCardinality(Operation o,Integer a) { 
        cardinalities.put(o,a);
    }   



    



    
}
