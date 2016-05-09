package cs.technion.ac.il.sd.app;

import cs.technion.ac.il.sd.ExternalCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Makefile Input Parser
 */
public class MakefileParser {

    private final File file;
    private HashMap<String, Compilable> nameToComp;
    private HashMap<String, List<String>> nameToDepNames;

    private static final String MAKEFILE_LINE_SPLITTER = "[\\s[=|:]\\s]+|[,\\s+]+|[\\s+$]";

    public static MakefileParser parse(File file) {
        return new MakefileParser(file).parse();
    }

    private MakefileParser(File file) {
        this.file = file;
        this.nameToComp = new HashMap<>();
        this.nameToDepNames = new HashMap<>();
    }

    private MakefileParser parse() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(l -> {
                String trm = l.trim();
                if (!trm.equals(""))
                    parseLine(trm);
            });
        } catch (IOException e) {
            throw new AssertionError();
        }
        return this;
    }

    private List<String> splitLine(String line) {
        String[] ss = line.split(MAKEFILE_LINE_SPLITTER);
        List<String> l = new ArrayList<>();
        for (String s : ss) {
            String trimmed = s.trim();
            if (!trimmed.equals("")) {
                l.add(s);
            }
        }
        return l;
    }

    private void parseLine(String line) {
        List<String> tasksOrFiles = splitLine(line);
        Compilable.Type type = line.contains(":") ? Compilable.Type.FILE : Compilable.Type.TASK;
        String target = tasksOrFiles.get(0);
        nameToComp.put(target, new CompilableImpl(target, type));
        List<String> dependencies = tasksOrFiles.subList(1, tasksOrFiles.size());
        for (String d : dependencies) {
            nameToComp.putIfAbsent(d, new CompilableImpl(d, Compilable.Type.FILE));
            nameToDepNames.putIfAbsent(d, new ArrayList<>());
        }
        nameToDepNames.put(target, dependencies);
    }

    public Optional<Compilable> getCompilable(String name) {
        return Optional.of(nameToComp.get(name));
    }

    public Set<Compilable> getCompilables() {
        return nameToComp.values()
                .stream()
                .collect(Collectors.toSet());
    }

    public Set<Compilable> getDependantsOf(Compilable target) {
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
