package bpm.ippm;

import bpm.ippm.alignment.Correspondence;
import bpm.ippm.alignment.Result;
import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.matcher.Pipeline;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static java.lang.Math.abs;

public class ilpTests {

    //@Test
    public void basic3Test(){
        File folder = new File(getClass().getClassLoader().getResource("./pnml/app_store/").getFile());
        for (double i = 0.0; i <= 1.0; i += 0.2) {
            Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            Pipeline p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC3).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            for(File file1 : folder.listFiles()) {
                for(File file2 : folder.listFiles()) {

                    System.out.println(file1.toString() + " - " + file2.toString());
                    Result r1 = p1.run(file1, file2);
                    Result r2 = p2.run(file1, file2);

                    //equal matching cant be done since sometimes multiple matchings with same max target value exist
                    //Assert.assertTrue("i= "+ i+"\n" +file1.getName() + " - " +file2.getName() + ":" +r1.getAlignment().toString() + "\n" + r2.getAlignment().toString(), r1.getAlignment().equals(r2.getAlignment()));

                    //equal similarity
                    Assert.assertTrue("i= "+ i+"\n" +file1.getName() + " - " +file2.getName() + ":" + r1.getSimilarity() + "vs." + r2.getSimilarity(), abs(r1.getSimilarity() - r2.getSimilarity()) < 0.001);
                }
            }
        }
    }

    /**
     * Test if max similarity is 1
     */
    @Test
    public void maxSimTest(){
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_purchase_comp1.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_purchase_comp1.pnml").getFile());

        //Behavior only test
        Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(1).Build();
        Pipeline p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(1).Build();
        Pipeline p3 = new Pipeline.Builder().withILP(AbstractILP.ILP.CUSTOM_IDENTIFICATION).atSimilarityWeight(1).Build();

        Assert.assertTrue("BASIC Similarity is not 1: " + p1.run(f1,f2).getSimilarity(), abs(p1.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("SYMMETRIC Similarity is not 1: " + p2.run(f1,f2).getSimilarity(), abs(p2.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("CUSTOM_IDENTIFICATION Similarity is not 1: " + p3.run(f1,f2).getSimilarity(), abs(p3.run(f1,f2).getSimilarity() -1.0) <= 0.0001);

        //Label only test
        p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(0).Build();
        p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(0).Build();
        p3 = new Pipeline.Builder().withILP(AbstractILP.ILP.RELAXED).atSimilarityWeight(0).Build();

        Assert.assertTrue("BASIC Similarity is not 1: " + p1.run(f1,f2).getSimilarity(), abs(p1.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("SYMMETRIC Similarity is not 1: " + p2.run(f1,f2).getSimilarity(), abs(p2.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("RELAXED Similarity is not 1: " + p3.run(f1,f2).getSimilarity(), abs(p3.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
    }

    /**
     * Test if SYMMETRIC and BASIC result in the same target value when using symmetric profile BP
     */
    @Test
    public void basic2Test(){
        File folder = new File(getClass().getClassLoader().getResource("./pnml/app_store/").getFile());
        for (double i = 0; i <= 1.0; i += 0.2) {
            Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            Pipeline p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            for(File file1 : folder.listFiles()) {
                for(File file2 : folder.listFiles()) {
                    Result r1 = p1.run(file1, file2);
                    Result r2 = p2.run(file1, file2);

                    //equal similarity
                    Assert.assertTrue("i= "+ i+"\n" +file1.getName() + " - " +file2.getName() + ":" + r1.getSimilarity() + "vs." + r2.getSimilarity(), abs(r1.getSimilarity() - r2.getSimilarity()) < 0.001);
                }
            }
        }
    }

    //@Test
    public void basic4Test(){
        File folder = new File(getClass().getClassLoader().getResource("./pnml/app_store/").getFile());
        for (double i = 0; i <= 1.0; i += 0.2) {
            Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            Pipeline p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC4).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            for(File file1 : folder.listFiles()) {
                for(File file2 : folder.listFiles()) {
                    Result r1 = p1.run(file1, file2);
                    Result r2 = p2.run(file1, file2);

                    //equal similarity
                    Assert.assertTrue("i= "+ i+"\n" +file1.getName() + " - " +file2.getName() + ":" + r1.getSimilarity() + "vs." + r2.getSimilarity(), abs(r1.getSimilarity() - r2.getSimilarity()) < 0.001);
                }
            }
        }
    }

    //@Test
    public void quadraticTest(){
        File folder = new File(getClass().getClassLoader().getResource("./pnml/app_store/").getFile());
        for (double i = 0; i <= 1.0; i += 0.2) {
            Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            Pipeline p2 = new Pipeline.Builder().withILP(AbstractILP.ILP.QUADRATIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            for(File file1 : folder.listFiles()) {
                for(File file2 : folder.listFiles()) {
                    Result r1 = p1.run(file1, file2);
                    Result r2 = p2.run(file1, file2);

                    //equal similarity
                    Assert.assertTrue("i= "+ i+"\n" +file1.getName() + " - " +file2.getName() + ":" + r1.getSimilarity() + "vs." + r2.getSimilarity(), abs(r1.getSimilarity() - r2.getSimilarity()) < 0.001);
                }
            }
        }
    }

    /**
     * Test the confidence of two similar nets
     */
    @Test
    public void similarityFunctionTest(){
        //Pipeline.PRINT_ENABLED = true;
        File file1 = new File(getClass().getClassLoader().getResource("./pnml/test-files/test3nodes.pnml").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("./pnml/test-files/test2nodes.pnml").getFile());
        for (double i = 0; i <= 1.0; i += 0.2) {
            Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC).atSimilarityWeight(i).atPostprocessThreshold(0.0).Build();
            Result r1 = p1.run(file1, file1);

            //As we compare two times the same model it should get perfect similarity on process and activity level
            for (Correspondence c : r1.getAlignment().getCorrespondences()) {
                Assert.assertTrue(c.toString() + " has no likelihood of 1: " + c.getLikelihood(), c.getLikelihood() >= 0.999);
            }
        }
    }
}
