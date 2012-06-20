package uk.ac.ebi.interpro.scan.search.sequence.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests {@link SequenceHelper}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class SequenceHelperTest {

    private final static String BRCA1_WITHOUT_HEADER =
            "MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQ\n" +
            "CPLCKNDITKRSLQESTRFSQLVEELLKIICAFQLDTGLEYANSYNFAKKENNSPEHLKD\n" +
            "EVSIIQSMGYRNRAKRLLQSEPENPSLQETSLSVQLSNLGTVRTLRTKQRIQPQKTSVYI\n" +
            "ELGSDSSEDTVNKATYCSVGDQELLQITPQGTRDEISLDSAKKAACEFSETDVTNTEHHQ\n" +
            "PSNNDLNTTEKRAAERHPEKYQGSSVSNLHVEPCGTNTHASSLQHENSSLLLTKDRMNVE\n" +
            "KAEFCNKSKQPGLARSQHNRWAGSKETCNDRRTPSTEKKVDLNADPLCERKEWNKQKLPC\n" +
            "SENPRDTEDVPWITLNSSIQKVNEWFSRSDELLGSDDSHDGESESNAKVADVLDVLNEVD\n" +
            "EYSGSSEKIDLLASDPHEALICKSERVHSKSVESNIEDKIFGKTYRKKASLPNLSHVTEN\n" +
            "LIIGAFVTEPQIIQERPLTNKLKRKRRPTSGLHPEDFIKKADLAVQKTPEMINQGTNQTE\n" +
            "QNGQVMNITNSGHENKTKGDSIQNEKNPNPIESLEKESAFKTKAEPISSSISNMELELNI\n" +
            "HNSKAPKKNRLRRKSSTRHIHALELVVSRNLSPPNCTELQIDSCSSSEEIKKKKYNQMPV\n" +
            "RHSRNLQLMEGKEPATGAKKSNKPNEQTSKRHDSDTFPELKLTNAPGSFTKCSNTSELKE\n" +
            "FVNPSLPREEKEEKLETVKVSNNAEDPKDLMLSGERVLQTERSVESSSISLVPGTDYGTQ\n" +
            "ESISLLEVSTLGKAKTEPNKCVSQCAAFENPKGLIHGCSKDNRNDTEGFKYPLGHEVNHS\n" +
            "RETSIEMEESELDAQYLQNTFKVSKRQSFAPFSNPGNAEEECATFSAHSGSLKKQSPKVT\n" +
            "FECEQKEENQGKNESNIKPVQTVNITAGFPVVGQKDKPVDNAKCSIKGGSRFCLSSQFRG\n" +
            "NETGLITPNKHGLLQNPYRIPPLFPIKSFVKTKCKKNLLEENFEEHSMSPEREMGNENIP\n" +
            "STVSTISRNNIRENVFKEASSSNINEVGSSTNEVGSSINEIGSSDENIQAELGRNRGPKL\n" +
            "NAMLRLGVLQPEVYKQSLPGSNCKHPEIKKQEYEEVVQTVNTDFSPYLISDNLEQPMGSS\n" +
            "HASQVCSETPDDLLDDGEIKEDTSFAENDIKESSAVFSKSVQKGELSRSPSPFTHTHLAQ\n" +
            "GYRRGAKKLESSEENLSSEDEELPCFQHLLFGKVNNIPSQSTRHSTVATECLSKNTEENL\n" +
            "LSLKNSLNDCSNQVILAKASQEHHLSEETKCSASLFSSQCSELEDLTANTNTQDPFLIGS\n" +
            "SKQMRHQSESQGVGLSDKELVSDDEERGTGLEENNQEEQSMDSNLGEAASGCESETSVSE\n" +
            "DCSGLSSQSDILTTQQRDTMQHNLIKLQQEMAELEAVLEQHGSQPSNSYPSIISDSSALE\n" +
            "DLRNPEQSTSEKAVLTSQKSSEYPISQNPEGLSADKFEVSADSSTSKNKEPGVERSSPSK\n" +
            "CPSLDDRWYMHSCSGSLQNRNYPSQEELIKVVDVEEQQLEESGPHDLTETSYLPRQDLEG\n" +
            "TPYLESGISLFSDDPESDPSEDRAPESARVGNIPSSTSALKVPQLKVAESAQSPAAAHTT\n" +
            "DTAGYNAMEESVSREKPELTASTERVNKRMSMVVSGLTPEEFMLVYKFARKHHITLTNLI\n" +
            "TEETTHVVMKTDAEFVCERTLKYFLGIAGGKWVVSYFWVTQSIKERKMLNEHDFEVRGDV\n" +
            "VNGRNHQGPKRARESQDRKIFRGLEICCYGPFTNMPTDQLEWMVQLCGASVVKELSSFTL\n" +
            "GTGVHPIVVVQPDAWTEDNGFHAIGQMCEAPVVTREWVLDSVALYQCQELDTYLIPQIPH\n" +
            "SHY";

    // http://www.uniprot.org/uniprot/P38398.fasta
    private final static String BRCA1_WITH_HEADER =
            ">sp|P38398|BRCA1_HUMAN Breast cancer type 1 susceptibility protein OS=Homo sapiens GN=BRCA1 PE=1 SV=2\n" +
            BRCA1_WITHOUT_HEADER;

    private final static String CHARS_29        = "MDLSALRVEEVQNVINAMQKILECPICLE";
    private final static String CHARS_30        = "MDLSALRVEEVQNVINAMQKILECPICLEL";
    private final static String CHARS_31        = "MDLSALRVEEVQNVINAMQKILECPICLELI";
    private final static String CHARS_31_SPACES = "MDLSALRVEEVQNV INAMQKILECPICLELI";
    private final static String TEXT            = "Protein phosphatase 2C, manganese/magnesium aspartate binding site";

    @Test
    public void testIsProteinSequence() {

        // Sequences
        assertTrue("30 characters", SequenceHelper.isProteinSequence(CHARS_30));
        assertTrue("31 characters", SequenceHelper.isProteinSequence(CHARS_31));
        assertTrue("Header",        SequenceHelper.isProteinSequence(BRCA1_WITH_HEADER));
        assertTrue("No header",     SequenceHelper.isProteinSequence(BRCA1_WITHOUT_HEADER));

        // Not recognised as sequences
        assertFalse("29 characters",             SequenceHelper.isProteinSequence(CHARS_29));
        assertFalse("31 characters with spaces", SequenceHelper.isProteinSequence(CHARS_31_SPACES));
        assertFalse("Text",                      SequenceHelper.isProteinSequence(TEXT));

    }

    @Test
    public void testCalculateMd5() {
        final String BRCA1_MD5 = "e40f752dedf675e2f7c99142ebb2607a";
        assertEquals("Header",    BRCA1_MD5, SequenceHelper.calculateMd5(BRCA1_WITH_HEADER));
        assertEquals("No header", BRCA1_MD5, SequenceHelper.calculateMd5(BRCA1_WITHOUT_HEADER));
    }

}