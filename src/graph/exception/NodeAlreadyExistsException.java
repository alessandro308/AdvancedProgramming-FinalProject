package graph.exception;

/**
 * Created by alessandro on 07/06/17.
 */
public class NodeAlreadyExistsException extends RuntimeException {
    public NodeAlreadyExistsException(String msg){
        super(msg);
    }
}
