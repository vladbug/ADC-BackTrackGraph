package btg;

import extended_parser_domain.Operation;

import java.util.*;



public class Annotation {
    private static final int HASH_TABLE_SIZE = 97; // it's a prime number

    private Operation operation;

    private Status status;

    private int tag;

    private List<Annotation> arguments; // not all of them will have these


    public Annotation(Operation operation, Status status, int tag) {
        this.operation = operation;
        this.status = status;
        this.tag = getHashedTag(tag);
        arguments = new LinkedList<>();
    }

     public Annotation(Operation operation, Status status, int tag, boolean to_hash) {
        this.operation = operation;
        this.status = status;
        this.tag = tag;
        arguments = new LinkedList<>();
    }

    // Knuth Variant on Division Method
    // DEPRECATED
    private int hash(int tag) {
        return (tag*(tag + 3)) % HASH_TABLE_SIZE;
    }

    /**
     * Hashing the tag and the operation id to make this annotation unique.
     * @return hashed value.
     */
    private int getHashedTag(int tag) {
        String toHash = tag + operation.getOperationID();
        return toHash.hashCode();
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

    public void setTag(int new_tag) {
        tag = new_tag;
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
