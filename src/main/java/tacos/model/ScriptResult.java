package tacos.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 脚本运行结果
 *
 * @author Richard Lu
 */
@Data
public class ScriptResult {
    /**
     * 脚本开始运行时间，从第一个Step开始计时
     */
    public Date start;

    /**
     * 脚本运行结束时间，即最后一个step运行完成的时刻
     */
    public Date end;

    /**
     * 執行結果
     */
    private List<StepResult> stepResultList;

    /**
     * measure_time_1 数据采样时间
     */
    public Date sampleTime_1;

    /**
     * measure_time_2 数据生成时间
     */
    public Date sampleTime_2;

    /**
     * measure_time_1 数据采样时间
     */
    public Date sampleTime_3;

    /**
     * Measurement time 1，单位 s
     */
    public Float measure_time_1;

    /**
     * Measurement time 2，可能为空，单位 s
     */
    public Float measure_time_2;

    /**
     * Measurement time 3，可能为空，单位 s
     */
    public Float measure_time_3;

    /**
     * 错误信息，如果执行出问题，可以查看错误信息
     */
    public String errorMessage = "";
}