import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cs.technion.ac.il.sd.ExternalCompiler;
import cs.technion.ac.il.sd.app.Makefile;
import cs.technion.ac.il.sd.app.MakefileModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ExampleTest {
  private final Injector injector = Guice.createInjector(new MakefileModule(), new AbstractModule() {
    @Override
    protected void configure() {
      // will be replaced with a real implementation in staff tests
      bind(ExternalCompiler.class).toInstance(Mockito.mock(ExternalCompiler.class));
    }
  });
  private final Makefile $ = injector.getInstance(Makefile.class);
  private final ExternalCompiler mock = injector.getInstance(ExternalCompiler.class);

  private void processFile(String name) {
    $.processFile(new File(getClass().getResource(name + "_build.txt").getFile()));
  }

  @Rule
  public Timeout globalTimeout = Timeout.seconds(10);

  @Test
  public void testSimple() {
    when(mock.wasModified(anyString())).thenReturn(true);
    processFile("small");
    InOrder inOrder = Mockito.inOrder(mock);
    inOrder.verify(mock).compile("f1");
    inOrder.verify(mock).compile("main");
    Mockito.verify(mock, times(2)).compile(anyString());
  }

  @Test
  public void withDependency() {
    when(mock.wasModified(anyString())).thenReturn(true);
    processFile("dep");
    InOrder inOrder = Mockito.inOrder(mock);
    inOrder.verify(mock).compile("f1");
    inOrder.verify(mock).compile("f2");
    inOrder.verify(mock).compile("main");
    Mockito.verify(mock, times(3)).compile(anyString());
  }

  @Test
  public void withModification() throws Exception {
    processFile("unmodified");
    when(mock.wasModified(anyString())).thenReturn(false);
    processFile("unmodified");
    Mockito.verify(mock, never()).compile(anyString());
  }
}
