package compiler.exception;

/**
 * Exception used in compiler class
 */
public class ParseFailed extends RuntimeException{
    public ParseFailed(String message){
        super(message);
    }
}
