package cs.technion.ac.il.sd.app;

import java.io.File;

import com.google.inject.Inject;

import cs.technion.ac.il.sd.ExternalCompiler;

public class FakeMakefile implements Makefile {
  private final ExternalCompiler external;

  @Inject
  public FakeMakefile(ExternalCompiler external) {
    this.external = external;
  }

  @Override
  public void processFile(File file) {
    switch (file.getName()) {
      case "small_build.txt":
        external.compile("f1");
        external.compile("main");
        break;
      case "dep_build.txt":
        external.compile("f1");
        external.compile("f2");
        external.compile("main");
        break;
      case "unmodified_build.txt":
        break;
      default:
        throw new UnsupportedOperationException("http://i.imgflip.com/112boa.jpg");
    }
  }
}
