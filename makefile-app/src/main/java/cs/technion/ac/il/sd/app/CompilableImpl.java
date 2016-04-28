package cs.technion.ac.il.sd.app;

/**
 * Compilable implementation
 */
public class CompilableImpl implements Compilable {

    private final String name;
    private Type type;
    private boolean wasModified;


    public CompilableImpl(String name, Type type) {
        this.name = name;
        this.type = type;
        this.wasModified = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean wasModified() {
        return wasModified;
    }

    @Override
    public boolean wasModified(boolean wasModified_) {
        boolean prevWasModified = wasModified;
        wasModified = wasModified_;
        return prevWasModified;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", wasModified=" + wasModified +
                '}';
    }
}
