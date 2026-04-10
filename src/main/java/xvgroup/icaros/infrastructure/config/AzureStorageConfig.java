package xvgroup.icaros.infrastructure.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    // Injeta o valor da sua connection string do application.properties
    @Value("${azure.storage.connection-string}")
    private String connectionString;

    // A anotação @Bean diz ao Spring: "Aqui    está o BlobServiceClient que você precisa!"
    @Bean
    public BlobServiceClient blobServiceClient() {
        // Cria o cliente de serviço de blob manualmente usando um builder
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}