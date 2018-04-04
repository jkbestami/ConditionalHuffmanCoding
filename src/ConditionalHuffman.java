/**
 *
 * An implementation of Conditional Huffman Coding:
 *
 *   A Huffman tree is generated for each three letter prefix
 *   with the frequencies of the letter following the prefix
 *
 *   To encode: the first three letters are encoded with a "default" huffman tree
 *              after that, each letter is encoded with the huffman tree of the
 *              triplet prefix that precedes it.
 *
 *   To decode: the first three letters are decoded with a "default" huffman tree
 *              after that, each letter is decoded with the huffman tree of the
 *              triplet prefix that precedes it.
 *
 *
 */





import java.io.*;
import java.util.*;

public class ConditionalHuffman {

    public static void main(String[] args) throws IOException {

        String corpus;
        String toDecode;
        String toEncode="";
        Scanner scanner;


        while (true) try {

            //default corpus to Moby Dick
            if (args.length == 0) {
                corpus = "MobyDick";
            } else {
                cleanText(args[0],"cleaned");
                corpus = "cleaned";
            }

            Map<String, int[]> tripletToFreq;
            scanner = new Scanner(System.in);

            tripletToFreq = generateFreqMap(corpus);

            Map<String, String[]> tripletToCode = new HashMap<String, String[]>();
            List<String> triplets = generateTriplets();

            for (String triplet : triplets) {
                int[] arrayOfFreq = tripletToFreq.get(triplet);
                HuffmanCode code = new HuffmanCode(arrayOfFreq);
                code.getCodes(code.tree, new StringBuffer());
                tripletToCode.put(triplet, code.arrayOfCodes);
            }

            System.out.println("(1) Encode");
            System.out.println("(2) Decode");
            System.out.println("Press anything else to exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            //ENCODE
            if (choice == 1) {
                System.out.println("Input string to encode [a-z]: ");
                toEncode = scanner.nextLine();

                while (!isValidEncode(toEncode)) {

                    System.out.println("Not a valid input string.");
                    System.out.println("Input string to encode: ");
                    toEncode = scanner.nextLine();

                }
                String encoded = encode(tripletToCode, toEncode);

                System.out.println("encoded: " + encoded);

                System.out.println("decode back: " + decode(tripletToCode, encoded));

            }
            //DECODE
            else if (choice == 2) {

                System.out.println("Input string to decode: ");
                toDecode = scanner.nextLine();

                while (!isValidDecode(toDecode)) {

                    System.out.println("Not a valid input string.");
                    System.out.println("Input string to decode: ");
                    toDecode = scanner.nextLine();

                }
                String decoded = decode(tripletToCode, toDecode);

                System.out.println("decoded: " + decoded);

                System.out.println("encoded back: " + encode(tripletToCode, decoded));

            }
            //EXIT
            else{

                System.out.println("exiting...");
                break;

            }


        } catch (StringIndexOutOfBoundsException | java.lang.ArrayIndexOutOfBoundsException e2){
            System.out.println("This is not a valid encoding, try again");
        }
        catch(java.util.InputMismatchException e){
            System.out.println("exiting...");
            break;
        }
    }



    /**
     *
     * Makes sure that the string only contains lowercase letters and spaces
     * so it can be encoded
     *
     * @returns : true if valid
     * */
    public static boolean isValidEncode(String toEncode){


        return toEncode.matches("[a-z ]+");



    }

    /**
     *
     * Makes sure that the string only contains 0s and 1s
     * so it can be decoded
     *
     * @returns : true if valid
     * @caution:  not all binary string can be decoded
     * */
    public static boolean isValidDecode(String toDecode){
        return toDecode.matches("[10]+");

    }


    /**
     *
     * @param:  Map<String,String[]> tripletToCode: Triplet ----> Code Array ('a'=1 etc)
     *          String toEncode : string to encode
     *
     * @returns: String: encoded string
     * */
    public static String encode(Map<String,String[]> tripletToCode, String toEncode){

        String ret = "";


        //this is the code that is used for the 1st 3 chars
        String[] defaultCode = tripletToCode.get("   ");


        int firstChar = toEncode.charAt(0)-96;
        int secondChar = toEncode.charAt(1)-96;
        int thirdChar = toEncode.charAt(2)-96;
        ret = defaultCode[firstChar] + defaultCode[secondChar] + defaultCode[thirdChar];

        int beginIndex = 0;
        int endIndex = 3;
        String triplet = "";
        String[] codeArray;

        while(endIndex<toEncode.length()){

            //get triplet prefix
            triplet = toEncode.substring(beginIndex,endIndex);
            //find corresponding huffman code
            codeArray = tripletToCode.get(triplet);

            //encode the following char
            int nextChar;
            if(toEncode.charAt(endIndex)==' '){
                nextChar = 0;
            }else{
                nextChar = toEncode.charAt(endIndex)-96;
            }
            ret = ret+codeArray[nextChar];

            //advance the three chars "window" by one
            beginIndex++;
            endIndex++;
        }

        return ret;
    }



    /**
     * @param: Map<String,String[]> tripletToCode: Triplet ----> Code Array ('a'=1 etc)
     *          String toDecode : string to decode
     *
     * @returns: String: decoded string
     *
     * */
    public static String decode(Map<String,String[]> tripletToCode, String toDecode){

        String ret = "";
        String[] codeArray;

        int beginIndex = 0;
        int endIndex = 0;
        boolean match = false;

        int matches = 0; //how many characters did we match
        String tentative = "";

        while(endIndex<toDecode.length()) {

            //for first three chars use the "default" huffman code
            if(matches<3){
                codeArray = tripletToCode.get("   ");
            }else{
                //else, figure out what huffman code to use according to triplet prefix
                String triplet = ret.substring(matches-3, matches);
                codeArray = tripletToCode.get(triplet);
            }

            //append to tentative chars from toDecode one at a time until a match is found
            while (!match) {
                endIndex++;
                tentative = toDecode.substring(beginIndex, endIndex);

                //check for a match between tentative and the entries of codeArray
                for (int i = 0; i < codeArray.length; i++) {
                    match = tentative.equals(codeArray[i]);
                    if (match) {
                        matches ++;
                        if(i==0){
                            ret = ret + " ";
                        }else {
                            ret = ret + "" + (char) (i + 96);
                        }
                        break;
                    }
                }

            }

            match = false;

            //increment
            beginIndex = endIndex;

        }


        return ret;
    }

    /**
     * creates a new file name *outputFile* of the modified (lowercase letters and 1spaces only) corpus
     *
     * @param: String inputFile
     *         String outputFile
     *
     * @throws IOException
     *
     * */
    public static void cleanText(String inputFile, String outputFile) throws  IOException{

        String text;
        BufferedReader in = (new BufferedReader(new FileReader(inputFile)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));


        while((text = in.readLine())!=null){

            text = text.replaceAll("(?U)[^\\p{Alnum}]\\d+"," ").toLowerCase();
            text = text.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
            text = text.trim().replaceAll("[\\s+]+", " ");

            if(!text.matches("[\\s]*")) {
                writer.write(text + " ");
            }
        }
        writer.close();


    }


    /**
     *
     * Calculates the frequencies of the following character, given a triplet prefix, of the corpus
     *
     * @param: String corpusFileName
     * @returns: Map<String, int[]> triplet ---> frequency of char that follows
     * @throws IOException
     * */
    public static Map<String, int[]> generateFreqMap(String corpusFileName) throws IOException{

        Map<String, int[]> ret = initializeMap();
        FileReader fr=new FileReader(corpusFileName);
        BufferedReader br=new BufferedReader(fr);


        Character firstChar = (char) br.read();
        Character secondChar = (char) br.read();
        Character thirdChar = (char) br.read();
        int nextChar = br.read();

        while(nextChar >=97&&nextChar<=122||nextChar==' ') {

            String triplet = firstChar.toString() + secondChar.toString() + thirdChar.toString();

            firstChar = secondChar;
            secondChar = thirdChar;
            thirdChar = (char)nextChar;

            if (nextChar == ' ') {
                nextChar = 0;
            }else{
                nextChar = nextChar - 96;
            }

            int[] array = ret.get(triplet);

            int newValue = array[nextChar] + 1;
            array[nextChar] = newValue;


            ret.put(triplet, array);

            nextChar = br.read();

        }

        List<String> listOfTriplets = generateTriplets();

        for(String triplet:listOfTriplets){

            int[] array = ret.get(triplet);

            //account for characters that have 0 frequency
            //by multiplying every frequency by 10 and adding 1
            for(int i = 0; i<array.length; i++){
                array[i] = (array[i]*10)+1;
            }

            ret.put(triplet,array);
        }

        return ret;
    }


    public static Map<String, int[]> initializeMap(){

        ArrayList<String> triplets = generateTriplets();
        Map<String, int[]> ret = new HashMap<String, int[]>();

        for(String triplet: triplets){
            int[] arr = new int[27];
            ret.put(triplet, arr);
        }

        return ret;
    }



    public static ArrayList<String> generateTriplets() {

        char first = 'a';
        char second = 'a';
        char third = 'a';

        ArrayList<String> ret = new ArrayList<String>();


        for(int i = 0; i<27; i++){
            for(int j = 0; j<27; j++){
                for(int k= 0; k<27; k++){


                    Character firstChar = (char) (first+i);
                    Character secondChar = (char) (second+j);
                    Character thirdChar = (char) (third+k);


                    if(i==26){

                        firstChar = ' ';
                    }
                    if(j==26){

                        secondChar = ' ';
                    }
                    if(k==26){

                        thirdChar = ' ';
                    }

                    String prefix = firstChar.toString() + secondChar.toString() + thirdChar.toString();
                    ret.add(prefix);
                }
            }
        }
        return ret;
    }
}