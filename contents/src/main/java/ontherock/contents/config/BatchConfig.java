package ontherock.contents.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ontherock.contents.batch.HotClipsTasklet;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job hotClipsJob(JobRepository jobRepository, Step hotClipsStep) {
        return new JobBuilder("hotClipsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(hotClipsStep)
                .build();
    }

    @Bean
    public Step hotClipsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, HotClipsTasklet hotClipsTasklet) {
        return new StepBuilder("hotClipsStep", jobRepository)
                .tasklet(hotClipsTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
