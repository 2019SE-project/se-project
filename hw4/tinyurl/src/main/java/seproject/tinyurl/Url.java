package seproject.tinyurl;

import lombok.Data;

import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String longurl;
    private String tinyurl;

    static Encoder encoder;

    public Url(String url){
        longurl = longurl;
        tinyurl = encoder.encode(id);
    }

}
