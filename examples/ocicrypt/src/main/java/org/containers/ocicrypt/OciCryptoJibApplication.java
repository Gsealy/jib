package org.containers.ocicrypt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class OciCryptoJibApplication {

    public static void main(String[] args) {
        SpringApplication.run(OciCryptoJibApplication.class, args);
    }

    @RestController
    public static class JibController {

        @GetMapping("/")
        public String hello() {
            return "Hello encrypted image, build with Jib!";
        }
    }
}
