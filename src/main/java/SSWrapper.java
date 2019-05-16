import org.rakib.UI.SentiStrengthSEUI;
import org.rakib.utility.FileUtility;
import uk.ac.wlv.sentistrength.SentiStrength;

import java.util.ArrayList;
import java.util.Map;

public class SSWrapper {

  private SentiStrength ss;
  private FileUtility util;
  private Map termsMap;

  public SSWrapper() throws Exception {
    this.ss = new SentiStrength();
    this.util = new FileUtility();

    String init[] = {"sentidata", "/path/to/config/files", "explain"};
    this.ss.initialise(init);

    this.termsMap = util.GetModifiedTermsMap("ModifiedTermsLookupTable.txt");
    ArrayList<String> emoticons = util.GetEmoTerms("EmoticonLookupTable.txt");
    this.util.listOfEmoticon = emoticons;
    SentiStrengthSEUI.emoTerms = util.GetEmoTerms("EmotionLookupTable.txt");
  }

  public String process(String text) {
    return this.util.GetProcessedLine(text, null, this.termsMap);
  }

  public String compute(String text) {
    return this.ss.computeSentimentScores(text);
  }

}
