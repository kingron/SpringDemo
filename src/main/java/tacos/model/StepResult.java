package tacos.model;

import lombok.Data;

import java.time.Instant;

@Data
public class StepResult {

    private ScriptStep step;
    private Instant start;
    private Instant end;
    private int status;
}
