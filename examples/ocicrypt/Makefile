all: package

help:
	@echo "Usage: make <target>"
	@echo
	@echo " * 'clean' - Remove all build and test binaries."
	@echo " * 'compile' - Compile oci-crypto-jib source."
	@echo " * 'package' - Package all source to jar file."
	@echo " * 'buildTar' - Build the encrypted image tar file with 'package'."

clean:
	./mvnw clean

compile:
	./mvnw compile -DskipTests

package:
	./mvnw package -DskipTests

buildTar:
	./mvnw package -DskipTests jib:buildTar

.PHONY: help compile package buildTar