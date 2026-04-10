package xvgroup.icaros.infrastructure.adapter.controller;


import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xvgroup.icaros.application.dto.LoginRequest;
import xvgroup.icaros.application.dto.LoginResponse;
import xvgroup.icaros.domain.service.TokenService;

@RestController
public class TokenController {


    private final TokenService tokenService;


    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse>login(@RequestBody LoginRequest loginRequest){
        var LoginResponse = tokenService.Login(loginRequest);

        return ResponseEntity.ok(LoginResponse);

    }




}

