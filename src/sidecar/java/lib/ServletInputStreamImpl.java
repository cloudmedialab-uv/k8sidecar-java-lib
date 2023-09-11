package sidecar.java.lib;

import java.io.ByteArrayInputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ServletInputStreamImpl extends ServletInputStream {

  private final ByteArrayInputStream sourceStream;

  public ServletInputStreamImpl(ByteArrayInputStream sourceStream) {
    this.sourceStream = sourceStream;
  }

  @Override
  public boolean isFinished() {
    return sourceStream.available() == 0;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {}

  @Override
  public int read() {
    return sourceStream.read();
  }
}
