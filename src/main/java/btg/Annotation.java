package btg;

import extended_parser_domain.Operation;

import java.util.*;


public class Annotation {

    private Operation operation;

    private Status status;

    private int cardinality;

    private List<Annotation> arguments; // not all of them will have these


    public Annotation(Operation operation, Status status, int cardinality) {
        this.operation = operation;
        this.status = status;
        this.cardinality = cardinality;
        arguments = new LinkedList<>();

    }


    public void addArgument(Annotation i) {
        arguments.add(i);
    }

    public List<Annotation> getArguments() {
        return arguments;
    }

    public boolean hasArguments() {
        return arguments.size() > 0;
    }

    public boolean hasTheSameArguments(List<Annotation> list) {
        return arguments.equals(list);
    }

    public Operation getOperation() {
        return operation;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status new_s) {
        status = new_s;
    }

    public int getCardinality() {
        return cardinality;
    }

    @Override
    public boolean equals(Object o) {
        
        Annotation other = (Annotation) o;

        if(this.operation.getOperationID().equals(other.operation.getOperationID()) &&
        this.cardinality == other.cardinality && this.status == other.status) {
            return true;
        }

        return false;
    }

    
    
}
