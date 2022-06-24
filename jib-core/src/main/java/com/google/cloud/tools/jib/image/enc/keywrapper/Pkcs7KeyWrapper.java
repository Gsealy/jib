package com.google.cloud.tools.jib.image.enc.keywrapper;

import com.google.cloud.tools.jib.image.enc.OciEncryption.OptsHolder;

/**
 * PKCS7 {@link KeyWrapper}
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class Pkcs7KeyWrapper implements KeyWrapper {

  @Override
  public String wrapKeys(OptsHolder optsHolder) {
    return "";
  }
}
