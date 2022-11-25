package tacos.config;

import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

// https://cloud.tencent.com/developer/article/1947192

/**
 * Quartz 配置类
 * <p>
 * 该类提供Quartz配置，同时也自动加载数据库中和文件的任务配置：<ul>
 * <li>定时间隔和时间点</li>
 * <li>要检查和的页面地址和API清单</li>
 * </ul>
 */
@Configuration
public class QuartzConfig {
    @Autowired
    private JobFactory jobFactory;
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    /**
     * 当触发器触发时，与之关联的任务被Scheduler中配置的JobFactory实例化，也就是每触发一次，就会创建一个任务的实例化对象
     * (如果缺省)则调用Job类的newInstance方法生成一个实例
     * (这里选择自定义)并将创建的Job实例化交给IoC管理
     *
     * @return JobFactory
     */
    @Bean
    public JobFactory jobFactory() {
        return new AdaptableJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object jobInstance = super.createJobInstance(bundle);
                capableBeanFactory.autowireBean(jobInstance);
                return jobInstance;
            }
        };
    }

    /**
     * 配置SchedulerFactoryBean
     * <p>
     * 将一个方法产生为Bean并交给Spring容器管理
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
        // Spring提供SchedulerFactoryBean为Scheduler提供配置信息,并被Spring容器管理其生命周期
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        //这句一定要加！！！！不然properties配置不生效！！！！
        factory.setConfigLocation(new ClassPathResource("/quartz.properties"));
        factory.setStartupDelay(1);
        factory.afterPropertiesSet();
        // 设置自定义Job Factory，用于Spring管理Job bean
        return factory;
    }

    @Bean(name = "scheduler")
    public Scheduler scheduler() throws Exception {
        return schedulerFactoryBean().getScheduler();
    }


}