package seproject.tinyurl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "urls")
@Table(appliesTo = "urls")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String longurl;

    @Column
    private String tinyurl;

    static Encoder encoder = new Encoder();

    public Url(String url){
        longurl = url;
        int number = longurl.hashCode();
        tinyurl = encoder.encode(number);
    }

}
