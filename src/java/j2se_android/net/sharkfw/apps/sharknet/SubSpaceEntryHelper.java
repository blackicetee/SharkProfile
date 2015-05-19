package net.sharkfw.apps.sharknet;

import java.util.Enumeration;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceEntry;

/**
 *
 * @author Florian Lehne
 * @param <T>
 */
public interface SubSpaceEntryHelper<T extends SubSpaceEntry> extends SubSpace {

    /**
     * Return a list of entries ordered by creation. Latest entry comes first.
     *
     * @return
     * @throws SharkKBException
     */
    public Enumeration<T> entries() throws SharkKBException;

    public T getEntry(int index) throws SharkKBException;

    public void removeEntry(T entry) throws SharkKBException;
}
