package studio.programkode.jatf.java25;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


public abstract class BaseAssignment
{
    @BeforeEach
    public void setUp() {
        Framework.setStandardOutput();
    }

    @AfterEach
    public void tearDown() {
        Framework.resetStandardOutput();
    }
}
