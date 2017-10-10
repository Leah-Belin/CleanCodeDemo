package com.objectmentor.utilities.getopts;

//Code available github.com/Leah-Belin/CleanCodeDemo 

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    private String[] args;
    private boolean valid = true;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> booleanArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> stringArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> integerArgs = new HashMap<Character, ArgumentMarshaler>();
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
        stringArgs.put(elementId, new StringArgumentMarshaler());
    }
    
    private boolean isStringSchemaElement(String elementTail){
        return elementTail.equals("*");
    }
    
    private boolean isBooleanSchemaElement(String elementTail){
        return elementTail.length() == 0;
    }
    
    private void parseBooleanSchemaElement(char elementId){
        booleanArgs.put(elementId, new BooleanArgumentMarshaler());        
    }
    
    private boolean isIntegerSchemaElement(String elementTail){
        return elementTail.equals("#");
    }
    
    private void parseIntegerSchemaElement(char elementId){
        integerArgs.put(elementId, new IntegerArgumentMarshaler());
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
            stringArgs.get(argChar).setString(args[currentArgument]);
        }catch(ArrayIndexOutOfBoundsException e){
            valid = false;
            errorArgument = argChar;
            errorCode = ErrorCode.MISSING_STRING;
        }
    }
    
    private boolean isString(char argChar){
        return stringArgs.containsKey(argChar);
    }
    
    private boolean isBoolean(char argChar){
        return booleanArgs.containsKey(argChar);
    }
    
    private boolean isInteger(char argChar){
      return integerArgs.containsKey(argChar);  
    }
    
    private void setBooleanArg(char argChar, boolean value){
        booleanArgs.get(argChar).set("true");
    }
    
    private void setIntegerArg(char argChar) throws Exception{
        currentArgument++;
        String parameter = null;
        try{
            parameter = args[currentArgument];
            integerArgs.get(argChar).setInteger(Integer.parseInt(parameter));
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
        Args.ArgumentMarshaler am = booleanArgs.get(arg);
        return am != null && am.getBoolean();
    }
    
    public String getString(char arg){
        Args.ArgumentMarshaler am = stringArgs.get(arg);
        return am.getString();
    }
    
    public int getInteger(char arg){
        Args.ArgumentMarshaler am = integerArgs.get(arg);
        return am == null ? 0 : am.getInteger();
    }
    
    public boolean has(char arg){
        return argsFound.contains(arg);
    }
    
    public boolean isValid(){
        return valid;
    }
    
    private abstract class ArgumentMarshaler{
        protected boolean booleanValue = false;
        private String stringValue = "";
        private int integerValue = 0;
                
        public boolean getBoolean(){
            return booleanValue;
        }
        
        public void setString(String s){
            stringValue = s;
        }
        
        public String getString(){
            return stringValue == null ? "" : stringValue;
        }
        
        public void setInteger(int i){
            integerValue = i;
        }
        
        public int getInteger(){
            return integerValue;
        }
        
        public abstract void set(String s);
    }
    
    private class BooleanArgumentMarshaler extends ArgumentMarshaler{
        public void set(String s){
            booleanValue = true;
        }
    }
    
    private class StringArgumentMarshaler extends ArgumentMarshaler{
        public void set(String s){
            
        }
    }
    
    private class IntegerArgumentMarshaler extends ArgumentMarshaler{
        public void set(String s){
            
        }
    }
}
