package btg;

import extended_parser_domain.Operation;

import java.util.LinkedList;
import java.util.List;


public class Annotation {

    private Operation operation;

    private Status status;

    private int tag;

    // Not all operations have dependencies; this could always be an empty list.
    private List<Annotation> dependencies;


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


    /**
     * Hashing the tag and the operation id to make this annotation unique.
     *
     * @return hashed value.
     */
    private int getHashedTag(int tag) {
        String toHash = tag + operation.getOperationID();
        return toHash.hashCode();
    }

    /**
     * Adds a dependency.
     *
     * @param dependency annotation dependency.
     */
    public void addDependency(Annotation dependency) {
        dependencies.add(dependency);
    }

    public List<Annotation> getDependencies() {
        return dependencies;
    }

    /**
     * Checks whether the annotation depends on other operations.
     *
     * @return true if it has dependencies; false otherwise.
     */
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    /**
     * Checks whether a dependency list is the same as this annotation.
     *
     * @param list dependency list.
     * @return true when the dependency lists are equal; false otherwise.
     * @pre hasDependencies()
     */
    public boolean hasTheSameDependencies(List<Annotation> list) {
        return dependencies.equals(list);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int value) {
        tag = value;
    }

    public String getOperationId() {
        return operation.getOperationID();
    }

    public String getOperationVerb() {
        return operation.getVerb();
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass())
            return false;

        Annotation other = (Annotation) o;

        String opId = getOperationId();
        String otherId = other.getOperationId();

        return opId.equals(otherId) && tag == other.getTag() && status == other.getStatus();
    }

}
