package org.demo.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HT11SpringWebFlux {

    // Для того чтобы Flapdoodle Embedded MongoDB работал на маках с процессорами m1
    static {
        System.setProperty("os.arch", "i686_64");
    }

    public static void main(String[] args) {
        SpringApplication.run(HT11SpringWebFlux.class, args);
    }
}
