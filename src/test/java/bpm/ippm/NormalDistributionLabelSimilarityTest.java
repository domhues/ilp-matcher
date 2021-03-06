package bpm.ippm;

import bpm.ippm.alignment.Result;
import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.matcher.Pipeline;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.LabelSimilarity;
import bpm.ippm.similarity.NormalDistributionLabelSimilarity;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Test the normal distribution noise for similarity evaluation
 */
public class NormalDistributionLabelSimilarityTest {

    /**
     * Test if caching over several iterations works properly
     */
    @Test
    public void functionalityTest(){

        NormalDistributionLabelSimilarity l = new NormalDistributionLabelSimilarity();

        String a = "Label A";
        String b = "Label B";
        double sim1 = l.sim(a,b);
        double sim2 = l.sim(a,b);
        double sim3 = l.sim(b,a);
        double sim4 = l.sim(a,b);

        Assert.assertTrue("1) Sim is not equal: " + sim1 +" " + sim2,sim1 == sim2);
        Assert.assertTrue("2) Sim is not equal: " + sim1 +" " + sim3,sim1 == sim3);
        Assert.assertTrue("3) Sim is not equal: " + sim1 +" " + sim4,sim1 == sim4);

        // test if sims are same over several executions!
        l = new NormalDistributionLabelSimilarity();
        double sim5 = l.sim(a,b);

        Assert.assertTrue("1) Sim is not equal: " + sim1 +" " + sim5,sim1 == sim2);


    }


    // Execute this to get the actual values for the Paper.
    //@Test
    public void simTest(){
        File path = new File("eval-data/pnml/sim-comp");

        for(File f1 : path.listFiles()) {
            for(File f2 : path.listFiles()) {
                Pipeline matcher = new Pipeline.Builder()
                        .atSimilarityWeight(0.5)
                        .withLabelSimilarity(LabelSimilarity.Similarities.NORMAL_DISTRIBUTION)
                        .withProfile(AbstractProfile.Profile.BPP)
                        .atPostprocessThreshold(0.0)
                        .withILP(AbstractILP.ILP.SYMMETRIC).Build();

                //System.out.println(f1.toString() +f2.toString());
                Result res = matcher.run(f1,f2);
                System.out.println(matcher.toString());
                System.out.println(res);
            }
        }


    }


}
