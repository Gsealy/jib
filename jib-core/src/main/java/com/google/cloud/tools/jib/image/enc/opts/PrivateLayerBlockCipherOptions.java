package com.google.cloud.tools.jib.image.enc.opts;

import com.google.cloud.tools.jib.json.JsonTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PrivateLayerBlockCipherOptions
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class PrivateLayerBlockCipherOptions implements JsonTemplate {

  @Nullable private byte[] symkey;
  @Nullable private String digest;
  @Nonnull private Map<String, byte[]> cipheroptions = new HashMap<>();

  public void setSymkey(@Nonnull byte[] symkey) {
    this.symkey = symkey;
  }

  @Nullable
  public String getDigest() {
    return digest;
  }

  public void setDigest(@Nonnull String digest) {
    this.digest = digest;
  }

  @Nonnull
  public Map<String, byte[]> getCipheroptions() {
    return cipheroptions;
  }

  public void setCipheroptions(@Nonnull Map<String, byte[]> cipheroptions) {
    this.cipheroptions = cipheroptions;
  }

  public void addCipheroptions(String key, byte[] value) {
    this.cipheroptions.put(key, value);
  }

  @Override
  public String toString() {
    return "PrivateLayerBlockCipherOptions{"
        + "symkey="
        + Arrays.toString(symkey)
        + ", digest='"
        + digest
        + '\''
        + ", cipheroptions="
        + cipheroptions
        + '}';
  }
}
