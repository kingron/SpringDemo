package tacos.util;

import mockit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.BeforeMethod;
import tacos.TacoCloudApplication;
import tacos.util.Impl.AbcImpl;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TacoCloudApplication.class)
public class DummyTest {
    @Autowired
    Dummy dummy;

    @Test
    public void testMethod1() throws NoSuchFieldException, IllegalAccessException {
        Abc abc = new Abc() {
            @Override
            public int method1() {
                return 100;
            }

            @Override
            public int method2() {
                return 0;
            }

            @Override
            public int method3() {
                return 0;
            }
        };

        Field field = dummy.getClass().getDeclaredField("abc");
        field.setAccessible(true);
        field.set(dummy, abc);
        assertEquals(dummy.callMe(), 100);
    }

    @Test
    public void testMockInterface() throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        MockUp<Abc> mock = new MockUp<Abc>(){
            @Mock
            public int method1() {
                return 100;
            }
        };

        Field field = dummy.getClass().getDeclaredField("abc");
        field.setAccessible(true);
        field.set(dummy, mock.getMockInstance());
        assertEquals(dummy.callMe(), 100);
    }


    @Injectable
    Abc abc;

    @Test
    public void testExpectation() throws NoSuchFieldException, IllegalAccessException {
        Field field = dummy.getClass().getDeclaredField("abc");
        field.setAccessible(true);
        field.set(dummy, abc);
        new Expectations() {{
            abc.method1();
            result = 100;
        }};
        assertEquals(dummy.callMe(), 100);
    }
}