package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.MusicalGenre;

import java.util.Set;
import java.util.stream.Collectors;

public record MusicalGenreRequest(MusicalGenre musicalGenre) {


    public static Set<MusicalGenre> toMusicalGenre(Set<MusicalGenre.Values> dto){

        return dto
                .stream()
                .map(MusicalGenre.Values::toMusicalGenre)
                .collect(Collectors.toSet());

    }
}
