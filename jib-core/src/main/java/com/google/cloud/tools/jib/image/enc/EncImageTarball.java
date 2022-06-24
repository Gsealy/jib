/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.image.enc;

import static com.google.cloud.tools.jib.image.enc.EncOciConfig.EnvelopeType;

import com.google.cloud.tools.jib.api.DescriptorDigest;
import com.google.cloud.tools.jib.api.ImageReference;
import com.google.cloud.tools.jib.blob.BlobDescriptor;
import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.hash.Digests;
import com.google.cloud.tools.jib.image.Image;
import com.google.cloud.tools.jib.image.Layer;
import com.google.cloud.tools.jib.image.enc.opts.PrivateLayerBlockCipherOptions;
import com.google.cloud.tools.jib.image.enc.opts.PublicLayerBlockCipherOptions;
import com.google.cloud.tools.jib.image.json.EncOciManifestTemplate;
import com.google.cloud.tools.jib.image.json.ImageToJsonTranslator;
import com.google.cloud.tools.jib.image.json.OciIndexTemplate;
import com.google.cloud.tools.jib.image.json.OciManifestTemplate;
import com.google.cloud.tools.jib.json.JsonTemplate;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import com.google.cloud.tools.jib.tar.TarStreamBuilder;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Translates an {@link Image} to a tarball that can be loaded into Docker. */
public class EncImageTarball {

  /** Time that entry is set in the tar. */
  private static final Instant TAR_ENTRY_MODIFICATION_TIME = Instant.EPOCH;

  private static final String LAYER_ANNOTATION_KEY_PREFIX = "org.opencontainers.image.enc.keys.";
  private static final String LAYER_ANNOTATION_PUBOPTS = "org.opencontainers.image.enc.pubopts";

  private final Image image;
  private final ImageReference imageReference;

  /** public key for encryption */
  private final Path keyPath;

  /** encryption method */
  private final OciEncryption encryption = new OciEncryption();

  /** key wrap type */
  private final String wrapType;

  /**
   * Instantiate with an {@link Image}.
   *
   * @param image the image to convert into a tarball
   * @param imageReference image reference to set in the manifest (note that the tag portion of the
   *     image reference is ignored)
   * @param allTargetImageTags the tags to tag the image with
   * @param keyPath public key path
   * @param wrapType use which method wrap key
   */
  public EncImageTarball(
      Image image,
      ImageReference imageReference,
      ImmutableSet<String> allTargetImageTags,
      Path keyPath,
      String wrapType) {
    this.image = image;
    this.imageReference = imageReference;
    this.keyPath = keyPath;
    this.wrapType = wrapType;
  }

  /**
   * Writes image tar bar in configured {@link Image#getImageFormat()} of OCI or Docker to output
   * stream.
   *
   * @param out the target output stream
   * @throws IOException if an error occurs writing out the image to stream
   */
  public void writeTo(OutputStream out) throws IOException {
    if (image.getImageFormat() == OciManifestTemplate.class) {
      ociWriteToEnc(out);
    } else {
      throw new IOException("Not Support Docker yet.");
    }
  }

  /** actually build enc image here, like enc inputStream etc. */
  private void ociWriteToEnc(OutputStream out) throws IOException {
    TarStreamBuilder tarStreamBuilder = new TarStreamBuilder();
    EncOciManifestTemplate manifest = new EncOciManifestTemplate();
    Map<String, String> annotations = new HashMap<>(2);

    final Image encImage = encryptImage();

    // Adds all the layers to the tarball and manifest
    for (int i = 0; i < encImage.getLayers().size(); i++) {
      annotations.clear();
      EncLayer encLayerInfo = ((EncLayer) encImage.getLayers().get(i));
      long size = encLayerInfo.getBlobDescriptor().getSize();
      DescriptorDigest encDigest = encLayerInfo.getBlobDescriptor().getDigest();
      tarStreamBuilder.addBlobEntry(
          encLayerInfo.getBlob(),
          size,
          "blobs/sha256/" + encDigest.getHash(),
          TAR_ENTRY_MODIFICATION_TIME);

      annotations.put(
          LAYER_ANNOTATION_KEY_PREFIX + wrapType.toLowerCase(), encLayerInfo.getKeyWrap());
      annotations.put(LAYER_ANNOTATION_PUBOPTS, encLayerInfo.getMac());
      manifest.addLayer(size, encDigest, annotations);
    }

    // Adds the container configuration to the tarball and manifest
    // use plain configuration
    JsonTemplate containerConfiguration =
        new ImageToJsonTranslator(image).getContainerConfiguration();
    BlobDescriptor configDescriptor = Digests.computeDigest(containerConfiguration);
    manifest.setContainerConfiguration(configDescriptor.getSize(), configDescriptor.getDigest());
    tarStreamBuilder.addByteEntry(
        JsonTemplateMapper.toByteArray(containerConfiguration),
        "blobs/sha256/" + configDescriptor.getDigest().getHash(),
        TAR_ENTRY_MODIFICATION_TIME);

    // Adds the manifest to the tarball
    BlobDescriptor manifestDescriptor = Digests.computeDigest(manifest);
    tarStreamBuilder.addByteEntry(
        JsonTemplateMapper.toByteArray(manifest),
        "blobs/sha256/" + manifestDescriptor.getDigest().getHash(),
        TAR_ENTRY_MODIFICATION_TIME);

    // Adds the oci-layout and index.json
    tarStreamBuilder.addByteEntry(
        "{\"imageLayoutVersion\": \"1.0.0\"}".getBytes(StandardCharsets.UTF_8),
        "oci-layout",
        TAR_ENTRY_MODIFICATION_TIME);
    OciIndexTemplate index = new OciIndexTemplate();
    // TODO: figure out how to tag with allTargetImageTags
    index.addManifest(manifestDescriptor, imageReference.toStringWithQualifier());
    tarStreamBuilder.addByteEntry(
        JsonTemplateMapper.toByteArray(index), "index.json", TAR_ENTRY_MODIFICATION_TIME);

    tarStreamBuilder.writeAsTarArchiveTo(out);
  }

  /**
   * Returns the total size of the image's layers in bytes.
   *
   * @return the total size of the image's layers in bytes
   */
  public long getTotalLayerSize() {
    long size = 0;
    for (Layer layer : image.getLayers()) {
      size += layer.getBlobDescriptor().getSize();
    }
    return size;
  }

  /** do encrypt image before tar output */
  public Image encryptImage() throws IOException {
    encryption.init(keyPath);
    // use origin image version and manifest media-type
    Image.Builder imageBuilder = Image.builder(image.getImageFormat());
    // copy all exclude layer info
    if (image.getCreated() != null) {
      imageBuilder.setCreated(image.getCreated());
    }
    imageBuilder.setArchitecture(image.getArchitecture());
    imageBuilder.setOs(image.getOs());
    if (image.getEntrypoint() != null) {
      imageBuilder.setEntrypoint(image.getEntrypoint());
    }
    if (image.getProgramArguments() != null) {
      imageBuilder.setProgramArguments(image.getProgramArguments());
    }
    if (image.getHealthCheck() != null) {
      imageBuilder.setHealthCheck(image.getHealthCheck());
    }
    if (image.getWorkingDirectory() != null) {
      imageBuilder.setWorkingDirectory(image.getWorkingDirectory());
    }
    if (image.getUser() != null) {
      imageBuilder.setUser(image.getUser());
    }

    for (Layer layer : image.getLayers()) {
      byte[] blobArray = Blobs.writeToByteArray(layer.getBlob());
      final PrivateLayerBlockCipherOptions priOpts = new PrivateLayerBlockCipherOptions();
      final PublicLayerBlockCipherOptions pubOpts = new PublicLayerBlockCipherOptions();
      // TODO(Gsealy): just set origin layer hash here. need hash with algo prefix
      priOpts.setDigest(layer.getBlobDescriptor().getDigest().toString());
      // do encrypt
      EncOciResult ociResult =
          encryption.encryptLayer(
              new EncOciConfig(keyPath, EnvelopeType.valueOf(wrapType), priOpts, pubOpts),
              blobArray);
      EncLayer encLayer = EncLayer.from(ociResult);
      imageBuilder.addLayer(encLayer);
    }

    return imageBuilder.build();
  }
}
