package com.google.cloud.tools.jib.image.enc;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Path;
import java.util.Base64;
import javax.annotation.Nullable;

/**
 * EncOciResult
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class EncOciResult {

  @Nullable private String mac;
  @Nullable private String keyWrap;
  @Nullable private Path tempPath;

  @Nullable
  public String getMac() {
    return mac;
  }

  public void setMac(@Nullable byte[] pubOpts) {
    this.mac = Base64.getEncoder().encodeToString(pubOpts);
  }

  @Nullable
  public String getKeyWrap() {
    return keyWrap;
  }

  public void setKeyWrap(@Nullable String keyWrap) {
    if (keyWrap != null) {
      this.keyWrap = Base64.getEncoder().encodeToString(keyWrap.getBytes(UTF_8));
    }
  }

  public void setEncryptedTempFile(@Nullable Path tempFile) {
    this.tempPath = tempFile;
  }

  @Nullable
  public Path getTempPath() {
    return tempPath;
  }
}
