import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.app.Compilable;
import cs.technion.ac.il.sd.app.MakefileParser;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

/**
 * Created by ant on 5/6/16.
 */
public class MakefileParserTest {
    private final Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            // will be replaced with a real implementation in staff tests
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

    @Test
    public void smallContainsAll() {
        parseFile("small");
        Set<String> t = $.getCompilables().stream()
                .map(c -> c.getName())
                .collect(Collectors.toSet());
        Assert.assertTrue("should contain main", t.contains("main"));
        Assert.assertTrue("should contain f1", t.contains("f1"));
        Assert.assertTrue("should contain exacly 2", t.size() == 2);
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
        Set<Compilable> deps1 = $.getDependantsOf(find("f1"));
        Set<Compilable> depsMain = $.getDependantsOf(find("main"));
        Assert.assertTrue("f1 does not have dependencies", deps1.size() == 0);
        Assert.assertTrue("main should depend only on f1", depsMain.contains(find("f1")) && depsMain.size() == 1);
    }

    @Test
    public void smallModified() {
        when(mock.wasModified("f1")).thenReturn(true);
        parseFile("small").updateModified(mock);
        Assert.assertTrue("f1 was modified", find("f1").wasModified());
        Assert.assertFalse("main wasnt modified", find("main").wasModified());
    }


}
