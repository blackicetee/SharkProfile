package SharkPlayground;

import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;

/**
 * Created by Mr.T on 08.05.2015.
 */
public class PlayWithMerge {
    public static void main(String args[]) throws SharkKBException {
        STSet plainSet = InMemoSharkKB.createInMemoSTSet();
        SemanticTag berlin = plainSet.createSemanticTag("Berlin", "http://www.berlin.de");
        SemanticTag hamburg = plainSet.createSemanticTag("Hamburg", "http://www.hamburg.de");
        SemanticTag bremen = plainSet.createSemanticTag("Bremen", "http://www.bremen.de");

        System.out.println(L.stSet2String(plainSet));
        System.out.println("---------------------------------------------------");

        STSet plainSet2 = InMemoSharkKB.createInMemoSTSet();

        plainSet.removeSemanticTag(berlin);
        SemanticTag paris = InMemoSharkKB.createInMemoSemanticTag("Paris", "http://www.paris.fr");
        SemanticTag paris2 = plainSet.merge(paris);

        System.out.println(L.stSet2String(plainSet));
        System.out.println("---------------------------------------------------");

        InMemoSharkKB kb = new InMemoSharkKB();
        STSet topicSTSet = kb.getTopicSTSet();

        System.out.println(L.stSet2String(topicSTSet));
        System.out.println("---------------------------------------------------");

        topicSTSet.merge(plainSet);

        System.out.println(L.stSet2String(topicSTSet));
        System.out.println("---------------------------------------------------");

        plainSet.getSemanticTag(paris.getSI());

        SharkCSAlgebra.isAny(plainSet);

        SharkCSAlgebra.identical(plainSet, plainSet2);

        plainSet.merge(paris);
        plainSet.merge(plainSet2);
    }
}
