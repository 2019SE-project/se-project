package seproject.tinyurl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlController {

    @Autowired
    UrlRepository ur;

    @GetMapping(value = "/{tinyurl}")
    public String getLongUrl(@RequestBody String tinyurl) {
        Url u = ur.findByTinyurl(tinyurl);
        return u.getLongurl();
    }

    @PutMapping(value = "/{longurl}")
    public String generateTinyUrl(@RequestBody String longurl) {
        Url u = new Url(longurl);
        return u.getTinyurl();
    }

}
