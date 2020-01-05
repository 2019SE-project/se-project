package seproject.tinyurl;

import org.springframework.data.repository.CrudRepository;


public interface UrlRepository extends CrudRepository<Url, Long> {
    Url findByTinyurl(String tinyurl);
}
