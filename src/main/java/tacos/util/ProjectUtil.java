package tacos.util;

import org.springframework.stereotype.Component;
import tacos.model.ScriptStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tacos.util.Util.loadResourceFile;
import static tacos.util.Util.strToInt;

/**
 * Project Utils
 * <p>
 * 项目专用工具类，无法公开其他项目使用，或者是项目特定依赖包相关的工具类
 *
 * @version 1.0
 * @since 11/3/2022
 */
@Component
public class ProjectUtil {
    public static final short SOURCE_SCHEDULE = 0;
    public static final short SOURCE_ADHOC = 1;

    private static final Map<String, String> mapConfig = new HashMap<>();
    private static final Map<String, String> mapTemplate = new HashMap<>();

    /**
     * load configuration from database or application.properties
     */
    public void loadConfig() {
        mapConfig.clear();
    }

    /**
     * get config value by name
     *
     * @param name the config name
     * @return config value string
     */
    public static String getConfig(String name) {
        return String.valueOf(mapConfig.get(name));
    }

    /**
     * get config value by name
     *
     * @param name         the config name
     * @param defaultValue default value if the config name not exist or parse error
     * @return config value
     */
    public static int getConfigInt(String name, int defaultValue) {
        return strToInt(mapConfig.get(name), defaultValue);
    }

    /**
     * load template from files or database
     */
    public void loadTemplate() {
        mapTemplate.clear();

        mapTemplate.put("LGN", loadResourceFile("case/LGN.json"));
        mapTemplate.put("D01", loadResourceFile("case/D01.json"));
        mapTemplate.put("D02", loadResourceFile("case/D02.json"));
        mapTemplate.put("D04", loadResourceFile("case/D04.json"));
        mapTemplate.put("D05", loadResourceFile("case/D05.json"));
        mapTemplate.put("D06", loadResourceFile("case/D06.json"));
        mapTemplate.put("D07", loadResourceFile("case/D07.json"));
        mapTemplate.put("D08", loadResourceFile("case/D08.json"));
        mapTemplate.put("D10", loadResourceFile("case/D10.json"));
        mapTemplate.put("S06", loadResourceFile("case/S06.json"));
        mapTemplate.put("S04", loadResourceFile("case/S04.json"));
        mapTemplate.put("S02", loadResourceFile("case/S02.json"));
    }

    /**
     * Run test
     *
     * @param cd     function code, null for all, otherwise match the function_cd
     * @param seq    Sequence no
     * @param source test request source: <br/>Schedule task = {@link #SOURCE_SCHEDULE} <br/>ADHOC = {@link #SOURCE_ADHOC}
     */
    public List<String> run(String cd, String seq, short source) {
        loadConfig();
        loadTemplate();
        loadPatient();

        List<String> results = new ArrayList<>();

        ScriptExecutor executor = ScriptExecutor.getInstance();
        if (null == executor) {
            throw new UnsupportedOperationException("Initialize Chrome failed, Check webdriver.chrome.driver configuration");
        }

        try {
            // Login
            String tmplLogin = mapTemplate.get("LGN");
            Map<String, String> placeholder = new HashMap<>();

            List<ScriptStep> loginSteps = ScriptExecutor.buildScript(tmplLogin, placeholder);
            executor.execute(loginSteps, "login", true);
        } finally {
            executor.quit();
        }
        return results;
    }

    /**
     * load pre-selected patient list from database or file
     *
     * @return Patient list
     */
    private List<String> loadPatient() {
        return new ArrayList<>();
    }


}