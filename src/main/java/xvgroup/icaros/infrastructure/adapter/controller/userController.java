package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xvgroup.icaros.application.dto.UserRequestDto;

import xvgroup.icaros.domain.service.UserService;


@RestController
public class userController {

   private UserService userService;

    public userController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<Void> CreateUser(@RequestBody UserRequestDto dto) {
        var user = userService.findByCpf(dto.cpf());
        if (user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this existing CPF");
        }
        userService.createUser(dto);
        return ResponseEntity.ok().build();

    }


}
