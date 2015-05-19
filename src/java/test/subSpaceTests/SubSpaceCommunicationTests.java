package subSpaceTests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import net.sharkfw.apps.sharknet.SharkNet;
import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetChatEntry;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.apps.sharknet.impl.SharkNetEngine;
import net.sharkfw.kep.SharkProtocolNotSupportedException;

import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkSecurityException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import usageExamples.TestConstants;

/**
 *
 * @author thsc
 */
public class SubSpaceCommunicationTests {
//    private static int keepConnectionAlive = Integer.MAX_VALUE;
    private static final int sleepSeconds = 1000;
    private static int keepConnectionAlive = sleepSeconds * 3;
    
    public SubSpaceCommunicationTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
     public void un_subscribing() throws SharkKBException, SharkNetException, InterruptedException, SharkSecurityException, IOException, SharkSubSpaceException, SharkProtocolNotSupportedException {
        SharkNet sharkNetAlice, sharkNetBob, sharkNetClara;
        
        L.setLogLevel(L.LOGLEVEL_ALL);
    
        // remove old version - if any
        FSSharkKB.removeFSStorage(TestConstants.kbFolder);
        
        // Bob
        SharkKB bobKB = new InMemoSharkKB();
        SharkEngine bobSE = new J2SEAndroidSharkEngine();
//        sharkNetClara = SharkNetEngine.createSharkNet(TestConstants.CLARAKBFOLDER);
        sharkNetBob = SharkNetEngine.createSharkNet(bobSE, bobKB);
//        sharkNetBob = SharkNetEngine.createSharkNet(TestConstants.BOBKBFOLDER);
        sharkNetBob.getSharkEngine().setConnectionTimeOut(keepConnectionAlive);
        
        // set owner
        PeerSemanticTag bobbobTag = sharkNetBob.getVocabulary().getPeerSTSet().createPeerSemanticTag("Bob", TestConstants.bobSIs, TestConstants.bobAddr);
        
        sharkNetBob.setOwner(bobbobTag);
        
        // set listener
        SNListener_Dummy bobListener = new SNListener_Dummy();
        sharkNetBob.addListener(bobListener);
        
        // open tcp
        bobSE.startTCP(TestConstants.BOBPORT);

        // Clara
//        sharkNetClara = SharkNetEngine.createSharkNet(TestConstants.CLARAKBFOLDER);
        SharkKB claraKB = new InMemoSharkKB();
        SharkEngine claraSE = new J2SEAndroidSharkEngine();
//        sharkNetClara = SharkNetEngine.createSharkNet(TestConstants.CLARAKBFOLDER);
        sharkNetClara = SharkNetEngine.createSharkNet(claraSE, claraKB);
        sharkNetClara.getSharkEngine().setConnectionTimeOut(keepConnectionAlive);
        
        // set owner
        PeerSemanticTag claraTag = sharkNetBob.getVocabulary().getPeerSTSet().createPeerSemanticTag("Clara", TestConstants.claraSIs, TestConstants.claraAddr);
        sharkNetClara.setOwner(claraTag);
        
        // set listener
        SNListener_Dummy claraListener = new SNListener_Dummy();
        sharkNetClara.addListener(claraListener);
        
        // open tcp
        claraSE.startTCP(TestConstants.CLARAPORT);

        // Alice
        SharkKB aliceKB = new InMemoSharkKB();
        SharkEngine aliceSE = new J2SEAndroidSharkEngine();
//        sharkNetClara = SharkNetEngine.createSharkNet(TestConstants.CLARAKBFOLDER);
        sharkNetAlice = SharkNetEngine.createSharkNet(aliceSE, aliceKB);
//        sharkNetAlice = SharkNetEngine.createSharkNet(TestConstants.ALICEKBFOLDER);
        sharkNetAlice.getSharkEngine().setConnectionTimeOut(keepConnectionAlive);
        
        // set owner
        PeerSemanticTag alicealiceTag = sharkNetAlice.getVocabulary().getPeerSTSet().createPeerSemanticTag("Alice", TestConstants.aliceSIs, TestConstants.aliceAddr);
        sharkNetAlice.setOwner(alicealiceTag);

        // open tcp
        aliceSE.startTCP(TestConstants.ALICEPORT);
        
        PeerSemanticTag alicebobTag = sharkNetAlice.getVocabulary().getPeerSTSet().createPeerSemanticTag("Bob", TestConstants.bobSIs, TestConstants.bobAddr);
        PeerSemanticTag aliceclaraTag = sharkNetAlice.getVocabulary().getPeerSTSet().createPeerSemanticTag("Clara", TestConstants.claraSIs, TestConstants.claraAddr);
        
        ArrayList<PeerSemanticTag> aliceChatPeers = new ArrayList<PeerSemanticTag>();
        aliceChatPeers.add(alicebobTag);
        
        SharkNetChat aliceChat = sharkNetAlice.createChat("aliceTestChat", null, aliceChatPeers.iterator());        
        
        // wait a second
        Thread.sleep(sleepSeconds);
        
        System.out.flush();
        System.err.flush();
        
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//                            Bob subscribes                                //");
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");
        

		{
			Integer iInviteDelay = 0; 
			boolean bIWantHalt = true;		
			while(bIWantHalt) {
				Thread.sleep(sleepSeconds/10);
				if (bobListener.lastInvitedChat != null) {
					bIWantHalt = false;
				}
				iInviteDelay++;			
				Assert.assertTrue("timeout in accepting invitation", iInviteDelay < 600);
			}	                
			System.out.println("invite delay:"+iInviteDelay/10.0);
		}	
        
        SharkNetChat bobChat = bobListener.lastInvitedChat;
        // remove - for testing
        bobListener.lastInvitedChat = null;
  
        // TODO
//        PeerSemanticTag bobInvitedChatOwner = bobChat.getOwner();
        
        // TODO
//        Assert.assertTrue(SharkCSAlgebra.identical(bobInvitedChatOwner, alicealiceTag));

        // subscribe
        // TODO
//        bobChat.subscribe();
        
        // and listen to new entries
        bobChat.addListener(bobListener);

        // give it a second
        Thread.sleep(sleepSeconds);
//        Thread.sleep(Integer.MAX_VALUE);
        
        // ok, Bob has subscribed - alice should know about it.
        // TODO
//        Enumeration<PeerSemanticTag> subscribedMember = aliceChat.subscribedMember();
        
        Enumeration<PeerSemanticTag> subscribedMember = null;
        Assert.assertNotNull(subscribedMember);
        
        boolean found = false;
        while(subscribedMember.hasMoreElements()) {
            PeerSemanticTag aliceChatBobSubscriber = subscribedMember.nextElement();
            if(SharkCSAlgebra.identical(aliceChatBobSubscriber, bobbobTag)) {
                found = true;
                break;
            }
        }
        
        // Alice has found Bob
        Assert.assertTrue(found);
        
        Thread.sleep(1000);
        
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//              Alice puts message into chat with Bob                        //");
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");

// alice writes a message
        String aliceText1 = "bob has entered our chat.";
        aliceChat.createEntry(aliceText1);
//        aliceChat.addTextMessage(aliceText1);
        
        // give it a second
        Thread.sleep(sleepSeconds);
//        Thread.sleep(Integer.MAX_VALUE);
        
        // bob should have now heard about it
        SharkNetChatEntry bobEntry = bobListener.lastChatEntry;
        bobListener.lastChatEntry = null;
        
        Assert.assertNotNull(bobEntry);

        String bobEntryString = bobEntry.getTextMessage();
        Assert.assertNotNull(bobEntryString);
        Assert.assertTrue(aliceText1.equals(bobEntryString));
        
        System.out.flush();
        System.err.flush();
        
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//                         Alice invites Clara                               //");
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");
        
        // invite clara
// TODO
//        aliceChat.inviteMember(claraTag, true);

        Thread.sleep(sleepSeconds);
        
        // for debugging
//        Thread.sleep(Integer.MAX_VALUE);
        
        Assert.assertNotNull(claraListener.lastInvitedChat);
        
        SharkNetChat claraChat = claraListener.lastInvitedChat;
        claraListener.lastInvitedChat = null;
        
        // subscribe
        System.out.flush();
        System.err.flush();
        
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//                            Clara subscribes                               //");
System.out.println(
"///////////////////////////////////////////////////////////////////////////////");
        
        /* subscribe and wish to receive any document that was 
        * already exchanged in that chat
        */
// TODO
//        claraChat.subscribe(0);
        
        // and listen to new entries
        claraChat.addListener(claraListener);

        // give it a second
        Thread.sleep(sleepSeconds);
//        Thread.sleep(Integer.MAX_VALUE);
        
        // ok, Alice should know clara now
// TODO        
//        subscribedMember = aliceChat.subscribedMember();
        
        Assert.assertNotNull(subscribedMember);
        
        found = false;
        while(subscribedMember.hasMoreElements()) {
            PeerSemanticTag subscriber = subscribedMember.nextElement();
            if(SharkCSAlgebra.identical(subscriber, claraTag)) {
                found = true;
                break;
            }
        }
        
        // Alice has found Clara
        Assert.assertTrue(found);
        
        // give it a second
        Thread.sleep(sleepSeconds);
//        Thread.sleep(Integer.MAX_VALUE);

        // Bob should also know clara
// TODO        
//        subscribedMember = bobChat.subscribedMember();
        
        Assert.assertNotNull(subscribedMember);
        
        found = false;
        while(subscribedMember.hasMoreElements()) {
            PeerSemanticTag subscriber = subscribedMember.nextElement();
            if(SharkCSAlgebra.identical(subscriber, claraTag)) {
                found = true;
                break;
            }
        }
        
        // Bob has found Clara
        Assert.assertTrue(found);
        
        // check if clara has received old messages in chat.
        SharkNetChatEntry claraEntry = claraListener.lastChatEntry;
        claraListener.lastChatEntry = null;
        
//        Assert.assertNotNull(claraEntry);
        // HIER WEITERMACHEN- siehe AbstractSubSpace.doExpose()
        
        // clara writes hello
        String claraText1 = "I'm so glad joining you.";
        claraChat.createEntry(claraText1);
//        claraChat.addTextMessage(claraText1);
        
        // give it some time
        Thread.sleep(sleepSeconds);
        
        // alice should have got it
        Assert.assertTrue(this.isIn(aliceChat, claraText1));
      
        
		{
			Integer iMessageDelay = 0; 
			boolean bIWantHalt = true;		
			while(bIWantHalt) {
				Thread.sleep(sleepSeconds/10);
				if (this.isIn(bobChat, claraText1)) {		
					bIWantHalt = false;
				}
				iMessageDelay++;			
				Assert.assertTrue("timeout in receiving message", iMessageDelay < 600);
			}	                
			System.out.println("message delay:"+iMessageDelay/10.0);
		}			
		
        Assert.assertTrue(this.isIn(bobChat, claraText1));		
        
        System.out.flush();
        System.err.flush();
        
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//                           Bob unsubscribes                               //");
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");
        // Bob unsubscribes
// TODO
//        bobChat.unsubscribe();
        
        Thread.sleep(sleepSeconds);
        
        System.out.flush();
        System.err.flush();
        
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");
System.out.println(
"//                         Alice second entry                               //");
System.out.println(
"//////////////////////////////////////////////////////////////////////////////");

        String aliceText2 = "Bob has unsubscribed";
        aliceChat.createEntry(aliceText2);
//        aliceChat.addTextMessage(aliceText2);
        
        Thread.sleep(sleepSeconds);
        
        // clara must have got that message
        Assert.assertTrue(this.isIn(claraChat, aliceText2));
        
        // bob must not have got it
        Assert.assertFalse(this.isIn(bobChat, aliceText2));
        
        // Alice closes chat.
        
        // Alice destroys chat
        
        // for debugging
//        Thread.sleep(Integer.MAX_VALUE);
        
        sharkNetAlice.getSharkEngine().stop();
        sharkNetBob.getSharkEngine().stop();
        sharkNetClara.getSharkEngine().stop();
        
        Thread.sleep(keepConnectionAlive);
        
        System.out.println("Test done!");
     }
    
    private boolean isIn(SharkNetChat chat, String text) throws SharkKBException {
        Enumeration<SharkNetChatEntry> entries = chat.entries();
        boolean found = false;
        while(entries.hasMoreElements()) {
             SharkNetChatEntry entry = entries.nextElement();
             if(entry.getTextMessage().equalsIgnoreCase(text)) {
                 found = true;
                 break;
             }
        }
        
        return found;
    }
}
