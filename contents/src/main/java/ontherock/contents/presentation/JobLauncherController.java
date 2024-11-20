package ontherock.contents.presentation;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contents/batch")
public class JobLauncherController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job hotClipsJob;

    @PostMapping("/hotclips")
    public ResponseEntity<String> launchHotClipsJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(hotClipsJob, jobParameters);
            return ResponseEntity.ok("HotClips job started successfully.");
        } catch (JobExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("HotClips job failed to start: " + e.getMessage());
        }
    }
}
