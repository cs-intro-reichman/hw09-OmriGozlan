import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
            String window = "";
            char c;
            In in = new In(fileName);
            for (int i = 0; i < windowLength; i++) {
                if (!in.isEmpty()) {
                    c = in.readChar();
                    window += c;
                } else {
                    break;
                }
            }
            while (!in.isEmpty()) {
                c = in.readChar();
                List probs = CharDataMap.get(window);
                if (probs == null) {
                    probs = new List();
                    CharDataMap.put(window, probs);
                }
                probs.update(c);
                window = window.substring(1) + c;
            }

            for (List probs : CharDataMap.values()) {
                calculateProbabilities(probs);
            }
        }


    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
    public void calculateProbabilities(List probs) {
        int totalChar = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData charD = probs.get(i);
            totalChar += charD.count;
        }

        double cp = 0;
        for (int i = 0; i <probs.getSize(); i++) {
            CharData node = probs.get(i);
            node.p = (double) node.count / totalChar;
            cp += node.p;
            node.cp = cp;
        }
    }




    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double rand = randomGenerator.nextDouble();
        char ans = ' ';
        for (int i = 0; i <probs.getSize() ; i++){
            if (probs.get(i).cp > rand){
                ans = probs.get(i).chr;
                break;
            }
        }
        return ans;
	}

    /**
	 * Generate a random text, based on the probabilities that were learned during training.
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param textLength - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
           int len = initialText.length();
            if (initialText.length() < windowLength) {
                return initialText;
            }
            StringBuilder generatedText = new StringBuilder(initialText);
            String currentWindow = initialText.substring(initialText.length() - windowLength);
            while (generatedText.length() -len < textLength) {
                List charDataList = CharDataMap.get(currentWindow);
                if (charDataList == null || charDataList.getSize() == 0) {
                    break;
                }
                char generatedChar = getRandomChar(charDataList);
                generatedText.append(generatedChar);
                currentWindow = generatedText.substring(generatedText.length() - windowLength);
            }

            return generatedText.toString();
        }


    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
            int windowLength = Integer.parseInt(args[0]);
            String initialText = args[1];
            int generatedTextLength = Integer.parseInt(args[2]);
            Boolean randomGeneration = args[3].equals("random");
            String fileName = args[4];
            LanguageModel lm;
            if (randomGeneration)
                lm = new LanguageModel(windowLength);
            else
                lm = new LanguageModel(windowLength, 20);
            lm.train(fileName);
            System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
