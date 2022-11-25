package tacos.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 脚本步骤数据结构
 *
 * @author Richard Lu
 */
@Slf4j
public class ScriptStep {
    public static final String MARKER_START = "start";
    public static final String MARKER_END = "end";
    public static final String MARKER_NONE = "none";
    public static final String ACTION_NONE = "none";
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_FILL = "fill";
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_CHECK = "check";
    public static final String ACTION_SELECT = "select";
    /**
     * 在浏览器中执行actionValue中给出的脚本
     */
    public static final String ACTION_SCRIPT = "script";
    /**
     * 同时等待多个元素出现，selectValue可以为逗号分隔的多个selector值
     */
    public static final String ACTION_WAIT = "wait";
    public static final String ACTION_GOTO = "goto";
    /**
     * 睡眠指定时间，单位: ms
     */
    public static final String ACTION_SLEEP = "sleep";
    public static final String ACTION_ANY_WAIT = "any";

    public static final String SELECTOR_ID = "id";
    public static final String SELECTOR_CLASS = "class";
    public static final String SELECTOR_NAME = "name";
    public static final String SELECTOR_XPATH = "xpath";

    public static final int MEASURE_TIME_1 = 1;
    public static final int MEASURE_TIME_2 = 2;
    public static final int MEASURE_TIME_3 = 3;

    /**
     * 步骤名称，仅用于存储，方便用户记忆，内部不适用
     */
    public String name = "No name";

    /**
     * 本步骤对应的选择器类型，可以为以下之一，可以使用SELECTOR_前缀常量赋值
     */
    public String selectorType = SELECTOR_ID;

    /**
     * 本步骤对应的选择器的值
     */
    public String selectorValue = "";

    /**
     * 本步骤对应的 marker 类型，值为以下一种，请使用MARKER_前缀常量赋值
     * <ul>
     *     <li>start: 标记计时开始</li>
     *     <li>end: 标记计时结束，此时开始计算 measurement time</li>
     *     <li>none: 无计时</li>
     * </ul>
     */
    public String marker = MARKER_NONE;

    /**
     * 本步骤执行的操作，可以为以下一种，请使用ACTION_前缀常量赋值:
     * <ul>
     *     <li>none: 无操作</li>
     *     <li>open: 打开 actionValue 对应的 url</li>
     *     <li>fill: 对selector元素填充文本</li>
     *     <li>click: 模拟点击selctor元素</li>
     *     <li>check: 核对页面元素值（value或者InnerText）是否与actionValue相同</li>
     * </ul>
     */
    public String action = ACTION_CLICK;

    /**
     * action对应的参数值
     */
    public String actionValue = "";

    /**
     * Mark为end时计算的时间差赋值到那个结果，可以为 1， 2， 3 <ul>
     * <li>1: 赋值给 measure_time_1</li>
     * <li>2: 赋值给 measure_time_2</li>
     * <li>3: 赋值给 measure_time_3</li>
     * </ul>
     */
    public int measure = 1;

    /**
     * 出错或者失败时是否停止步骤并返回
     */
    public boolean stopOnError = false;

    /**
     * 本步骤大的超时设置，单位: 秒，默认 10 秒
     */
    public int timeout = 10;

    /**
     * 是否截图
     */
    public boolean capture = false;

    /**
     * 下一步执行动作步骤，仅用于 goto 条件为 true 时执行
     */
    public int nextStep = 0;

    @Override
    public String toString() {
        return "测试步骤(" + name + ") => " + new Gson().toJson(this);
    }

    /**
     * 脚本列表转字符串输出
     *
     * @param steps  脚本列表
     * @param pretty 是否格式化输出
     * @return JSON字符串
     */
    public static String toJSON(List<ScriptStep> steps, boolean pretty) {
        GsonBuilder builder = new GsonBuilder();
        if (pretty) {
            builder.setPrettyPrinting();
        }

        Gson gson = builder.create();
        return gson.toJson(steps);
    }

    /**
     * JSON 字符串转 List
     *
     * @param json JSON字符串
     * @return 脚本列表对象
     */
    public static List<ScriptStep> parse(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        Type listType = new TypeToken<ArrayList<ScriptStep>>() {
        }.getType();

        try {
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            log.error("JSON parse error: " + e.getMessage());
            return new ArrayList<>();
        }

    }
}