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

package com.google.cloud.tools.jib.api;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Builds to a tarball archive.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * EncTarImage tarImage = EncTarImage.at(Paths.get("image.tar")
 *                              , Paths.get("key.pem")
 *                              , "JWE")
 *                             .named("myimage");
 * }</pre>
 */
public class EncTarImage {

  /**
   * TODO(Gsealy): how to config keyPath Constructs a {@link EncTarImage} with the specified path.
   *
   * @param tarPath the path to the tarball archive
   * @param keyPath the path to the key file
   * @param wrapType the wrap type of key
   * @return a new {@link EncTarImage}
   */
  public static EncTarImage at(Path tarPath, Path keyPath, String wrapType) {
    return new EncTarImage(tarPath, keyPath, wrapType);
  }

  private final Path tarPath;
  private final Path keyPath;
  private final String wrapType;
  @Nullable private ImageReference imageReference;

  /** Instantiate with {@link #at}. */
  private EncTarImage(Path tarPath, Path keyPath, String wrapType) {
    this.tarPath = tarPath;
    this.keyPath = keyPath;
    this.wrapType = wrapType;
  }

  /**
   * Sets the name of the image. This is the name that shows up when the tar is loaded by the Docker
   * daemon.
   *
   * @param imageReference the image reference
   * @return this
   */
  public EncTarImage named(ImageReference imageReference) {
    this.imageReference = imageReference;
    return this;
  }

  /**
   * Sets the name of the image. This is the name that shows up when the tar is loaded by the Docker
   * daemon.
   *
   * @param imageReference the image reference
   * @return this
   * @throws InvalidImageReferenceException if {@code imageReference} is not a valid image reference
   */
  public EncTarImage named(String imageReference) throws InvalidImageReferenceException {
    return named(ImageReference.parse(imageReference));
  }

  Path getTarPath() {
    return tarPath;
  }

  Path getKeyPath() {
    return keyPath;
  }

  public String getWrapType() {
    return wrapType;
  }

  Optional<ImageReference> getImageReference() {
    return Optional.ofNullable(imageReference);
  }
}
