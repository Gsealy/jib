# Build an encrypted image using Jib

> now Just support maven

This is an example of how to easily build a encrypted image for a Spring Boot application with Jib.

## Try it yourself

You can containerize the application with one of the following commands.

**Maven:**

```shell
make buildTar
```

<!-- Run a @springboot app on #Kubernetes in seconds @kubernetesio #jib #java -->

you can find the regular tarball and encrytped image tarball in `target` path.

```
ocicrypt-0.0.1.tar
ocicrypt-0.0.1.tar.encrypted
```

## config in pom.xml

- just support JWE wrap method;

- just support OCI format image;

- encrypted tarball extension name，`encrypted`

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.2.1</version>
    <configuration>
        <from>
            <platforms>
                <platform>
                    <os>linux</os>
                    <architecture>amd64</architecture>
                </platform>
            </platforms>
        </from>
        <to>
            <image>${registry.address}${project.artifactId}:${project.version}</image>
        </to>
        <container>
            <mainClass>org.containers.ocicrypt.OciCryptoJibApplication</mainClass>
            <format>OCI</format>
        </container>
        <outputPaths>
            <tar>${project.build.directory}\${project.artifactId}-${project.version}.tar</tar>
            <key>${project.basedir}\src\test\resources\pubkey.pem</key>
            <wrap>JWE</wrap>
        </outputPaths>
    </configuration>
</plugin>

```

we are add two element to do this.

- **&lt;key&gt;**, publickey path

- **&lt;wrap&gt;**, wrap method, include: JWE/PKCS11/PKCS7, **just support JWE now**.

## More information

Learn [more about Jib](https://github.com/GoogleContainerTools/jib).

Learn [more about ocicrypt](https://github.com/containers/ocicrypt).