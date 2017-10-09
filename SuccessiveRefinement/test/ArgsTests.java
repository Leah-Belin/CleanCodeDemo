import com.objectmentor.utilities.getopts.Args;
import java.text.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArgsTests {
        
    public ArgsTests() {
    }

    @Test
    public void Parse_returns_true_if_schema_and_args_areEmpty() throws Exception {
        Args instance = new Args("", new String[0]);
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }
    
    @Test
    public void parseSchema_returns_false_with_one_argument_but_no_schema() throws Exception {
        Args instance = new Args("", new String[]{"-x"});
        boolean expected = false;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }
    
    @Test
    public void parseArguments_returns_true_with_schema_and_no_args() throws Exception {
        Args instance = new Args("x", new String[]{});
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }  
    
    @Test
    public void parseSchema_returns_true_if_schema_has_whiteSpace() throws Exception {
        Args instance = new Args(" x ", new String[]{"Y"});
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }
    
    @Test
    public void parseSchema_returns_true_if_schema_has_comma() throws Exception {
        Args instance = new Args("x,", new String[]{"Y"});
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }
    
    @Test
    public void parseSchema_returns_true_if_schema_has_multiple_comma_separated_elements() throws Exception {
        Args instance = new Args("x, x", new String[]{"Y"});
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }  

    @Test //boolean is defined as tail length 0
    public void parseSchema_returns_true_if_schema_is_boolean() throws Exception {
        Args instance = new Args("x", new String[]{"Y"});
        boolean expected = true;
        boolean actual = instance.parse();
        assertEquals(expected, actual);
    }       
}
