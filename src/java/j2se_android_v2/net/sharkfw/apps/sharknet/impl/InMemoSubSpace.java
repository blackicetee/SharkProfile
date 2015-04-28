package net.sharkfw.apps.sharknet.impl;

import net.sharkfw.subspace.AbstractSubSpace;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpaceGuardKP;

/**
 * @author thsc
 */
public class InMemoSubSpace extends AbstractSubSpace {
    
    public InMemoSubSpace(SubSpaceGuardKP guardKP) throws SharkSubSpaceException {
        super(guardKP);
    }
}
