import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.app.Makefile;
import cs.technion.ac.il.sd.app.MakefileModule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by ant on 5/7/16.
 */
public class MakefileTest {

    private final Injector injector = Guice.createInjector(new MakefileModule(), new AbstractModule() {
        @Override
        protected void configure() {
            bind(ExternalCompiler.class).toInstance(Mockito.mock(ExternalCompiler.class));
        }
    });

    private final Makefile $ = injector.getInstance(Makefile.class);
    private final ExternalCompiler mock = injector.getInstance(ExternalCompiler.class);

    private void processFile(String name) {
        $.processFile(new File(getClass().getResource(name + "_build.txt").getFile()));
    }


    @Test
    public void biggerModified() {
        when(mock.wasModified(anyString())).thenReturn(false);
        when(mock.wasModified("f.go")).thenReturn(true);
        processFile("bigger");
//        Assert.assertTrue("f.go should have been modified", find("f.go").wasModified());
//        Assert.assertTrue("f.cpp should have been modified", find("f.cpp").wasModified());
//        Assert.assertTrue("t should have been modified", find("t").wasModified());
//        Assert.assertTrue("main should have been modified", find("main").wasModified());
//        Assert.assertFalse("f.java shouldn't have been modified", find("f.java").wasModified());
//        Assert.assertFalse("f.asm shouldn't have been modified", find("f.asm").wasModified());
//        Assert.assertFalse("f.c shouldn't have been modified", find("f.c").wasModified());
//        Assert.assertFalse("t2 shouldn't have been modified", find("t2").wasModified());
    }
}
