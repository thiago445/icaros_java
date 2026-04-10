package xvgroup.icaros.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_tweets") // Renomeado para plural, uma convenção comum
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOVO: Campo para o título da publicação
    private String title;

    // Campo para o conteúdo de texto da publicação
    @Column(columnDefinition = "TEXT")
    private String messageContent;

    // NOVO: Campo para a URL da imagem ou vídeo do Azure
    private String mediaUrl;

    @CreationTimestamp
    private Instant creationTimestamp;

    // Relação com o autor da publicação
    @ManyToOne(fetch = FetchType.LAZY) // LAZY é melhor para performance
    @JoinColumn(name = "user_id")
    private User user;

    // NOVO: Relação para as curtidas (Muitos-para-Muitos)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_tweet_likes", // Nome da tabela de junção
            joinColumns = @JoinColumn(name = "tweet_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>(); // Usar Set para garantir que um usuário curta apenas uma vez

    // NOVO: Relação para os comentários (Um-para-Muitos)
    @OneToMany(
            mappedBy = "tweet", // "tweet" é o nome do campo na entidade Comment
            cascade = CascadeType.ALL,
            orphanRemoval = true // Se um tweet for deletado, seus comentários também serão
    )
    private List<Comment> comments = new ArrayList<>(); // Usar List para manter a ordem dos comentários

}