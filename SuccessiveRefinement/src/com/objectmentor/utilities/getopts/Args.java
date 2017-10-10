package com.objectmentor.utilities.getopts;

//Code available github.com/Leah-Belin/CleanCodeDemo 

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    private String[] args;
    private boolean valid = true;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private int currentArgument;
    private char errorArgument = '\0';
    
    enum ErrorCode{
        OK, MISSING_STRING, MISSING_INTEGER, INVALID_INTEGER
    }
    
    private ErrorCode errorCode = ErrorCode.OK;
    
    public Args(String schema, String[]args) throws Exception{
        this.schema = schema;
        this.args = args;
        valid = parse();
    }
    
    public boolean parse() throws Exception{
        if(schema.length() == 0 && args.length == 0)
            return true;
        parseSchema();
        parseArguments();
        return valid;
    }
    
    private boolean parseSchema() throws ParseException{
        for(String element: schema.split(",")){
            if(element.length() > 0) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }            
        }
        return true;
    }
    
    private void parseSchemaElement(String element) throws ParseException{
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if(isBooleanSchemaElement(elementTail))
            parseBooleanSchemaElement(elementId);
        if(isStringSchemaElement(elementTail))
            parseStringSchemaElement(elementId); 
        if(isIntegerSchemaElement(elementTail))
            parseIntegerSchemaElement(elementId);
    }
    
    private void validateSchemaElementId(char elementId) throws ParseException{
        if(!Character.isLetter(elementId)){
            throw new ParseException(
            "Bad character:" + elementId + "in Args format:" + schema, 0);
        }
    }
    
    private void parseStringSchemaElement(char elementId){
        marshalers.put(elementId, new StringArgumentMarshaler());
    }
    
    private boolean isStringSchemaElement(String elementTail){
        return elementTail.equals("*");
    }
    
    private boolean isBooleanSchemaElement(String elementTail){
        return elementTail.length() == 0;
    }
    
    private void parseBooleanSchemaElement(char elementId){
        marshalers.put(elementId, new BooleanArgumentMarshaler());        
    }
    
    private boolean isIntegerSchemaElement(String elementTail){
        return elementTail.equals("#");
    }
    
    private void parseIntegerSchemaElement(char elementId){
        marshalers.put(elementId, new IntegerArgumentMarshaler());
    }
    
    private boolean parseArguments()throws Exception{
        for(currentArgument = 0; currentArgument < args.length; currentArgument++){
            String arg = args[currentArgument];
            parseArgument(arg);
        }
        
        return true;
    }
    
    private  void parseArgument(String arg) throws Exception{
        if(arg.startsWith("-"))
            parseElements(arg);
    }
    
    private void parseElements(String arg)throws Exception{
        for(int i = 1; i < arg.length(); i++)
            parseElement(arg.charAt(i));
    }
    
    private void parseElement(char argChar)throws Exception{
        if (setArgument(argChar)) 
            argsFound.add(argChar);           
        else{
            unexpectedArguments.add(argChar);
            valid = false;
        }
    }
    
    private boolean setArgument(char argChar) throws Exception{
        boolean set = true;
        if(isBoolean(argChar))
            setBooleanArg(argChar, true);
        else if(isString(argChar))
            setStringArg(argChar, "");
        else if(isInteger(argChar))
            setIntegerArg(argChar);
        else
            set = false;
        return set;
    }
    
    private void setStringArg(char argChar, String s){
        currentArgument++;
        try{
            marshalers.get(argChar).set(args[currentArgument]);
        }catch(ArrayIndexOutOfBoundsException e){
            valid = false;
            errorArgument = argChar;
            errorCode = ErrorCode.MISSING_STRING;
        }
    }
    
    private boolean isString(char argChar){
        ArgumentMarshaler m = marshalers.get(argChar);
        return m instanceof StringArgumentMarshaler;
    }
    
    private boolean isBoolean(char argChar){
        ArgumentMarshaler m = marshalers.get(argChar);
        return m instanceof BooleanArgumentMarshaler;
    }
    
    private boolean isInteger(char argChar){
      ArgumentMarshaler m = marshalers.get(argChar);
        return m instanceof IntegerArgumentMarshaler;  
    }
    
    private void setBooleanArg(char argChar, boolean value){
        try{
        marshalers.get(argChar).set("true");
        }catch(Exception e){
            
        }
    }
    
    private void setIntegerArg(char argChar) throws Exception{
        currentArgument++;
        String parameter = null;
        try{
            parameter = args[currentArgument];
            marshalers.get(argChar).set(parameter);
        }catch(ArrayIndexOutOfBoundsException e){
            valid = false;
            errorCode = ErrorCode.MISSING_INTEGER;
            throw new Exception();
        }catch(NumberFormatException e){
            valid = false;
            errorCode = ErrorCode.INVALID_INTEGER;
            throw new Exception();
        }
    }
        
    public int cardinality(){
        return argsFound.size();
    }
    public String usage(){
        if(schema.length() > 0)
            return "-["+schema+"]"; else
            return "";
    }
    
    public String errorMesage() throws Exception{
        if(unexpectedArguments.size() > 0) {
            return unexpectedArgumentMessage();
        }else
            switch(errorCode){
                case MISSING_STRING:
                    return String.format("Could not find string parameter for -%c.", errorArgument);
                case OK:
                    throw new Exception("TILT: Should not get here.");
            }
        return"";
    }
    
    private String unexpectedArgumentMessage(){
        StringBuffer message = new StringBuffer("Argument(s)-");
        for(char c:unexpectedArguments){
            message.append(c);
        }
        message.append("unexpected.");
        
        return message.toString();
    }
    
    public boolean getBoolean(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        return am != null && (Boolean)am.get();
    }
    
    public String getString(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        return am == null ? "" : (String)am.get();
    }
    
    public int getInteger(char arg){
        Args.ArgumentMarshaler am = marshalers.get(arg);
        return am == null ? 0 : (int)am.get();
    }
    
    public boolean has(char arg){
        return argsFound.contains(arg);
    }
    
    public boolean isValid(){
        return valid;
    }
    
    private abstract class ArgumentMarshaler{
        public abstract void set(String s);
        public abstract  Object get();
    }
    
    private class BooleanArgumentMarshaler extends ArgumentMarshaler{
        private boolean booleanValue = false;

        public void set(String s){
            booleanValue = true;
        }
        
        public Object get(){
            return booleanValue;
        }
    }
    
    private class StringArgumentMarshaler extends ArgumentMarshaler{
        protected String stringValue = "";
        
        public void set(String s){
            stringValue = s;
        }
        
        public Object get(){
            return stringValue == null ? "" : stringValue;
        }
    }
    
    private class IntegerArgumentMarshaler extends ArgumentMarshaler{
        protected int integerValue = 0;
        
        public void set(String s){
            try{
                integerValue = Integer.parseInt(s);
            }catch(NumberFormatException e){
                
            }
        }
        
        public Object get(){
            return integerValue;
        }
    }
}
