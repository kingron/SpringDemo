package tacos;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

@Slf4j
@SpringBootApplication
public class TacoCloudApplication {

    public static void main(String[] args) {
        if (!CollectionUtils.isEmpty(Arrays.asList(args)) && "generate".equals(args[0])) {
            runGenerate();
            return;
        }
        SpringApplication.run(TacoCloudApplication.class, args);
    }

    private static void runGenerate() {
        System.out.print("\n\n\n\nPlease input your password: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String password = br.readLine();

            if (!StringUtils.hasText(password)) {
                log.error("you need input some password text");
                System.exit(-1);
                return;
            }
            String encryptPassword = generateEncryptPassword("your-salt", password);
            System.out.println("Your encrypted password: " + encryptPassword);
        } catch (IOException e) {
            log.error("Failed to encrypt password: {}", e.getMessage());
            System.exit(-1);
        } finally {
            System.exit(0);
        }
    }

    private static String generateEncryptPassword(String salt, String password) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(salt);
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor.encrypt(password);
    }
}
