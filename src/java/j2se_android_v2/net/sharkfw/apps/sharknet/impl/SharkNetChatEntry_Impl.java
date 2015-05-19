package net.sharkfw.apps.sharknet.impl;

import net.sharkfw.apps.sharknet.SharkNetChatEntry;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.subspace.InMemoSubSpaceEntry;
import net.sharkfw.system.L;
import net.sharkfw.system.Util;

/**
 *
 * @author thsc
 */
public class SharkNetChatEntry_Impl extends InMemoSubSpaceEntry implements SharkNetChatEntry {

    protected String message;
    protected Information info;
    private SemanticTag topic;
    private SharkKB baseKB;

    public SharkNetChatEntry_Impl(ContextPoint storedCP, SharkKB baseKB) {
        super(storedCP);
        this.baseKB = baseKB;
    }

    public static final String CHAT_MESSAGE_ENTRY_TYPE = "text/plain";

    public SharkNetChatEntry_Impl(ContextPoint cp, String message) {
        super(cp);

        // store message
        Information i = this.cp.addInformation(message);
        i.setContentType(CHAT_MESSAGE_ENTRY_TYPE);

        this.message = message;
        this.info = i;
    }

    public SharkNetChatEntry_Impl(ContextPoint cp, ContextPoint sourceCP, Information info) throws SharkKBException {
        super(cp);

        Information newInfo = cp.addInformation();
        newInfo.streamContent(info.getOutputStream());

        newInfo.setContentType(info.getContentType());

        this.info = info;
    }

    public static final String CHAT_SI_ENTRY_TYPE = "x-sharkfw/subject-identifiers-string";

    public SharkNetChatEntry_Impl(ContextPoint cp, SemanticTag topic) {
        super(cp);
        Information newInfo = cp.addInformation(Util.array2string(topic.getSI()));

        newInfo.setContentType(CHAT_SI_ENTRY_TYPE);

        this.topic = topic;
    }

    @Override
    public boolean hasInformation() throws SharkKBException {
        this.restore();
        return this.info != null;
    }

    @Override
    public boolean hasTextMessage() throws SharkKBException {
        this.restore();
        return this.message != null;
    }

    @Override
    public boolean hasSemanticTag() throws SharkKBException {
        this.restore();
        return this.topic != null;
    }

    @Override
    public SemanticTag getSemanticTag() throws SharkKBException {
        this.restore();
        if (this.topic == null) {
            throw new SharkKBException("no semantic tag in there");
        }

        return this.topic;
    }

    @Override
    public Information getInformation() throws SharkKBException {
        this.restore();
        if (this.info == null) {
            throw new SharkKBException("no information in there");
        }

        return this.info;
    }

    @Override
    public String getTextMessage() throws SharkKBException {
        this.restore();
        if (this.message == null) {
            throw new SharkKBException("no message in there");
        }

        // return this.info;
        return this.message;
    }

    @Override
    public long getTime() {
        return this.cp.getContextCoordinates().getTime().getFrom();
    }

    protected boolean restored() {
        return (this.message != null || this.topic != null || this.info != null);
    }

    /**
     * restore data from cp
     */
    protected void restore() throws SharkKBException {
        if (!this.restored()) {
            // restore from cp

            if (this.cp == null) {
                L.w("no context point set from which to restore chat entries", this);
                throw new SharkKBException("no context point set from which to restore chat entries");
            }

            Information entryInfo = this.cp.enumInformation().nextElement();
            String contentType = entryInfo.getContentType();

            if (contentType.equalsIgnoreCase(CHAT_MESSAGE_ENTRY_TYPE)) {
                // text message
                this.message = new String(entryInfo.getContentAsByte());
                this.info = entryInfo;
            } else if (contentType.equalsIgnoreCase(CHAT_SI_ENTRY_TYPE)) {
                String siString = new String(entryInfo.getContentAsByte());
                String[] sis = Util.string2array(siString);

                this.topic = this.baseKB.getSemanticTag(sis);

                if (this.topic == null) {
                    throw new SharkKBException("referenced topic cannot be found in knowledge base");
                }
            } else {
                // another information
                this.info = entryInfo;
            }
        }
    }
}
