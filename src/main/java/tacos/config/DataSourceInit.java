package tacos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Automatically init the database and data if needed
 *
 * @author lrr670
 * @since 1.0
 */
@Configuration
@Slf4j
public class DataSourceInit {
    @Value("classpath:db/schema.sql")
    private Resource ddl;
    @Value("classpath:db/data.sql")
    private Resource dml;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator(dataSource));
        return initializer;
    }

    private DatabasePopulator databasePopulator(DataSource dataSource) {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            jdbcTemplate.queryForMap("select count(1) from ete_result");
        } catch (DataAccessException e) {
            log.warn("初始化脚本报错:" + e.getMessage());
            // 报错,表不存在,初次导入，执行相关脚本
            populator.addScripts(ddl);
            populator.addScripts(dml);
        } catch (Exception e) {
            log.warn("初始化脚本报错:" + e.getMessage());
        }
        return populator;
    }

}