package com.google.cloud.tools.jib.image.enc;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.google.cloud.tools.jib.image.enc.EncOciConfig.EnvelopeType;
import com.google.cloud.tools.jib.image.enc.keywrapper.JweKeyWrapper;
import com.google.cloud.tools.jib.image.enc.keywrapper.KeyWrapper;
import com.google.cloud.tools.jib.image.enc.keywrapper.Pkcs11KeyWrapper;
import com.google.cloud.tools.jib.image.enc.keywrapper.Pkcs7KeyWrapper;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * OciEncryption
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class OciEncryption {

  /** support ocicrypt, same as it */
  @SuppressWarnings("unused")
  private static final String AES_ALGO = "AES_256_CTR_HMAC_SHA256";

  @SuppressWarnings("unused")
  private static final String SM_ALGO = "SM4_CTR_HMAC_SM3";

  static {
    Security.addProvider(new BouncyCastleProvider());
    try {
      SR = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable private static Cipher cipher;
  @Nullable private static Mac hmac;

  private static final SecureRandom SR;

  private static final Map<EnvelopeType, KeyWrapper> WRAPPER_MAP = new HashMap<>(6);

  private static final OptsHolder CONFIG = new OptsHolder();

  /** inner config holder */
  public static class OptsHolder {

    /** 镜像 Layer, 对称密钥 */
    @Nullable private byte[] symmetricKey;

    /** 对称加密, Nonce或者IV */
    @Nullable private byte[] nonce;

    /** 公钥信息 */
    @Nullable private PublicKey publicKey;

    /**
     * 压缩的加密信息明文，包括对称密钥，layer杂凑，nonce。例：<br>
     *
     * <pre>
     * {
     *     "symkey": "lCweCGkNKc2xTSFhLJT0zi0KnyqoPz6LleHsDiHPwmo=",
     *     "digest": "sha256: 516faecaf516999e53ceefe29a76ef1dbd51e6f1d02747e3af942b62a21691cd",
     *     "cipheroptions": {
     *         "nonce": "+w9lFX/agy2d5qZ837ImnQ=="
     *     }
     * }
     * </pre>
     */
    @Nullable private byte[] priOpts;

    /** 根据私钥类型, 判断使用AES还是SM4加密 */
    public boolean isAes;

    @Nullable
    public byte[] getSymmetricKey() {
      return symmetricKey;
    }

    public void setSymmetricKey(@Nullable byte[] symmetricKey) {
      this.symmetricKey = symmetricKey;
    }

    @Nullable
    public byte[] getNonce() {
      return nonce;
    }

    public void setNonce(@Nullable byte[] nonce) {
      this.nonce = nonce;
    }

    @Nullable
    public PublicKey getPublicKey() {
      return publicKey;
    }

    public void setPublicKey(@Nullable PublicKey publicKey) {
      this.publicKey = publicKey;
    }

    @Nullable
    public byte[] getPriOpts() {
      return priOpts;
    }

    public void setPriOpts(@Nullable byte[] priOpts) {
      this.priOpts = priOpts;
    }

    public boolean isAes() {
      return isAes;
    }

    public void setAes(boolean aes) {
      isAes = aes;
    }
  }

  public void init(Path keyPath) throws IOException {
    registryKeyWrapper();
    boolean useAes = guessAlgorithm(keyPath);
    CONFIG.setAes(useAes);
    // prepare cipher key
    byte[] symmetricKey;
    if (useAes) {
      // aes 256
      symmetricKey = new byte[256 / 8];
    } else {
      symmetricKey = new byte[128 / 8];
    }
    SR.nextBytes(symmetricKey);
    CONFIG.setSymmetricKey(symmetricKey);
    SecretKey key;
    if (useAes) {
      key = new SecretKeySpec(symmetricKey, "AES");
      try {
        cipher = Cipher.getInstance("AES/CTR/NoPadding");
        hmac = Mac.getInstance("HmacSHA256");
      } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        throw new IOException(e);
      }
    } else {
      key = new SecretKeySpec(symmetricKey, "SM4");
      try {
        cipher = Cipher.getInstance("SM4/CTR/NoPadding");
        hmac = Mac.getInstance("HmacSM3");
      } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        throw new IOException(e);
      }
    }
    try {
      if (cipher == null) {
        throw new IOException("can't init Cipher, cipher is null");
      }
      byte[] iv = new byte[128 / 8];
      SR.nextBytes(iv);
      CONFIG.setNonce(iv);

      cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
      hmac.init(new SecretKeySpec(Objects.requireNonNull(CONFIG.getSymmetricKey()), "HmacSHA256"));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new IOException(e);
    }
  }

  /** registry {@link KeyWrapper} to Map */
  private static void registryKeyWrapper() {
    WRAPPER_MAP.put(EnvelopeType.JWE, new JweKeyWrapper());
    WRAPPER_MAP.put(EnvelopeType.PKCS7, new Pkcs7KeyWrapper());
    WRAPPER_MAP.put(EnvelopeType.PKCS11, new Pkcs11KeyWrapper());
  }

  /**
   * guess use which Algorithm, RSA or SM
   *
   * @return use RSA? else use SM
   */
  public boolean guessAlgorithm(Path keyPath) throws IOException {
    final byte[] allBytes = Files.readAllBytes(keyPath);
    try {
      final PublicKey publicKey = EncUtils.readPublicKey(allBytes);
      if (publicKey != null) {
        CONFIG.setPublicKey(publicKey);
        return "RSA".equalsIgnoreCase(publicKey.getAlgorithm());
      }
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e);
    }
    return false;
  }

  public EncOciResult encryptLayer(EncOciConfig ociConfig, byte[] plainLayer) throws IOException {
    final EncOciResult encOciResult = new EncOciResult();
    if (cipher == null) {
      throw new IOException("Cipher is null");
    }
    if (hmac == null) {
      throw new IOException("Mac is null");
    }
    Path tempFile = Files.createTempFile(null, ".enc.tmp");
    tempFile.toFile().deleteOnExit();
    try (ByteArrayInputStream is = new ByteArrayInputStream(plainLayer);
        OutputStream wbc = Files.newOutputStream(tempFile, CREATE, WRITE)) {
      byte[] b = new byte[8192];
      int offset;
      while ((offset = is.read(b)) != -1) {
        final byte[] out = cipher.update(b, 0, offset);
        hmac.update(out);
        wbc.write(out);
      }
      byte[] aFinal = cipher.doFinal();
      wbc.write(aFinal);

      encOciResult.setEncryptedTempFile(tempFile);
      // set mac
      final byte[] encMac = hmac.doFinal();
      ociConfig.getPubOpts().setHmac(encMac);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      // ignore
    }

    // prepare pri/pub opts
    if (CONFIG.getSymmetricKey() != null) {
      ociConfig.getPriOpts().setSymkey(CONFIG.getSymmetricKey());
    }
    if (CONFIG.isAes()) {
      ociConfig.getPubOpts().setCipher(AES_ALGO);
    } else {
      ociConfig.getPubOpts().setCipher(SM_ALGO);
    }
    if (CONFIG.getNonce() != null) {
      ociConfig.getPriOpts().addCipheroptions("nonce", CONFIG.getNonce());
    }

    CONFIG.setPriOpts(JsonTemplateMapper.toByteArray(ociConfig.getPriOpts()));
    encOciResult.setMac(JsonTemplateMapper.toByteArray(ociConfig.getPubOpts()));

    final EnvelopeType type = ociConfig.getType();
    KeyWrapper keyWrapper = WRAPPER_MAP.get(type);
    if (keyWrapper != null) {
      final String wrapKeys = keyWrapper.wrapKeys(CONFIG);
      encOciResult.setKeyWrap(wrapKeys);
    }

    return encOciResult;
  }
}
