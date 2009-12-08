package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for HMMER3 output, based upon the working parser used in Onion.
 * This has been designed to work as fast as possible, parsing a file
 * most of which is ignored.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 *
Query:       7tm_2  [M=242]
Accession:   PF00002.17
Description: 7 transmembrane receptor (Secretin family)
Scores for complete sequences (score includes all domains):
   --- full sequence ---   --- best 1 domain ---    -#dom-
    E-value  score  bias    E-value  score  bias    exp  N  Sequence      Description
    ------- ------ -----    ------- ------ -----   ---- --  --------      -----------
      8e-42  152.1   7.7    1.1e-41  151.6   5.3    1.2  1  UPI00000015B6


Domain and alignment annotation for each sequence:
>> UPI00000015B6
   #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
 ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
   1 !  151.6   5.3   1.3e-48   1.1e-41       4     242 .]    2376    2605 ..    2373    2605 .. 0.94

  Alignments for each domain:
  == domain 1    score: 151.6 bits;  conditional E-value: 1.3e-48
                     ----S----------------------------------------------------------------------------------------- CS
          7tm_2    4 lkvittvGlslSlvaLlvaivilllfrklrctrntihlnLflslilrailvlvkiaalenkeeeseakCkvvavflhYfllanffWllvEglyl 97
                     lk++t+v+l++ l+aLl++++ l+l+r lr++++ i  nL ++l l+++++l++i++ +   +     C+v+a++lh+++l++f W l+E+l+l
  UPI00000015B6 2376 LKTLTYVALGVTLAALLLTFFFLTLLRILRSNQHGIRRNLTAALGLAQLVFLLGINQADLPFA-----CTVIAILLHFLYLCTFSWALLEALHL 2464
                     799*****************************************************9999997.....************************** PP

                     -------------------------------------.--------.----------------------------------------------- CS
          7tm_2   98 ylllvltffserkklkvylliGwgvPavvvvvwaivrkagyenekc.WlsnekkllwiikgpvlviilvNfvllinilrvlvqklrsketseke 190
                     y++l++++  ++  +++y+++GwgvPa ++ ++++++++gy+n ++ Wls  ++l+w+++gpv++++ + ++l i   r  ++  r+  ++ k
  UPI00000015B6 2465 YRALTEVRDVNTGPMRFYYMLGWGVPAFITGLAVGLDPEGYGNPDFcWLSIYDTLIWSFAGPVAFAVSMSVFLYILAARASCAAQRQGFEK-KG 2557
                     *************************************99****8877****99*****************************998844444.44 PP

                     ---------------------------------------------------- CS
          7tm_2  191 kkrkklvkstlvllpLLGityvlflfapeekvssvvflyleailnslqGffv 242
                       +   ++ ++++l+LL+ t++l+l+ ++++  + +f+yl+a+ n++qG f+
  UPI00000015B6 2558 PVS--GLQPSFAVLLLLSATWLLALLSVNSD--TLLFHYLFATCNCIQGPFI 2605
                     444..58999********************5..8***************886 PP



Internal pipeline statistics summary:
-------------------------------------
Query model(s):                            1  (242 nodes)
Target sequences:                          1  (2923 residues)
Passed MSV filter:                         1  (1); expected 0.0 (0.02)
Passed bias filter:                        1  (1); expected 0.0 (0.02)


 */
public class Hmmer3SearchMatchParser<T extends RawMatch> implements MatchParser {

    private static final String END_OF_RECORD = "//";

    /**
     * Group 1: Model accession.
     */
    //private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile ("^[^:]*:\\s+(\\w+).*$" );
    private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile ("^[^:]*:\\s+(\\w+)\\s+\\[M=(\\d+)\\].*$" );

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */
//    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "inclusion threshold";

    private static final String DOMAIN_SECTION_START = ">> ";

    private static final String DOMAIN_ALIGNMENT_SECTION_START = "  ==";

    private static final String START_OF_DOMAIN_ALIGNMENT_SECTION = "Alignments for each domain";

    //Output File to write Gene3D parser output in ssf format suitable for Domain Finder input
    File ssfFile = new File("C:\\Manjula\\input_for_DF.txt");

    // Group 1: Uniparc protein accession
    private static final Pattern DOMAIN_SECTION_START_PATTERN = Pattern.compile("^>>\\s+(\\S+).*$");

    /**
     * This interface has a single method that
     * takes the HmmsearchOutputMethod object, containing sequence
     * and domain matches and converts it to RawProtein
     * objects.  The converter MAY perform additional steps, such as
     * filtering the raw results by specific criteria, such as GA value
     * cutoff.
     */
    private Hmmer3ParserSupport<T> hmmer3ParserSupport;

    @Required
    public void setParserSupport(Hmmer3ParserSupport<T> hmmer3ParserSupport) {
        this.hmmer3ParserSupport = hmmer3ParserSupport;
    }

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage{
        LOOKING_FOR_METHOD_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAIN_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        PARSING_DOMAIN_ALIGNMENTS,
        FINISHED_SEARCHING_RECORD
    }



    public Set<RawProtein<T>> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein<T>> rawResults = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            HmmsearchOutputMethod method = null;
            String currentSequenceIdentifier = null;
            Map<String, DomainMatch> domains = new HashMap<String, DomainMatch>();
            StringBuilder alignSeq = new StringBuilder();
            DomainMatch currentDomain =null;
            //generate ssf file for Domain Finder
            DomainFinderInputWriter dfiw = new DomainFinderInputWriter(ssfFile);
            ParsingStage stage = ParsingStage.LOOKING_FOR_METHOD_ACCESSION;
            //Matcher domainAlignSequenceMatcher = null;
            int lineNumber = 0;
            while (reader.ready()){
                lineNumber++;
                String line = reader.readLine();
                if (line.startsWith(END_OF_RECORD)){
                    // Process a complete record - store all sequence / domain scores
                    // for the method.
                    if (method == null){
                        throw new ParseException("Got to the end of a hmmscan full output file section without finding any details of a method.", null, line, lineNumber);
                    }
                    // Store the matches to the method.
                    hmmer3ParserSupport.addMatch (method, rawResults);

                    method = null;  // Will check if method is not null after finishing the file, and store it if so.
                    stage = ParsingStage.LOOKING_FOR_METHOD_ACCESSION;
                }
                else {   // Trying to be efficient - only look for EXPECTED lines in the entry.
                    switch (stage){
                        case LOOKING_FOR_METHOD_ACCESSION:
                            //if (hmmer3ParserSupport.getHmmKey()== Hmmer3ParserSupport.HmmKey.ACCESSION)
                            //if (line.startsWith(MODEL_ACCESSION_LINE)){
                            if(line.startsWith( hmmer3ParserSupport.getHmmKey().getPrefix())) {
                                stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                                Matcher queryLineMatcher = MODEL_ACCESSION_LINE_PATTERN.matcher(line);
                                if (queryLineMatcher.matches()){
                                    //System.out.println(queryLineMatcher.group(1) + " ::: " + queryLineMatcher.group(2));
                                    method = new HmmsearchOutputMethod(queryLineMatcher.group(1));
                                    method.setMethodAccessionLength(Integer.parseInt(queryLineMatcher.group(2)));
                                }
                                else {
                                    throw new ParseException("Found a line starting with " + hmmer3ParserSupport.getHmmKey().getPrefix() + " but cannot parse it with the MODEL_ACCESSION_LINE regex.",null, line, lineNumber);
                                }
                            }
                            break;

                        case LOOKING_FOR_SEQUENCE_MATCHES:
                            // Sanity check.
                            if (method == null){
                                throw new ParseException("The parse stage is 'looking for sequence matches' however there is no method in memory.", null, line, lineNumber);
                            }
                            // Got to the end of the sequence matches that pass?
                            // Due to hmmer3 beta version peculiarities we cannot currently use
                            // the inclusion threshold marking to find the end of the matches, se we take them all
                            // Original code left in comments in case we can ever put it back
                            if (/*line.contains(END_OF_GOOD_SEQUENCE_MATCHES) || */ line.trim().length() == 0){
                                // If there are no good sequence matches, completely stop searching this record.
                                stage = (method.getSequenceMatches().size() == 0)
                                        ? ParsingStage.FINISHED_SEARCHING_RECORD
                                        : ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                                currentSequenceIdentifier=null;
                                currentDomain=null;
                            }
                            else {

                                Matcher sequenceMatchLineMatcher = SequenceMatch.SEQUENCE_LINE_PATTERN.matcher(line);
                                if (sequenceMatchLineMatcher.matches()){
                                    // Found a sequence match line above the threshold.
                                    // Make a record of the UPI.
//                                    String upi = sequenceMatchLineMatcher.group(SequenceMatch.SEQUENCE_ID_GROUP);
                                    SequenceMatch sequenceMatch = new SequenceMatch(sequenceMatchLineMatcher);
                                    method.addSequenceMatch(sequenceMatch);
                                }
                            }
                            break;

                        case LOOKING_FOR_DOMAIN_SECTION:

                            if (line.startsWith(DOMAIN_SECTION_START)){

                                // Find out which model the domain matches are for and then parse them.
                                Matcher domainSectionHeaderMatcher = DOMAIN_SECTION_START_PATTERN.matcher(line);
                                if (domainSectionHeaderMatcher.matches()){
                                    domains.clear();
                                    currentSequenceIdentifier  = domainSectionHeaderMatcher.group(1);
                                }
                                else {
                                    throw new ParseException("This line looks like a domain section header line, but I cannot parse out the sequence id.", null, line, lineNumber);
                                }
                                stage = ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE;
                            }
                            
                            if (hmmer3ParserSupport.parseAlignments()) {
                                //to handle domain alignment
                                if ( line.startsWith(DOMAIN_ALIGNMENT_SECTION_START)){

                                    Matcher domainAlignmentMatcher = DomainMatch.DOMAIN_ALIGNMENT_LINE_PATTERN.matcher(line);
                                    if (domainAlignmentMatcher.matches()){

                                        alignSeq.setLength(0);
                                        String domainNumber = domainAlignmentMatcher.group(1);
                                        currentDomain = domains.get(domainNumber); //get the current domain object.
                                    } else {
                                        throw new ParseException("Unable to parse domain alignment section line", null, line, lineNumber);
                                    }
                                }
                                // getting the actual alignment sequence string

                                if ( (currentDomain!=null) && (currentSequenceIdentifier!=null) && (line.trim().startsWith(currentSequenceIdentifier) )){
                                    Matcher alignmentSequencePattern = DomainMatch.ALIGNMENT_SEQUENCE_PATTERN.matcher(line);
                                    if(alignmentSequencePattern.matches() && alignmentSequencePattern.group(1).equals(currentSequenceIdentifier)){
                                        alignSeq.append(alignmentSequencePattern.group(3));
                                        currentDomain.setAlignment(alignSeq.toString());
                                    } else {
                                        throw new ParseException("Unable to parse alignment", null, line, lineNumber);
                                    }
                                }
                            }

                            break;

                        case LOOKING_FOR_DOMAIN_DATA_LINE:

                            // Look for a domain data line.
                            Matcher domainDataLineMatcher = DomainMatch.DOMAIN_LINE_PATTERN.matcher(line);
                            if (line.contains(START_OF_DOMAIN_ALIGNMENT_SECTION)){
                                stage = ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                            }
                            else if (domainDataLineMatcher.matches()){
                                DomainMatch domainMatch = new DomainMatch(domainDataLineMatcher);
                                method.addDomainMatch(currentSequenceIdentifier, domainMatch);
                                
                                //before start of next sequence match store the method in ssf file
                                //send the parsed domain details to ssf file generation
                                  dfiw.writeMethodToFile(method,currentSequenceIdentifier,domainDataLineMatcher.group(1));
                                  domains.put(domainDataLineMatcher.group(1),domainMatch);
                            }
                            break;


                    }


                }
            }
            if (method != null){

               hmmer3ParserSupport.addMatch (method, rawResults);
            }

        } finally {
            if (reader != null){
                reader.close();
            }
        }
        return new HashSet<RawProtein<T>> (rawResults.values());
    }

}
