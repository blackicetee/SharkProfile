package usageExamples;

/**
 *
 * @author thsc
 */
public class TestConstants {
    static public String kbFolder = "./sharkkb";
    
    static public String[] aliceSIs = new String[] {
        "http://www.sharksystem.net/alice.html"
    };
      
    static public String[] bobSIs = new String[] {
        "http://www.sharksystem.net/bob.html"
    };
      
    static public String[] claraSIs = new String[] {
        "http://www.sharksystem.net/clara.html"
    };
      
    public static final String ALICEKBFOLDER = TestConstants.kbFolder + "/alicekb";
    public static final String BOBKBFOLDER = TestConstants.kbFolder + "/bobkb";
    public static final String CLARAKBFOLDER = TestConstants.kbFolder + "/clarakb";

    public static final String ALICETCP = "tcp://localhost:5555";
    public static final int ALICEPORT = 5555;
    
    public static final String BOBTCP = "tcp://localhost:6666";
    public static final int BOBPORT = 6666;    
    
    public static final String CLARATCP = "tcp://localhost:7777";
    public static final int CLARAPORT = 7777;    
    
    public static String[] aliceAddr = new String[] {ALICETCP};
    public static String[] bobAddr = new String[] {BOBTCP};
    public static String[] claraAddr = new String[] {CLARATCP};
    
    /////////////////////////////////////////////////////////////////////
    //                            topics                               //
    /////////////////////////////////////////////////////////////////////
    
    public static final String javaSIS[] = new String[] {"http://java.sharksystem.net"};
    public static final String divingSIS[] = new String[] {"http://diving.sharksystem.net"};
}
