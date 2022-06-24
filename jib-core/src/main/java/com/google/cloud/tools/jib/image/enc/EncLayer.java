package com.google.cloud.tools.jib.image.enc;

import static java.nio.file.StandardOpenOption.READ;

import com.google.cloud.tools.jib.api.DescriptorDigest;
import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.blob.BlobDescriptor;
import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.hash.Digests;
import com.google.cloud.tools.jib.image.Layer;
import com.google.cloud.tools.jib.image.LayerPropertyNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Enc Layer construct
 *
 * @author <a href="mailto:gsealy@outlook.com">Gsealy</a>
 */
public class EncLayer implements Layer {

  public static EncLayer from(EncOciResult ociResult) throws IOException {
    Path encLayer = ociResult.getTempPath();
    if (encLayer == null) {
      throw new IOException("Need encLayer");
    }
    Blob encBlob = Blobs.from(encLayer);
    final BlobDescriptor encDescriptor =
        Digests.computeDigest(Files.newInputStream(encLayer, READ));
    String keyWrap = ociResult.getKeyWrap();
    String mac = ociResult.getMac();
    if (mac == null) {
      throw new IOException("Need Mac Value");
    }
    if (keyWrap == null) {
      throw new IOException("Need keyWrap Value");
    }
    return new EncLayer(encBlob, encDescriptor, encDescriptor.getDigest(), mac, keyWrap);
  }

  private final Blob blob;
  private final BlobDescriptor descriptor;
  private final DescriptorDigest diffId;
  private final String mac;
  private final String keyWrap;

  public EncLayer(
      Blob blob, BlobDescriptor descriptor, DescriptorDigest diffId, String mac, String keyWrap) {
    this.blob = blob;
    this.descriptor = descriptor;
    this.diffId = diffId;
    this.mac = mac;
    this.keyWrap = keyWrap;
  }

  @Override
  public Blob getBlob() throws LayerPropertyNotFoundException {
    return blob;
  }

  @Override
  public BlobDescriptor getBlobDescriptor() throws LayerPropertyNotFoundException {
    return descriptor;
  }

  @Override
  public DescriptorDigest getDiffId() throws LayerPropertyNotFoundException {
    return diffId;
  }

  public String getMac() {
    return mac;
  }

  public String getKeyWrap() {
    return keyWrap;
  }
}
