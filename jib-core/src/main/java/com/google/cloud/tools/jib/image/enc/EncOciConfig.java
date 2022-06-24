package com.google.cloud.tools.jib.image.enc;

import com.google.cloud.tools.jib.image.enc.opts.PrivateLayerBlockCipherOptions;
import com.google.cloud.tools.jib.image.enc.opts.PublicLayerBlockCipherOptions;
import java.nio.file.Path;

/**
 * OCI encryption config collection
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class EncOciConfig {

  /** block cipher key envelope type */
  enum EnvelopeType {
    JWE,
    PKCS7,
    GPG,
    PKCS11,
  }

  private Path keyPath;
  private EnvelopeType type;

  private PrivateLayerBlockCipherOptions priOpts;
  private PublicLayerBlockCipherOptions pubOpts;

  public EncOciConfig(
      Path keyPath,
      EnvelopeType type,
      PrivateLayerBlockCipherOptions priOpts,
      PublicLayerBlockCipherOptions pubOpts) {
    this.keyPath = keyPath;
    this.type = type;
    this.priOpts = priOpts;
    this.pubOpts = pubOpts;
  }

  public Path getKeyPath() {
    return keyPath;
  }

  public EnvelopeType getType() {
    return type;
  }

  public PrivateLayerBlockCipherOptions getPriOpts() {
    return priOpts;
  }

  public PublicLayerBlockCipherOptions getPubOpts() {
    return pubOpts;
  }
}
