package ontherock.contents.application;

import ontherock.contents.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostHashtagService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    public void addHashtagsToPost(long postId, List<String> keywords) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        for (String keyword : keywords) {
            Hashtag hashtag = hashtagRepository.findByKeyword(keyword)
                    .orElseGet(() -> hashtagRepository.save(new Hashtag(null, keyword)));

            PostHashtag postHashtag = new PostHashtag();
            postHashtag.setPost(post);
            postHashtag.setHashtag(hashtag);
            postHashtagRepository.save(postHashtag);
        }
    }
}
