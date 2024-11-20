package ontherock.contents.application;

import ontherock.contents.domain.Post;
import ontherock.contents.domain.PostHashtag;
import ontherock.contents.domain.PostHashtagRepository;
import ontherock.contents.dto.response.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostSearchService {

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    public List<PostResponse> findPostsByHashtag(String keyword) {
        List<PostHashtag> postHashtags = postHashtagRepository.findByHashtagKeyword(keyword);
        return postHashtags.stream()
                .map(PostHashtag::getPost)
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }
}
