package ontherock.contents.batch;

import lombok.extern.slf4j.Slf4j;
import ontherock.contents.domain.PostWithLikes;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HotClipsCache {

    private final AtomicReference<List<PostWithLikes>> hotClips = new AtomicReference<>();

    public void updateHotClips(List<PostWithLikes> hotClips) {
        this.hotClips.set(hotClips);
        log.error("핫클립 업데이트했어욧 "+ hotClips);
    }

    public List<PostWithLikes> getHotClips() {
        return hotClips.get();
    }
}
