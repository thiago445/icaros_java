package xvgroup.icaros.domain.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureStorageService {
    private final BlobServiceClient blobServiceClient;

    @Autowired
    public AzureStorageService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    /**
     * Faz o upload de um arquivo para o Azure Blob Storage.
     *
     * @param file O arquivo a ser enviado (vindo do controller).
     * @param containerName O nome do contêiner (ex: "imagens-perfil").
     * @return A URL do arquivo após o upload.
     * @throws java.io.IOException Se ocorrer um erro ao ler o arquivo.
     */
    public String uploadFile(MultipartFile file, String containerName) throws IOException {
        // Gera um nome de arquivo único para evitar colisões
        String blobName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        // Obtém o cliente do contêiner. Cria o contêiner se ele não existir.
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        containerClient.createIfNotExists();

        // Obtém o cliente do blob (o arquivo em si)
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Faz o upload dos dados do arquivo
        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true); // O 'true' sobrescreve se já existir
        }

        // Retorna a URL do blob para ser salva no banco de dados
        return blobClient.getBlobUrl();
    }

}
