package xvgroup.icaros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class IcarosApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcarosApplication.class, args);
	}

}
