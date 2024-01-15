package btg;

import extended_parser_domain.Operation;

import java.util.*;



public class Annotation {
    private static final int HASH_TABLE_SIZE = 97; // it's a prime number

    private Operation operation;

    private Status status;

    private int tag;

    private List<Annotation> dependencies; // not all of them will have these


    public Annotation(Operation operation, Status status, int tag) {
        this.operation = operation;
        this.status = status;
        this.tag = getHashedTag(tag);
        dependencies = new LinkedList<>();
    }

     public Annotation(Operation operation, Status status, int tag, boolean to_hash) {
        this.operation = operation;
        this.status = status;
        this.tag = tag;
         dependencies = new LinkedList<>();
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


    public void addDependency(Annotation i) {
        dependencies.add(i);
    }

    public List<Annotation> getDependencies() {
        return dependencies;
    }

    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    public boolean hasTheSameDependencies(List<Annotation> list) {
        return dependencies.equals(list);
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
    public boolean equals (Object o) {
        Annotation other = (Annotation) o;

        String opId = operation.getOperationID();
        String otherId = other.getOperation().getOperationID();

        return opId.equals(otherId) && tag == other.getTag() && status == other.getStatus();
    }

}
