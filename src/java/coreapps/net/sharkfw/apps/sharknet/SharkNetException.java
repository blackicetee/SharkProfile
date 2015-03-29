package net.sharkfw.apps.sharknet;


import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.system.SharkException;

/**
 *
 * @author thsc
 */
public class SharkNetException extends SharkException {
    public SharkNetException() {
        super();
    }
    
    public SharkNetException(String message) {
        super(message);
    }

    public SharkNetException(SharkSubSpaceException ex) {
        this(ex.getLocalizedMessage());
    }
}
