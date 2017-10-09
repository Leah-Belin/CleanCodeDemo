import com.objectmentor.utilities.getopts.Args;
import java.text.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArgsTests {
      
    @Test
    public void Parse_returns_true_if_schema_and_args_areEmpty() throws Exception {
        Args instance = new Args("", new String[0]);
        assertEquals(true, instance.parse());
    }
    
    @Test
    public void parseSchema_returns_false_with_one_argument_but_no_schema() throws Exception {
        Args instance = new Args("", new String[]{"-x"});
        assertEquals(false, instance.parse());
    }
    
    @Test
    public void parseArguments_returns_true_with_schema_and_no_args() throws Exception {
        Args instance = new Args("x", new String[]{});
        assertEquals(true, instance.parse());
    }  
    
    @Test
    public void parseSchema_returns_true_if_schema_has_whiteSpace() throws Exception {
        Args instance = new Args(" x ", new String[]{"Y"});
        assertEquals(true, instance.parse());
    }
    
    @Test
    public void parseSchema_returns_true_if_schema_has_comma() throws Exception {
        Args instance = new Args("x,", new String[]{"Y"});
        assertEquals(true, instance.parse());
    }
    
    @Test
    public void parseSchema_returns_true_if_schema_has_multiple_comma_separated_elements() throws Exception {
        Args instance = new Args("x, x", new String[]{"Y"});
        assertEquals(true, instance.parse());
    }  

    @Test 
    public void parseSchema_returns_true_if_simple_boolean() throws Exception {
        Args instance = new Args("x", new String[]{"-x"});
        assertEquals(true, instance.parse());
    }      

    @Test(expected = ParseException.class)
    public void parseSchema_throws_exception_if_NonLetterSchema() throws Exception {
          new Args("*", new String[]{});
    }
    
    @Test
    public void parseSchema_returns_true_if_simple_string() throws Exception {
      Args instance = new Args("x*", new String[]{"-x", "param"});
      assertEquals(true, instance.parse());
    }
    
    @Test
    public void parseSchema_returns_false_if_missing_string_argument() throws Exception {
        Args instance = new Args("x*", new String[]{"-x"});
        assertEquals(false, instance.parse());
    }
    
    @Test 
    public void getBoolean_returns_boolean() throws Exception {
        Args instance = new Args("x", new String[]{"-x"});
        assertEquals(true, instance.getBoolean('x'));
    }  
    
    @Test 
    public void getBoolean_returns_false_if_null() throws Exception {
        Args instance = new Args("x", new String[]{""});
        assertEquals(false, instance.getBoolean('x'));
    } 
    
    @Test 
        public void getBoolean_returns_false_if_invalid_arg() throws Exception{
        Args instance = new Args("x", new String[]{"-x"});
        assertEquals(false, instance.getBoolean('y'));
    }
    
    @Test 
    public void getString_returns_string() throws Exception {
        Args instance = new Args("x*", new String[]{"-x", "param"});
        assertEquals("param", instance.getString('x'));
    }  
    
    @Test 
    public void getString_returns_blank_if_null() throws Exception {
        Args instance = new Args("x*", new String[]{"-x"});
        assertEquals("", instance.getString('x'));
    } 
    
    @Test 
    public void getInteger_returns_integer() throws Exception {
        Args instance = new Args("x#", new String[]{"-x", "42"});
        assertEquals(42, instance.getInteger('x'));
    }
    
    @Test(expected = Exception.class)
    public void getInteger_throws_exception_if_null() throws Exception {
        new Args("x#", new String[]{"-x"});
    } 
    
    @Test
    public void has_returns_simple_boolean()throws Exception{
        Args instance = new Args("x", new String[]{"-x"});
        assertTrue(instance.has('x'));
    }
    
    @Test
    public void has_returns_simple_string() throws Exception{
        Args instance = new Args("x*", new String[]{"-x", "param"});
        assertTrue(instance.has('x'));
    }
    
    @Test
    public void has_returns_simple_integer() throws Exception{
        Args instance = new Args("x#", new String[]{"-x", "42"});
        assertTrue(instance.has('x'));
    }  
  
}
