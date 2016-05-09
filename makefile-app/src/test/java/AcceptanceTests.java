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
 * Acceptance tests for the assignment
 */
public class AcceptanceTests {

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
     * Tests that a more complex file compiles in the correct order,
     * after a file with several dependencies was modified.
     * Note that there are 2 correct ways to compile in order.
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


}
