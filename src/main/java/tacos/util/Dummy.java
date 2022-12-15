package tacos.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tacos.util.Impl.AbcImpl;

@Component
public class Dummy {
    @Autowired
    private Abc abc;

    public int callMe() {
        return abc.method1();
    }
}
