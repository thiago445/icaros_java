package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xvgroup.icaros.domain.service.AzureStorageService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final AzureStorageService azureStorageService;

    @Autowired
    public FileUploadController(AzureStorageService azureStorageService) {
        this.azureStorageService = azureStorageService;
    }

    @PostMapping("/upload/post")
    public ResponseEntity<Map<String, String>> uploadPostMedia(@RequestParam("file") MultipartFile file) {
        try {
            // Define o contêiner para mídias de posts
            String containerName = "media-posts";
            String fileUrl = azureStorageService.uploadFile(file, containerName);

            // Retorna a URL do arquivo para o cliente
            // Agora o frontend pode usar essa URL, e o backend pode salvá-la no banco de dados
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha no upload do arquivo."));
        }
    }
}