package seproject.tinyurl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import seproject.tinyurl.entity.Encoder;
import seproject.tinyurl.entity.Url;
import seproject.tinyurl.repository.UrlRepository;

import javax.websocket.server.PathParam;

@RestController
public class UrlController {

    @Autowired
    UrlRepository ur;

    @GetMapping(value = "/{tinyurl}")
    public String getLongUrl(@PathParam("url") String url) {
        System.out.println("tinyurl: ");
        System.out.println(url);
        Url u = ur.findByTinyurl(url);
        return u.getLongurl();
    }

    @PostMapping(value = "/longurl")
    public String generateTinyUrl(@PathParam("url") String url) {
        System.out.println("longurl: ");
        System.out.println(url);
        Url u = new Url(url);
        ur.save(u);
        return u.getTinyurl();
    }

}
