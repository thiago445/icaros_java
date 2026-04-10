package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.RequestProfile;
import xvgroup.icaros.application.dto.ResponseProfile;
import xvgroup.icaros.domain.service.AzureStorageService;
import xvgroup.icaros.domain.service.ProfileService;

import java.io.IOException;
import java.util.UUID;

@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final AzureStorageService azureStorageService;

    public ProfileController(ProfileService profileService, AzureStorageService azureStorageService) {
        this.profileService = profileService;
        this.azureStorageService = azureStorageService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createProfile(
            @RequestPart("profileData") RequestProfile dto,
            @RequestPart("profilePicture") MultipartFile profilePictureFile,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhotoFile,
            JwtAuthenticationToken token)
    {
        try {
            String profilePictureUrl = azureStorageService.uploadFile(profilePictureFile, "profile-pictures");
            String coverPhotoUrl = null;

            if (coverPhotoFile != null && !coverPhotoFile.isEmpty()) {
                coverPhotoUrl = azureStorageService.uploadFile(coverPhotoFile, "cover-photos");
            }

            //save profile with URL of image
            profileService.createProfile(dto, profilePictureUrl, coverPhotoUrl,token);

            return ResponseEntity.status(HttpStatus.CREATED).body("profile was created with success!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error in upload of anybody.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error to create .");
        }
    }

    @GetMapping("/profile")
    ResponseEntity<ResponseProfile> getProfileUser( JwtAuthenticationToken token){

        System.out.println("esse é o token enviado: "+token);
        String userIdString = token.getToken().getSubject();

        // 2. Converte para UUID
        UUID userId = UUID.fromString(userIdString);
       ResponseProfile profile= profileService.getProfile(userId);

       return ResponseEntity.ok(profile);
        

    }

}

