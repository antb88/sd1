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
import java.util.Set;

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


}
