package Seguridad_en_APP_Web.demo;

import Seguridad_en_APP_Web.demo.model.Rol;
import Seguridad_en_APP_Web.demo.repository.CustomUserRepository;
import Seguridad_en_APP_Web.demo.service.CustomUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
