package ontherock.contents.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "climbing_gyms")
public class ClimbingGym {
    @Id
    private Long placeId;

    private String name;
    private String address;
}