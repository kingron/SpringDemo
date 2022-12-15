package tacos.util.Impl;

import org.springframework.stereotype.Component;
import tacos.util.Abc;

@Component
public class AbcImpl implements Abc {
    @Override
    public int method1() {
        return 50;
    }

    @Override
    public int method2() {
        return 0;
    }

    @Override
    public int method3() {
        return 0;
    }
}
