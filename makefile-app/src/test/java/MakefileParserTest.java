import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.app.Compilable;
import cs.technion.ac.il.sd.app.MakefileParser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests fpr {@link MakefileParser}
 */
public class MakefileParserTest {
    private final Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(ExternalCompiler.class).toInstance(Mockito.mock(ExternalCompiler.class));
        }
    });

    private MakefileParser $;

    private final ExternalCompiler mock = injector.getInstance(ExternalCompiler.class);

    private MakefileParser parseFile(String name) {
        $ = MakefileParser.parse(new File(getClass().getResource(name + "_build.txt").getFile()));
        return $;
    }

    private Compilable find(String name) {
        return $.getCompilable(name).get();
    }

    private boolean depends(String who, String onWhom) {
        return $.getDependantsOf(find(who)).contains(find(onWhom));
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    @Test
    public void smallContainsAll() {
        parseFile("small");
        Set<Compilable> all = $.getCompilables();
        Assert.assertTrue("should contain main", all.contains(find("main")));
        Assert.assertTrue("should contain f1", all.contains(find("f1")));
        Assert.assertTrue("should contain exactly 2", all.size() == 2);
    }

    @Test
    public void smallTypes() {
        parseFile("small");
        Assert.assertEquals(find("f1").getType(), Compilable.Type.FILE);
        Assert.assertEquals(find("main").getType(), Compilable.Type.TASK);
    }

    @Test
    public void smallDependencies() {
        parseFile("small");
        Set<Compilable> dOf1 = $.getDependantsOf(find("f1"));
        Set<Compilable> dOfMain = $.getDependantsOf(find("main"));
        Assert.assertTrue("f1 does not have dependencies", dOf1.size() == 0);
        Assert.assertTrue("main should depend only on f1", dOfMain.contains(find("f1")) && dOfMain.size() == 1);
    }

    @Test
    public void smallModified() {
        when(mock.wasModified("f1")).thenReturn(true);
        parseFile("small").updateModified(mock);
        Assert.assertTrue("f1 was modified", find("f1").wasModified());
        Assert.assertFalse("main wasnt modified", find("main").wasModified());
    }

    @Test
    public void depContainsAll() {
        parseFile("dep");
        Set<Compilable> all = $.getCompilables();
        Assert.assertTrue("should contain main", all.contains(find("main")));
        Assert.assertTrue("should contain f1", all.contains(find("f1")));
        Assert.assertTrue("should contain f2", all.contains(find("f2")));
        Assert.assertTrue("should contain exactly 3", all.size() == 3);
    }

    @Test
    public void depTypes() {
        parseFile("dep");
        Assert.assertEquals(find("f2").getType(), Compilable.Type.FILE);
        Assert.assertEquals(find("f1").getType(), Compilable.Type.FILE);
        Assert.assertEquals(find("main").getType(), Compilable.Type.TASK);
    }

    @Test
    public void depDependencies() {
        parseFile("dep");
        Assert.assertTrue("f1 should not  have dependencies", $.getDependantsOf(find("f1")).isEmpty());
        Assert.assertTrue("f2 should depend on f1", $.getDependantsOf(find("f2")).contains(find("f1")));
        Assert.assertTrue("f2 should depend only on f1", $.getDependantsOf(find("f2")).size() == 1);
        Assert.assertTrue("main should depend on f2", $.getDependantsOf(find("main")).contains(find("f2")));
        Assert.assertTrue("main should depend only on f1", $.getDependantsOf(find("main")).size() == 1);
    }

    @Test
    public void depModified() {
        when(mock.wasModified(anyString())).thenReturn(true);
        parseFile("dep").updateModified(mock);
        Assert.assertTrue("f1 should have been modified", find("f1").wasModified());
        Assert.assertTrue("f2 should have been modified", find("f2").wasModified());
        Assert.assertFalse("main shouldn't have been modified", find("main").wasModified());
    }

    @Test
    public void biggerParsedCorrectly() {
        parseFile("bigger");
        Set<String> names = new HashSet<>(Arrays.asList("main", "t", "t2", "f.py", "f.cpp", "f.java", "f.h", "f.go", "f.asm"));
        Set<String> actual = $.getCompilables().stream().map(Compilable::getName).collect(Collectors.toSet());
        Assert.assertEquals(names, actual);
    }

    @Test
    public void biggerDependanciesCounts() {
        parseFile("bigger");
        Assert.assertTrue("main should have 3 dependants", $.getDependantsOf(find("main")).size() == 3);
        Assert.assertTrue("f.cpp should have 3 dependants", $.getDependantsOf(find("f.cpp")).size() == 3);
        Assert.assertTrue("t should have 4 dependants", $.getDependantsOf(find("t")).size() == 4);
        Assert.assertTrue("f.go should have 1 dependants", $.getDependantsOf(find("f.go")).size() == 1);
        Assert.assertTrue("f.py should have 0 dependants", $.getDependantsOf(find("f.py")).size() == 0);
        Assert.assertTrue("f.java should have 0 dependants", $.getDependantsOf(find("f.java")).size() == 0);
        Assert.assertTrue("f.h should have 0 dependants", $.getDependantsOf(find("f.h")).size() == 0);
        Assert.assertTrue("f.asm should have 0 dependants", $.getDependantsOf(find("f.asm")).size() == 0);
        Assert.assertTrue("t2 should have 0 dependants", $.getDependantsOf(find("t2")).size() == 0);
    }

    @Test
    public void biggerDependancies() {
        parseFile("bigger");

        Assert.assertTrue("main should depend on t", depends("main", "t"));
        Assert.assertTrue("main should depend on f.java", depends("main", "f.java"));
        Assert.assertTrue("main should depend on f.cpp", depends("main", "f.cpp"));

        Assert.assertTrue("t should depend on f.py", depends("t", "f.py"));
        Assert.assertTrue("t should depend on f.go", depends("t", "f.go"));
        Assert.assertTrue("t should depend on f.h", depends("t", "f.h"));
        Assert.assertTrue("t should depend on f.cpp", depends("t", "f.cpp"));

        Assert.assertTrue("f.cpp should depend on f.go", depends("f.cpp", "f.go"));
        Assert.assertTrue("f.cpp should depend on f.h", depends("f.cpp", "f.h"));
        Assert.assertTrue("f.cpp should depend on f.py", depends("f.cpp", "f.py"));

        Assert.assertTrue("f.go should depend on f.asm", depends("f.go", "f.asm"));
    }

    @Test
    public void biggerTypes() {
        parseFile("bigger");

        Set<String> files = new HashSet<>(Arrays.asList("f.py", "f.cpp", "f.java", "f.h", "f.go", "f.asm"));
        Set<String> tasks = new HashSet<>(Arrays.asList("main", "t", "t2"));
        Set<String> actualTasks = $.getCompilables().stream()
                .filter(c -> c.getType() == Compilable.Type.TASK)
                .map(Compilable::getName)
                .collect(Collectors.toSet());
        Set<String> actualFiles = $.getCompilables().stream()
                .filter(c -> c.getType() == Compilable.Type.FILE)
                .map(Compilable::getName)
                .collect(Collectors.toSet());
        Assert.assertEquals(files, actualFiles);
        Assert.assertEquals(tasks, actualTasks);

    }

}
