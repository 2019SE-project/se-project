package seproject.tinyurl.repository;

import org.springframework.data.repository.CrudRepository;
import seproject.tinyurl.entity.Url;


public interface UrlRepository extends CrudRepository<Url, Long> {
    Url findByTinyurl(String tinyurl);
}
