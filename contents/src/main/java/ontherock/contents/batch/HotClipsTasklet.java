package ontherock.contents.batch;

import lombok.extern.slf4j.Slf4j;
import ontherock.contents.application.PostService;
import ontherock.contents.domain.PostWithLikes;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@JobScope
public class HotClipsTasklet implements Tasklet {

    @Autowired
    private PostService postService;

    @Autowired
    private HotClipsCache hotClipsCache;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        List<PostWithLikes> hotClips = postService.getHotClips();
        hotClipsCache.updateHotClips(hotClips);
        return RepeatStatus.FINISHED;
    }
}
