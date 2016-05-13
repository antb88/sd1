package cs.technion.ac.il.sd.app;

/**
 * Compilable - represents either File or Task compilable object
 */
public interface Compilable {

    enum Type {
        FILE,
        TASK;
    }
    String getName();
    Type getType();
    boolean wasModified();
    boolean wasModified(boolean wasModified);
    boolean wasTraversed();
    boolean wasTraversed(boolean wasTraversed_);


}
