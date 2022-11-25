package tacos.util;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.FileCopyUtils;
import tacos.model.ScriptResult;
import tacos.model.ScriptStep;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tacos.model.ScriptStep.*;
import static tacos.util.Util.*;

/**
 * 脚本执行器 - Script Executor
 * <p>
 * 运行给定的脚本序列并返回测量指标，使用示例 <<br/>
 * Run the given script and return the measurement metrics, usage:
 * <pre>{@code
 * Gson gson = new Gson();
 * Type listType = new TypeToken<ArrayList<ScriptStep>>() {
 * }.getType();
 *
 * List<ScriptStep> steps = gson.fromJson(json, listType);
 * ScriptExecutor executor = ScriptExecutor.getInstance();
 * ScriptResult result = executor.execute(steps);
 * }</pre>
 */

@Slf4j
public class ScriptExecutor {
    private static final String ERROR_FIND_ELEMENT = "can not find the element, check selectorType & selectorValue, or update script when page changed";
    private static final String ERROR_SELECTOR_TYPE = "Wrong selectorType, should be one of [id, class, name, xpath]";
    private static final String ERROR_MISS_START_MARKER = "The end marker before start marker";
    private static final String ERROR_INVALID_ACTION = "Invalid action, only [open, select, check, fill, click, none] allowed";
    private static final String CONFIG_DRIVER = "webdriver.chrome.driver";

    private boolean capture = false;
    private String dataFolder;
    private final ChromeDriver driver;

    private ScriptResult result = new ScriptResult();

    /**
     * 脚本循环索引变量 <br/>
     * Loop variable, used for GOTO command to change the step
     */
    private int index = 0;

    /**
     * 最新的 start marker 时间点 <br/>
     * The last start marker time
     */
    private Date lastStart;

    /**
     * 获取实例，获取实例后，不再需要时，需要调用 {@link #quit()} 关闭Chrome和ChromeDriver <br/>
     * return ScriptExecutor instance, when done, MUST call {@link #quit()}
     *
     * @return 返回 ScriptExecutor 实例，失败返回 null <br/>Script Executor instance, Null if error
     * @see #quit()
     */
    public static ScriptExecutor getInstance() {
        try {
            return new ScriptExecutor();
        } catch (Exception e) {
            log.error("launch Chrome error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Better use getInstance()
     */
    public ScriptExecutor() {
        String driverPath;
        URL url = ScriptResult.class.getResource("");
        String protocol = url.getProtocol();
        if ("jar".equals(protocol)) {
            driverPath = getAppPath() + "chromedriver.exe";
        } else {
            driverPath = getProperty(CONFIG_DRIVER, "src/main/resources/chromedriver.exe");
        }
        log.warn("Chrome驱动地址: " + driverPath);
        System.setProperty(CONFIG_DRIVER, driverPath);
        ChromeOptions chromeOptions = new ChromeOptions();

        driver = new ChromeDriver(chromeOptions);
        // driver.manage().timeouts().implicitlyWait(500, TimeUnit.MICROSECONDS);
        driver.manage().window().maximize();
    }

    /**
     * 关闭Chrome，释放资源 <br/>
     * Close chrome, release resource
     */
    public void quit() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            log.warn("Close Chrome error: " + getDriverError(e));
        }
    }

    /**
     * 执行 Steps 并返回本次脚本运行结果 <br/>
     * Execute the script and return the result
     *
     * @param steps 测试脚本步骤清单 <br/> Steps of the script
     * @return 测试结果对象 <br/> Return the test result
     */
    public ScriptResult execute(List<ScriptStep> steps, String tag, boolean capture) {
        if (steps == null) {
            return null;
        }

        this.capture = capture;
        lastStart = null;
        result = new ScriptResult();
        result.start = new Date();
        networkThrottling();
        dataFolder = ProjectUtil.getConfig("data_dir")
                + now(FORMAT_DATE) + File.separator
                + getValidFilename(tag) + File.separator
                + now(FORMAT_SHORT_TIME) + File.separator;
        if (capture) {
            //noinspection ResultOfMethodCallIgnored
            new File(dataFolder).mkdirs();
        }

        for (index = 0; index < steps.size(); index++) {
            if (executeStep(steps.get(index))) {
                break;
            }
        }

        result.end = new Date();
        return result;
    }

    /**
     * 执行测试步骤 <br/>Execute a step of the script
     *
     * @param step 步骤对象 <br/>Step
     * @return 若停止测试，返回 true，否则返回 false <br/>return true if STOP the script, otherwise return false
     */
    private boolean executeStep(ScriptStep step) {
        log.debug("开始步骤: " + step);

        if (MARKER_START.equals(step.marker)) {
            lastStart = new Date();
        }

        boolean stop = false;
        switch (step.action) {
            case ACTION_OPEN:
                stop = doOpen(step);
                break;
            case ACTION_CHECK:
                stop = doCheck(step);
                break;
            case ACTION_CLICK:
                stop = doClick(step);
                break;
            case ACTION_FILL:
                stop = doFill(step);
                break;
            case ACTION_SELECT:
                stop = doSelect(step);
                break;
            case ACTION_WAIT:
                stop = doWait(step);
                break;
            case ACTION_ANY_WAIT:
                stop = doAnyWait(step);
                break;
            case ACTION_GOTO:
                stop = doGoto(step);
                break;
            case ACTION_SCRIPT:
                stop = doScript(step);
                break;
            case ACTION_SLEEP:
                stop = doSleep(step);
                break;
            case ACTION_NONE:
                break;
            default:
                stop = step.stopOnError;
                setError(step, ERROR_INVALID_ACTION);
                break;
        }

        if (MARKER_END.equals(step.marker) && !stop) {
            setMeasureTime(step);
        }
        if (capture) {
            screenCapture(step);
        }
        log.debug("结束步骤: " + step.name);
        return stop;
    }

    /**
     * 执行浏览器脚本，脚本内容为 actionValue，脚本的返回值为下一个step的索引
     *
     * @param step 执行步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    private boolean doScript(ScriptStep step) {
        try {
            Object ret = driver.executeScript(step.actionValue);
            if (ret != null) {
                setError(step, toStr(ret));
            }
            return false;
        } catch (Exception e) {
            log.error("脚本错误: " + e.getMessage());
            setError(step, getDriverError(e));
            return step.stopOnError;
        }
    }

    /**
     * 等待一定时间
     *
     * @param step 执行步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doSleep(ScriptStep step) {
        try {
            Thread.sleep(strToInt(step.actionValue, 0));
            return false;
        } catch (InterruptedException e) {
            log.error("Sleep错误: " + e.getMessage());
            setError(step, e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 若给定的条件为true，跳转到特定步骤
     *
     * @param step 执行步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doGoto(ScriptStep step) {
        try {
            boolean scriptResult = (boolean) driver.executeScript(step.actionValue);
            if (scriptResult) {
                index = Math.max(-1, step.nextStep - 1);
            }
            return false;
        } catch (Exception e) {
            setError(step, "goto error: " + getDriverError(e));
            log.error("doGoto 出错: " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 支持并发 Check，selectorValue可以是逗号分隔的多个Selector值
     *
     * @param step 执行步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doWait(ScriptStep step) {
        String[] selectors = step.selectorValue.split(",");
        if (selectors.length <= 1) return doCheck(step);

        CountDownLatch latch = new CountDownLatch(selectors.length);
        ExecutorService executor = Executors.newFixedThreadPool(selectors.length);
        long[] durations = new long[selectors.length];
        WebElement[] elements = new WebElement[selectors.length];

        for (int i = 0; i < selectors.length; i++) {
            final int index = i;
            executor.submit(() -> {
                long start = System.currentTimeMillis();
                elements[index] = waitElement(step.selectorType, selectors[index], step.timeout);
                durations[index] = System.currentTimeMillis() - start;
                latch.countDown();
            });
        }
        try {
            latch.await();
            String s = "Duration: " + join(durations, ",");
            setError(step, s);
            log.debug("并发查找 " + s);

            // 如果查找元素列表有 null ，说明某个步骤有错误
            if (inList(elements, null)) {
                return step.stopOnError;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("并发查找错误: " + e.getMessage());
            setError(step, e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 支持并发 Check，selectorValue可以是逗号分隔的多个Selector值
     *
     * @param step 执行步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doAnyWait(ScriptStep step) {
        String[] selectors = step.selectorValue.split(",");
        if (selectors.length <= 1) return doCheck(step);
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(selectors.length);
        long[] durations = new long[selectors.length];
        WebElement[] elements = new WebElement[selectors.length];

        for (int i = 0; i < selectors.length; i++) {
            final int index = i;
            executor.submit(() -> {
                long start = System.currentTimeMillis();
                elements[index] = waitElement(step.selectorType, selectors[index], step.timeout);
                durations[index] = System.currentTimeMillis() - start;
                latch.countDown();
            });
        }
        try {
            latch.await();
            executor.shutdown();
            String s = "Duration: " + join(durations, ",");
            setError(step, s);
            log.debug("并发查找 " + s);

            // Any情况下，必须所有的都为null出错，才应该算错误，否则只要有一个不为 null，就应该算正确
            boolean allNull = true;
            for (WebElement e : elements) {
                if (e != null) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                return step.stopOnError;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("任意查找报错: " + e.getMessage());
            setError(step, e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 抓取屏幕快照
     *
     * @param step 操作步骤
     */
    private void screenCapture(ScriptStep step) {
        // 步骤截图开关处理
        if (!step.capture) {
            return;
        }

        try {
            String filename = dataFolder + getValidFilename(step.name) + "_" + now(FORMAT_SHORT_TIME) + ".png";
            File srcFile = driver.getScreenshotAs(OutputType.FILE);
            File dstFile = new File(filename);
            FileCopyUtils.copy(srcFile, dstFile);
            log.debug("截屏成功 " + filename);
            Files.delete(srcFile.toPath());
        } catch (Exception e) {
            setError(step, "capture failure " + getDriverError(e));
            log.warn("截屏失败 " + e.getMessage());
        }
    }

    /**
     * 设置测量结果，测量结果仅在 marker == "end" 时计算
     *
     * @param step 脚本对象
     */
    private void setMeasureTime(ScriptStep step) {
        if (lastStart == null) {
            setError(step, ERROR_MISS_START_MARKER);
            log.info("找不到开始时刻，脚本配置有误: " + step);
            return;
        }

        long duration = diffTime(lastStart, now());
        if (step.measure == MEASURE_TIME_1) {
            result.measure_time_1 = duration / 1000f;
            result.sampleTime_1 = now();
        } else if (step.measure == MEASURE_TIME_2) {
            result.measure_time_2 = duration / 1000f;
            result.sampleTime_2 = now();
        } else if (step.measure == MEASURE_TIME_3) {
            result.measure_time_3 = duration / 1000f;
            result.sampleTime_3 = now();
        } else {
            setError(step, "invalid measure value, should be one 1 or 2 or 3");
            log.error("配置有误: " + step);
        }
        lastStart = null;
    }

    /**
     * 执行 fill 动作
     *
     * @param step 脚本对象
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doFill(ScriptStep step) {
        try {
            WebElement element = waitElement(step);
            if (element == null) {
                setError(step, ERROR_FIND_ELEMENT);
                return step.stopOnError;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", element);
            element.sendKeys(step.actionValue);
            return false;
        } catch (Exception e) {
            setError(step, "fill error: " + getDriverError(e));
            log.error("doFill 出错: " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 查找脚本对应的元素
     *
     * @param step 脚本对象
     * @return 找到的页面元素，若异常或失败，返回 null
     */
    protected WebElement findElement(ScriptStep step) {
        switch (step.selectorType) {
            case SELECTOR_ID:
                return driver.findElement(By.id(step.selectorValue));
            case SELECTOR_CLASS:
                return driver.findElement(new By.ByClassName(step.selectorValue));
            case SELECTOR_NAME:
                return driver.findElement(new By.ByTagName(step.selectorValue));
            case SELECTOR_XPATH:
                return driver.findElement(By.xpath(step.selectorValue));
            default:
                setError(step, "selectorType only be one of [id, class, xpath, name]");
                log.error("selectType非法: " + step.selectorType);
                return null;
        }
    }

    /**
     * 模拟点击动作
     *
     * @param step 脚本对象
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doClick(ScriptStep step) {
        try {
            WebElement element = waitElement(step);
            if (element == null) {
                setError(step, ERROR_FIND_ELEMENT);
                return step.stopOnError;
            }

            element.click();
            return false;
        } catch (Exception e) {
            setError(step, "click error: " + getDriverError(e));
            log.error("doClick 出错: " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 提取浏览器返回的错误信息，去掉无用的内容
     *
     * @param e 异常错误
     * @return 干净的错误信息
     */
    private String getDriverError(Exception e) {
        return left(e.getMessage(), "(Session info:").replace("javascript error: ", "").trim();
    }


    /**
     * 等待页面元素
     *
     * @param step 脚本对象
     * @return 返回等待并查找到的页面元素，若错误或异常，返回 null
     */
    protected WebElement waitElement(ScriptStep step) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, step.timeout);
            wait.pollingEvery(Duration.ofMillis(20));
            switch (step.selectorType) {
                case SELECTOR_ID:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ById(step.selectorValue)));
                case SELECTOR_CLASS:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByClassName(step.selectorValue)));
                case SELECTOR_NAME:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByTagName(step.selectorValue)));
                case SELECTOR_XPATH:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByXPath(step.selectorValue)));
                default:
                    setError(step, ERROR_SELECTOR_TYPE);
                    return null;
            }
        } catch (TimeoutException e) {
            setError(step, "wait element timeout");
            log.warn("查找元素超时: " + e.getMessage());
            return null;
        }
    }

    /**
     * 查找指定元素
     *
     * @param selectorType  选择器类型
     * @param selectorValue 选择器值
     * @param timeout       超时设置，单位 s
     * @return 找到返回 元素，否则返回 null
     */
    protected WebElement waitElement(String selectorType, String selectorValue, long timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            wait.pollingEvery(Duration.ofMillis(20));
            switch (selectorType) {
                case SELECTOR_ID:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ById(selectorValue)));
                case SELECTOR_CLASS:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByClassName(selectorValue)));
                case SELECTOR_NAME:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByTagName(selectorValue)));
                case SELECTOR_XPATH:
                    return wait.until(ExpectedConditions.presenceOfElementLocated(new By.ByXPath(selectorValue)));
                default:
                    return null;
            }
        } catch (TimeoutException e) {
            log.warn("查找元素超时: " + e.getMessage());
            return null;
        }
    }

    /**
     * 执行 check 动作
     *
     * @param step 脚本对象
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doCheck(ScriptStep step) {
        try {
            WebElement element = waitElement(step);
            if (element == null) {
                setError(step, ERROR_FIND_ELEMENT);
                return step.stopOnError;
            }

            String value = toStr(element.getText());
            // 如果 actionValue 为空，表示只要判断元素存在，其中有值即可，否则元素值必须和 actionValue 匹配
            // 支持正则表达式
            if ((isEmpty(step.actionValue) && !isEmpty(value))
                    || equal(value, step.actionValue)
                    || match(step.actionValue, value)) {
                return false;
            }

            setError(step, "check failed, timeout or selector wrong, got: " + value + ", expect: " + step.actionValue);
            return step.stopOnError;
        } catch (Exception e) {
            setError(step, "check error: " + getDriverError(e));
            log.error("doCheck 出错: " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 选择 ul/li
     *
     * @param step 脚本步骤
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doSelect(ScriptStep step) {
        try {
            WebElement element = waitElement(step);
            if (element == null) {
                setError(step, ERROR_FIND_ELEMENT);
                return step.stopOnError;
            }

            List<WebElement> list = element.findElements(By.tagName("li"));
            for (WebElement item : list) {
                if (equal(item.getText(), step.actionValue)) {
                    item.click();
                    return false;
                }
            }
            return step.stopOnError;
        } catch (Exception e) {
            setError(step, "select error: " + getDriverError(e));
            log.warn("doSelect 出错 " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 执行 open 动作
     *
     * @param step 脚本对象
     * @return 是否停止测试, true = 停止, false = 继续
     */
    protected boolean doOpen(ScriptStep step) {
        try {
            driver.get(step.actionValue);
            return false;
        } catch (Exception e) {
            setError(step, "open error: " + getDriverError(e));
            log.error("doOpen 出错 " + e.getMessage());
            return step.stopOnError;
        }
    }

    /**
     * 设置错误信息
     *
     * @param step 执行的步骤
     * @param s    错误内容
     */
    private void setError(ScriptStep step, String s) {
        String buf = now(FORMAT_DATETIME) + ": Step [" + step.name + "] " + s;
        if (isEmpty(result.errorMessage)) {
            result.errorMessage = buf;
        } else {
            result.errorMessage += System.lineSeparator() + buf;
        }
    }

    /**
     * 根据模板和参数列表，生成测试Steps，以便后面执行
     *
     * @param jsonTemplate JSON模板字符串
     * @param params       占位符数据HashMap key = 占位符, value = 占位符对应的值
     * @return 返回构建好的Steps列表
     */
    public static List<ScriptStep> buildScript(String jsonTemplate, Map<String, String> params) {
        if (isEmpty(jsonTemplate)) {
            return new ArrayList<>();
        }

        final String[] target = {jsonTemplate};
        params.forEach((key, value) -> target[0] = target[0].replaceAll(key, value));
        return ScriptStep.parse(target[0]);
    }

    /**
     * 模拟网络限速
     */
    protected void networkThrottling() {
        if (!strToBoolean(ProjectUtil.getConfig("network.throttling"))) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("offline", false);
        map.put("latency", ProjectUtil.getConfigInt("network.latency", 500));
        map.put("download_throughput", ProjectUtil.getConfigInt("network.download", 1000 * 1000));
        map.put("upload_throughput", ProjectUtil.getConfigInt("network.upload", 500 * 1000));
        CommandExecutor executor = driver.getCommandExecutor();
        try {
            executor.execute(
                    new Command(driver.getSessionId(), "setNetworkConditions", ImmutableMap.of("network_conditions", ImmutableMap.copyOf(map)))
            );
        } catch (Exception e) {
            log.warn("限速失败 " + e.getMessage());
        }
    }
}