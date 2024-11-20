package ontherock.contents.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job hotClipsJob;

    //    @Scheduled(fixedRate = 60000) 1분마다 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void runHotClipsJob() {
        try {
            jobLauncher.run(hotClipsJob, new JobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
