package btg;

import extended_parser_domain.Operation;

import java.util.*;



public class Annotation {

    private Operation operation;

    private Status status;

    private int tag;

    private int HASH_TABLE_SIZE = 97; // it's a prime number

    private List<Annotation> arguments; // not all of them will have these


    public Annotation(Operation operation, Status status, int tag) {
        this.operation = operation;
        this.status = status;
        int hashed_value = this.hash(tag);
        this.tag = hashed_value;
        arguments = new LinkedList<>();

    }

    // Knuth Variant on Division Method
    private int hash(int tag) {
        return (tag*(tag + 3)) % HASH_TABLE_SIZE;
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

    public int getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        
        Annotation other = (Annotation) o;

        if(this.operation.getOperationID().equals(other.operation.getOperationID()) &&
        this.tag == other.tag && this.status == other.status) {
            return true;
        }

        return false;
    }

    
    
}
