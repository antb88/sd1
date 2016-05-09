import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.app.Makefile;
import cs.technion.ac.il.sd.app.MakefileModule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link cs.technion.ac.il.sd.app.MakefileImpl}
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


    /**
     * Tests that a file that should not fail doesnt
     */
    @Test
    public void biggerModifiedDoesNotFail() {
        when(mock.wasModified(anyString())).thenReturn(true);
        processFile("bigger");
        Mockito.verify(mock, never()).fail();
    }


    /** Tests that a more complex files compiles the needed number of files */
    @Test
    public void biggerModifiedCompilesTotal() {
        when(mock.wasModified(anyString())).thenReturn(false);
        when(mock.wasModified("f.asm")).thenReturn(true);
        processFile("bigger");
        Mockito.verify(mock, times(5)).compile(anyString());
    }

    /** Tests that a more complex file compiles in the correct order,
     *  after a file with several dependencies was modified.
     *  Note that there are 2 correct ways to compile in order.
     */
    @Test
    public void biggerModifiedCompilesInOrder() {
        when(mock.wasModified(anyString())).thenReturn(false);
        when(mock.wasModified("f.asm")).thenReturn(true);
        processFile("bigger");
        InOrder inOrder = Mockito.inOrder(mock);
        inOrder.verify(mock).compile("f.asm");
        inOrder.verify(mock).compile("f.go");
        inOrder.verify(mock, times(2)).compile(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.equals("t") || o.equals("f.cpp");
            }
        }));
        inOrder.verify(mock).compile("main");
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Tests that a file compiles each needed task exactly once
     */
    @Test
    public void biggerModifiedEachCompilesExactlyOnce() {
        when(mock.wasModified(anyString())).thenReturn(false);
        when(mock.wasModified("f.asm")).thenReturn(true);
        processFile("bigger");
        Mockito.verify(mock, never()).fail();
        Mockito.verify(mock, times(1)).compile("f.asm");
        Mockito.verify(mock, times(1)).compile("f.go");
        Mockito.verify(mock, times(1)).compile("f.cpp");
        Mockito.verify(mock, times(1)).compile("t");
        Mockito.verify(mock, times(1)).compile("main");
    }

    /**
     * Tests that an unmodified larger file isnt compiled if nothing is modified
     */
    @Test
    public void biggerUnmodifiedNoCompilation() {
        when(mock.wasModified(anyString())).thenReturn(false);
        processFile("bigger");
        Mockito.verify(mock, never()).compile(anyString());
        Mockito.verify(mock, never()).fail();
    }

    /**
     * Tests for failure in case of a cycle in a complex file
     */
    @Test
    public void cycleModifiedFails() {
        when(mock.wasModified(anyString())).thenReturn(true);
        processFile("cycle");
        Mockito.verify(mock, never()).compile(anyString());
        Mockito.verify(mock, times(1)).fail();
    }

    /**
     * Tests that even unmodified file with a cycle fails
     */
    @Test
    public void cycleUnModifiedFails() {
        when(mock.wasModified(anyString())).thenReturn(false);
        processFile("cycle");
        Mockito.verify(mock, never()).compile(anyString());
        Mockito.verify(mock, times(1)).fail();
    }

    /**
     * Tests that a several thousands tasks file with dependencies designed as a tree
     * meaning all depending on a single root (recursively), compiles the correct amount of times
     */
    @Test
    public void bigTreeTestCompilesCorrectAmount() {
        when(mock.wasModified("file")).thenReturn(true);
        processFile("tree");
        Mockito.verify(mock, never()).fail();
        Mockito.verify(mock, times(32767)).compile(anyString());
    }

    /**
     * Tests that a several thousands tasks file with dependencies designed as a tree
     * does not compile in case root hasnt changed
     */
    @Test
    public void bigTreeTestUnmodifiedShouldNotCompile() {
        when(mock.wasModified("file")).thenReturn(false);
        processFile("tree");
        Mockito.verify(mock, never()).fail();
        Mockito.verify(mock, never()).compile(anyString());
    }
}
