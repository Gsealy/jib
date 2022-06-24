package com.google.cloud.tools.jib.image.enc.keywrapper;

import com.google.cloud.tools.jib.image.enc.OciEncryption.OptsHolder;

/**
 * PKCS11 {@link KeyWrapper}
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class Pkcs11KeyWrapper implements KeyWrapper {

  @Override
  public String wrapKeys(OptsHolder optsHolder) {
    return "";
  }
}
