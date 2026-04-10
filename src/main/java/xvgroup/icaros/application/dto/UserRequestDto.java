package xvgroup.icaros.application.dto;


import java.util.List;


public record UserRequestDto(String name,
                             String cpf,
                             String email,
                             List<String> musicalGenre,
                             String role,
                             String password) {

}
