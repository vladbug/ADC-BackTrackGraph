package btg;

import parser_domain.Operation;

import java.util.*;


public class Information {

    private Operation operation;

    private Status status;

    private int cardinality;

    private List<Information> arguments; // not all of them will have these

    private List<Operation> feeding;

    public Information(Operation operation, Status status, int cardinality) {
        this.operation = operation;
        this.status = status;
        this.cardinality = cardinality;
        arguments = new LinkedList<>();
        feeding = new LinkedList<>();
    }


    public void addArgument(Information i) {
        arguments.add(i);
    }

    public void addChilden(Operation o) {
        feeding.add(o);
    }

    public boolean checkUse(Operation o) {
        return feeding.contains(o);
    }

    public List<Information> getArguments() {
        return arguments;
    }

    public boolean hasTheSameArguments(List<Information> list) {
        return arguments.equals(list);
    }

    public Operation getOperation() {
        return operation;
    }

    public Status getStatus() {
        return status;
    }

    public int getCardinality() {
        return cardinality;
    }

    @Override
    public boolean equals(Object o) {
        
        Information other = (Information) o;

        if(this.operation.getOperationID().equals(other.operation.getOperationID()) &&
        this.cardinality == other.cardinality && this.status == other.status) {
            return true;
        }

        return false;
    }

    
    
}
