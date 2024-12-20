package tcs.system.lib_common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tcs.system.lib_common.fileUtil.fileSystem.FileSystemProperties;
import tcs.system.lib_common.keycloak.KeycloakProperties;

@SpringBootApplication
@EnableConfigurationProperties({KeycloakProperties.class, FileSystemProperties.class})
public class LibCommonApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibCommonApplication.class, args);
	}

}
