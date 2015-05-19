import net.sharkfw.apps.sharknet.SharkNet;
import net.sharkfw.apps.sharknet.impl.SharkNetEngine;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.SharkEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author thsc
 */
public class SharkNetEngineTests {
    
    public SharkNetEngineTests() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

     @Test
     public void basics() {
         SharkKB baseKB = new InMemoSharkKB();
         SharkEngine se = new J2SEAndroidSharkEngine();
         SharkNet sn = SharkNetEngine.createSharkNet(se, baseKB);
     }
}
