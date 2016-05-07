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

    @Test
    public void biggerModifiedDoesNotFail() {
        when(mock.wasModified(anyString())).thenReturn(true);
        processFile("bigger");
        Mockito.verify(mock, never()).fail();
    }

    @Test
    public void biggerUnModified() {
        when(mock.wasModified(anyString())).thenReturn(false);
        processFile("bigger");
        Mockito.verify(mock, never()).fail();
        Mockito.verify(mock, never()).compile(anyString());
    }

    @Test
    public void biggerModifiedCompilesTotal() {
        when(mock.wasModified(anyString())).thenReturn(false);
        when(mock.wasModified("f.asm")).thenReturn(true);
        processFile("bigger");
        Mockito.verify(mock, times(5)).compile(anyString());
    }

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

    @Test
    public void biggerUnmodifiedNoCompilation() {
        when(mock.wasModified(anyString())).thenReturn(false);
        processFile("bigger");
        Mockito.verify(mock, never()).compile(anyString());
    }

    @Test
    public void cycleModifiedFails() {
        when(mock.wasModified(anyString())).thenReturn(true);
        processFile("cycle");
        Mockito.verify(mock, never()).compile(anyString());
        Mockito.verify(mock, times(1)).fail();
    }

    @Test
    public void cycleUnModifiedFails() {
        when(mock.wasModified(anyString())).thenReturn(false);
        processFile("cycle");
        Mockito.verify(mock, never()).compile(anyString());
        Mockito.verify(mock, times(1)).fail();
    }
}
