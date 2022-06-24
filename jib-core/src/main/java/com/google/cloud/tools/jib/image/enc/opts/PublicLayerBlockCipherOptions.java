package com.google.cloud.tools.jib.image.enc.opts;

import com.google.cloud.tools.jib.json.JsonTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PublicLayerBlockCipherOptions
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class PublicLayerBlockCipherOptions implements JsonTemplate {

  @Nullable private String cipher;
  @Nullable private byte[] hmac;
  @Nonnull private Map<String, byte[]> cipheroptions = new HashMap<>();

  @Nullable
  public byte[] getHmac() {
    return hmac;
  }

  public void setHmac(@Nullable byte[] hmac) {
    this.hmac = hmac;
  }

  @Nullable
  public String getCipher() {
    return cipher;
  }

  public void setCipher(@Nonnull String cipher) {
    this.cipher = cipher;
  }

  @Nonnull
  public Map<String, byte[]> getCipheroptions() {
    return cipheroptions;
  }

  public void setCipheroptions(@Nonnull Map<String, byte[]> cipheroptions) {
    this.cipheroptions = cipheroptions;
  }

  @Override
  public String toString() {
    return "PublicLayerBlockCipherOptions{"
        + "hmac="
        + Arrays.toString(hmac)
        + ", cipher='"
        + cipher
        + '\''
        + ", cipheroptions="
        + cipheroptions
        + '}';
  }
}
