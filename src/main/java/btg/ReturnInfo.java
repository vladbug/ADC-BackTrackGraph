package btg;

import java.util.LinkedList;
import java.util.*;

import parser_domain.Operation;

public class ReturnInfo {

    private Operation operation; // the father operation

    private int operation_cardinality;

    private Map<Operation,Integer> cardinalities;

    public ReturnInfo(Operation operation) {
        this.operation = operation;
        cardinalities = new HashMap<>();
    }

    public void setOperationCardinality(int c) {
        operation_cardinality = c;
    }

    public int getOperationCardinality() {
        return operation_cardinality;
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
