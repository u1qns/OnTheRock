package ontherock.contents.application;

import ontherock.contents.domain.Hashtag;
import ontherock.contents.domain.HashtagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HashtagService {

    @Autowired
    private HashtagRepository hashtagRepository;

    public Hashtag createHashtag(String keyword) {
        return hashtagRepository.findByKeyword(keyword)
                .orElseGet(() -> hashtagRepository.save(new Hashtag(null, keyword)));
    }
}
