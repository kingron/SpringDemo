package tacos.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * SQL Helper
 */
@Component
@Slf4j
public class SqlHelper {
    private static JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlHelper(JdbcTemplate jdbcTemplate) {
        SqlHelper.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Query for first column of first record
     *
     * @param sql SQL statement
     * @return first column integer of first record
     */
    public static Integer queryForInt(String sql) {
        Map<String, Object> map = jdbcTemplate.queryForMap(sql);
        Object obj = map.values().iterator().next();
        if (obj == null) {
            return null;
        }
        return Integer.parseInt(obj.toString());
    }

    public static String version() {
        return queryForString("select version()");
    }

    /**
     * Query for single record with single field
     *
     * @param sql SQL statement
     * @return first column value of first record
     */
    public static String queryForString(String sql) {
        Map<String, Object> map = jdbcTemplate.queryForMap(sql);
        Object obj = map.values().iterator().next();
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * Run SQL and return List Object
     * <p>
     * Usage:
     * <pre>{@code
     *         RowMapper<Result> mapper = new BeanPropertyRowMapper<>(Result.class);
     *         List<Result> ret = SqlHelper.query("select * from ete_result", mapper);
     * }</pre>
     *
     * @param sql       SQL statement
     * @param rowMapper RowMapper
     * @param <T>       Type
     * @return List Object Of T
     */
    public static <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    public static void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}