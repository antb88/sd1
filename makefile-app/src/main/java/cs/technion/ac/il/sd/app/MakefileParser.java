package cs.technion.ac.il.sd.app;

import cs.technion.ac.il.sd.ExternalCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Makefile Input Parser
 */
public class MakefileParser {

    private final File file;
    private HashMap<String, Compilable> nameToComp;
    private HashMap<String, List<String>> nameToDepNames;

    private static final String MAKEFILE_LINE_SPLITTER = "[\\s+[=|:]*\\s*]+|[,\\s+]+";

    public static MakefileParser parse(File file) {
        return new MakefileParser(file).parseFile(file);
    }

    private MakefileParser(File file) {
        this.file = file;
        this.nameToComp = new HashMap<>();
        this.nameToDepNames = new HashMap<>();
    }

    private MakefileParser parseFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(this::parseLine);
        } catch (IOException e) {
            throw new AssertionError();
        }
        return this;
    }

    private void parseLine(String line) {
        Compilable.Type type = line.contains(":") ? Compilable.Type.FILE : Compilable.Type.TASK;
        String[] tasksOrFiles = line.split(MAKEFILE_LINE_SPLITTER);

        String taskOrFile = tasksOrFiles[0];
        nameToComp.put(taskOrFile, new CompilableImpl(taskOrFile, type));
        ArrayList<String> dependencies = new ArrayList<>();
        for (int i = 1; i < tasksOrFiles.length; i++) {
            String dependency = tasksOrFiles[i];
            dependencies.add(dependency);
            nameToComp.putIfAbsent(dependency, new CompilableImpl(dependency, Compilable.Type.FILE));
            nameToDepNames.putIfAbsent(dependency, new ArrayList<>());
        }
        nameToDepNames.put(taskOrFile, dependencies);
    }

    public Collection<Compilable> getCompilables() {
        return nameToComp.values()
                .stream()
                .collect(Collectors.toSet());
    }

    public Collection<Compilable> getDependantsFor(Compilable target) {
        return nameToDepNames.get(target.getName())
                .stream()
                .map(nameToComp::get)
                .collect(Collectors.toSet());
    }

    public MakefileParser updateModified(ExternalCompiler external) {
        nameToComp.values().stream()
                .filter(c -> c.getType() == Compilable.Type.FILE)
                .forEach(c -> c.wasModified(external.wasModified(c.getName())));
        return this;
    }
}
