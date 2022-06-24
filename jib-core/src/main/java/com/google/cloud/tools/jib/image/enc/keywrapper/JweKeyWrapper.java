package com.google.cloud.tools.jib.image.enc.keywrapper;

import com.google.cloud.tools.jib.image.enc.OciEncryption.OptsHolder;
import com.google.common.annotations.VisibleForTesting;
import java.security.interfaces.RSAPublicKey;
import org.apache.cxf.rs.security.jose.jwa.ContentAlgorithm;
import org.apache.cxf.rs.security.jose.jwa.KeyAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.ContentEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweEncryption;
import org.apache.cxf.rs.security.jose.jwe.JweHeaders;
import org.apache.cxf.rs.security.jose.jwe.JweJsonProducer;
import org.apache.cxf.rs.security.jose.jwe.JweUtils;
import org.apache.cxf.rs.security.jose.jwe.KeyEncryptionProvider;

/**
 * Jwe {@link KeyWrapper}
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class JweKeyWrapper implements KeyWrapper {

  @Override
  public String wrapKeys(OptsHolder optsHolder) {
    if (optsHolder.isAes()) {
      if (optsHolder.getPublicKey() != null && optsHolder.getPriOpts() != null) {
        return rsaEncrypt(((RSAPublicKey) optsHolder.getPublicKey()), optsHolder.getPriOpts());
      }
    } else {
      // TODO(Gsealy): support SM
      return "not support yet.";
    }
    return "";
  }

  @VisibleForTesting
  String rsaEncrypt(RSAPublicKey publicKey, byte[] plain) {
    KeyEncryptionProvider keyEncryption =
        JweUtils.getPublicKeyEncryptionProvider(publicKey, KeyAlgorithm.RSA_OAEP);

    ContentEncryptionProvider contentEncryption =
        JweUtils.getContentEncryptionProvider(ContentAlgorithm.A256GCM);

    JweHeaders protectedHeaders = new JweHeaders(KeyAlgorithm.RSA_OAEP, ContentAlgorithm.A256GCM);

    JweJsonProducer p = new JweJsonProducer(protectedHeaders, plain, true);
    return p.encryptWith(new JweEncryption(keyEncryption, contentEncryption));
  }
}
