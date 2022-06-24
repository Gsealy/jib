package com.google.cloud.tools.jib.image.enc.keywrapper;

import com.google.cloud.tools.jib.image.enc.OciEncryption.OptsHolder;

/**
 * {@link KeyWrapper}
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public interface KeyWrapper {

  /**
   * wrap the symm key,
   *
   * @param optsHolder inner config
   * @return return {@link java.util.Base64} String
   */
  String wrapKeys(OptsHolder optsHolder);
}
