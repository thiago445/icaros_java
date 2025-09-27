package xvgroup.icaros.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToMany()
    @JoinTable(name = "tb_event_musicalGenre",
    joinColumns = @JoinColumn(name = "event_id"),
    inverseJoinColumns = @JoinColumn(name = "musicalGenre_id"))
    private Set<MusicalGenre> musicalGenres;

    @ManyToOne(optional = false)
    @JoinColumn(name = "eventLeader_id")
    private User eventLeader;

    @ManyToMany()
    @JoinTable(name = "tb_event_performers",
    joinColumns = @JoinColumn(name = "event_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> performers;


    @ManyToMany()
    @JoinTable(name = "tb_event_users",
                joinColumns = @JoinColumn(name = "event_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> attendees;

    @CreationTimestamp
    private Instant creationTimesTamp;

    private Instant dateTime;

    public Event(UUID id, String name, Set<MusicalGenre> musicalGenres, User eventLeader, Set<User> performers, Set<User> attendees, Instant creationTimesTamp, Instant dateTime) {
        this.id = id;
        this.name = name;
        this.musicalGenres = musicalGenres;
        this.eventLeader = eventLeader;
        this.performers = performers;
        this.attendees = attendees;
        this.creationTimesTamp = creationTimesTamp;
        this.dateTime = dateTime;
    }

    public Event() {
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMusicalGenres(Set<MusicalGenre> musicalGenres) {
        this.musicalGenres = musicalGenres;
    }

    public void setEventLeader(User eventLeader) {
        this.eventLeader = eventLeader;
    }

    public void setPerformers(Set<User> performers) {
        this.performers = performers;
    }

    public void setAttendees(Set<User> attendees) {
        this.attendees = attendees;
    }

    public void setCreationTimesTamp(Instant creationTimesTamp) {
        this.creationTimesTamp = creationTimesTamp;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<MusicalGenre> getMusicalGenres() {
        return musicalGenres;
    }

    public User getEventLeader() {
        return eventLeader;
    }

    public Set<User> getPerformers() {
        return performers;
    }

    public Set<User> getAttendees() {
        return attendees;
    }

    public Instant getCreationTimesTamp() {
        return creationTimesTamp;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
