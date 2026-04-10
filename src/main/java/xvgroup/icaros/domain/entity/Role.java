package xvgroup.icaros.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "tb_role")
public class Role {
    @Id
    private Long roleId;

    private String description;

    public Role() {
    }

    public Role(Long roleId, String description) {
        this.roleId = roleId;
        this.description = description;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Getter
    public enum Values {
        MUSICIAN(1L, "musician"),
        PRODUCER(2L, "producer"),
        LOVER(3L, "lover");

        private Long id;
        private String description;

        Values(Long id, String description) {
            this.id = id;
            this.description = description;
        }

        public Role toRole() {
            return new Role(id, description);
        }

    }
}