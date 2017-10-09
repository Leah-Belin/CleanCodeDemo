package com.objectmentor.utilities.args;

//Code available github.com/Leah-Belin/CleanCodeDemo 
//Uncle Bob's code: https://github.com/unclebob/javaargs/commit/53fbebe4a24fb973d546d10be7d465d73c0bf382

import java.util.*;
import java.text.ParseException;

public class Args {
    private String schema;
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private ListIterator<String> currentArgument;
    private List<String> argsList;

    public Args(String schema, String[]args) throws ArgsException{
        this.schema = schema;
        argsList = Arrays.asList(args);
        parse();
    }
    
    public void parse() throws ArgsException{
        parseSchema();
        parseArguments();
    }
    
    private boolean parseSchema() throws ArgsException{
        for(String element: schema.split(",")){
            if(element.length() > 0){
                parseSchemaElement(element.trim());
            }            
        }
        return true;
    }
    
    private void parseSchemaElement(String element) throws ArgsException{
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if(elementTail.length() == 0)
            marshalers.put(elementId, new BooleanArgumentMarshaler());
        else if(elementTail.equals("*"))
            marshalers.put(elementId, new StringArgumentMarshaler());
        else if(elementTail.equals("#"))
            marshalers.put(elementId, new IntegerArgumentMarshaler());
        else if(elementTail.equals("##"))
            marshalers.put(elementId, new DoubleArgumentMarshaler());
        else{
            throw new ArgsException(ArgsException.ErrorCode.INVALID_ARGUMENT_FORMAT, 
                    elementId, elementTail);
        }
    }
    
    private void validateSchemaElementId(char elementId) throws ArgsException{
        if(!Character.isLetter(elementId)){
            throw new ArgsException(
            ArgsException.ErrorCode.INVALID_ARGUMENT_NAME, elementId, null);
        }
    }
        
    private void parseArguments() throws ArgsException{
        for(currentArgument = argsList.listIterator(); currentArgument.hasNext();){
            String arg = currentArgument.next();
            parseArgument(arg);
        }            
    }
    
    private  void parseArgument(String arg) throws ArgsException{
        if(arg.startsWith("-"))
            parseElements(arg);
    }
    
    private void parseElements(String arg) throws ArgsException{
        for(int i = 1; i < arg.length(); i++)
            parseElement(arg.charAt(i));
    }
    
    private void parseElement(char argChar) throws ArgsException{
        if (setArgument(argChar)) 
            argsFound.add(argChar);            
        else{
            throw new ArgsException(ArgsException.ErrorCode.UNEXPECTED_ARGUMENT, argChar, null);
        }                    
    }
    
    private boolean setArgument(char argChar) throws ArgsException{
        ArgumentMarshaler m = marshalers.get(argChar);
        if(m == null)
            return false;
        try{
            m.set(currentArgument);
            return true;
        }catch(ArgsException e){
            e.setErrorArgumentId(argChar);
            throw e;
        }
    }    
  
    public int cardinality(){
        return argsFound.size();
    }
    public String usage(){
        if(schema.length() > 0)
            return "-["+schema+"]"; 
        else
            return "";
    }
    
    public boolean has(char arg){
        return argsFound.contains(arg);
    }  

    public int nextArgument() {
        return currentArgument.nextIndex();
      }

      public boolean getBoolean(char arg) {
        return BooleanArgumentMarshaler.getValue(marshalers.get(arg));
      }

      public String getString(char arg) {
        return StringArgumentMarshaler.getValue(marshalers.get(arg));
      }

      public int getInt(char arg) {
        return IntegerArgumentMarshaler.getValue(marshalers.get(arg));
      }

      public double getDouble(char arg) {
        return DoubleArgumentMarshaler.getValue(marshalers.get(arg));
      }    
}
