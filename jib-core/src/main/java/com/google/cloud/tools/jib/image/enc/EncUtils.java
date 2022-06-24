package com.google.cloud.tools.jib.image.enc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.annotation.Nullable;

/**
 * EncUtils
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class EncUtils {

  /**
   * Read a X509 encoded public key. Maybe DER or PEM encoded.
   *
   * @param bytes the key data as a byte array.
   * @return parsed public key.
   * @throws IOException *
   * @throws NoSuchAlgorithmException *
   */
  @Nullable
  public static PublicKey readPublicKey(final byte[] bytes)
      throws IOException, NoSuchAlgorithmException {
    byte[] data = bytes;
    // decode from PEM format
    if (((char) data[0]) == '-') {
      data = convertPEMToDER(new String(data, StandardCharsets.UTF_8));
    }
    X509EncodedKeySpec spec = new X509EncodedKeySpec(data);

    try {
      return KeyFactory.getInstance("RSA").generatePublic(spec);
    } catch (InvalidKeySpecException e) {
      try {
        return KeyFactory.getInstance("EC").generatePublic(spec);
      } catch (InvalidKeySpecException e2) {
        // ignore
      }
    }
    return null;
  }

  /**
   * Read a PEM format.
   *
   * <p>This does not currently support encrypted PEM formats.
   *
   * @param key string containing PEM formatted data.
   * @return DER formatted data.
   * @throws IOException *
   */
  public static byte[] convertPEMToDER(final String key) throws IOException {
    if (!key.contains("PUBLIC")) {
      throw new IOException("not a PEM format public key");
    }
    String publickeypem =
        key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

    return Base64.getMimeDecoder().decode(publickeypem);
  }
}
