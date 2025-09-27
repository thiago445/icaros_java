package xvgroup.icaros.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_musicalGenre")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MusicalGenre {
    @Id
    @Column(name = "musicalGenre_Id")
    private Long musicalGenreId;

   private String genre;


    @Getter
    public enum Values{
        SAMBA(1L, "samba"),
        PAGODE(2L, "pagode"),
        FORRO(3L, "forro"),
        SERTANEJO(4L, "sertanejo"),
        FUNK(5L, "funk");

        private long musicalGenreId;
        private String genre;

        Values(long musicalGenreId, String genre) {
            this.musicalGenreId = musicalGenreId;
            this.genre = genre;
        }

        public MusicalGenre toMusicalGenre(){
            return new MusicalGenre(musicalGenreId, genre);
        }
    }
}
