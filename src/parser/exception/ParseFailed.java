package parser.exception;

/**
 * Exception used in parser class
 */
public class ParseFailed extends RuntimeException{
    public ParseFailed(String message){
        super(message);
    }
}
