package ontherock.contents.presentation;

import ontherock.contents.application.HashtagService;
import ontherock.contents.domain.Hashtag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contents")
public class HashtagController {

    @Autowired
    private HashtagService hashtagService;

    @PostMapping("/hashtags")
    public ResponseEntity<Hashtag> createHashtag(@RequestBody String keyword) {
        Hashtag hashtag = hashtagService.createHashtag(keyword);
        return ResponseEntity.ok(hashtag);
    }
}
