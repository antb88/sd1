package cs.technion.ac.il.sd.app;

/**
 * Compilable - represents either File or Task compilable object
 */
public interface Compilable {

    String getName();

    enum Type {
        FILE,
        TASK;
    }

    Type getType();

    boolean wasModified();

    boolean wasModified(boolean wasModified);

}
