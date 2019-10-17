package bpm.matcher;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.jbpt.petri.Node;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Unit Tests for Alignments
 */
public class AlignmentTest {

    @Test
    public void complexCorrespondenceTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();
        Assert.assertTrue(c.isComplexCorrespondence());

        c = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p1n2)
                .build();
        Assert.assertTrue(!c.isComplexCorrespondence());
    }

    @Test(expected = Exception.class)
    public void properCorrespondenceTest(){
        new Correspondence.Builder().build();
    }

    @Test
    public void MutuallyExclusivenessTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        Assert.assertTrue(!a.isMutuallyExclusive());

        a = new Alignment.Builder().addCorrespondence(c1).build();
        Assert.assertTrue(a.isMutuallyExclusive());
    }

    @Test
    public void MappedTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        System.out.println(a.toString());
        Assert.assertTrue(a.isMapped(p1n1,p2n1));
        Assert.assertTrue(a.isMapped(p1n2,p2n1));
        Assert.assertTrue(!a.isMapped(p1n1,p1n2));
        Assert.assertTrue(!a.isMapped(p1n1,p1n1));
    }

    @Test
    public void getCorrespondenceTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        Assert.assertTrue(a.getCorrespondenceOfNode(p1n1).size() == 1);
        Assert.assertTrue(a.getCorrespondenceOfNode(p2n1).size() == 2);
    }

    @Test
    public void nodeEqualityTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id1");
        Node n3 = new Node("n1");
        n3.setId("id2");

        // Equals function test
        Assert.assertTrue(n1.equals(n2));
        Assert.assertTrue(!n1.equals(n3));
        Assert.assertTrue(!n2.equals(n3));

        // HashCode Test
        Assert.assertTrue(n1.hashCode() == n2.hashCode());
        Assert.assertTrue(n1.hashCode() != n3.hashCode());
        Assert.assertTrue(n2.hashCode() != n3.hashCode());
    }

    @Test
    public void correspondeceEqualityTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id2");
        Node n3 = new Node("n3");
        n3.setId("id3");
        Node n4 = new Node("n4");
        n4.setId("id1");
        Node n5 = new Node("n5");
        n5.setId("id2");
        Node n6 = new Node("n5");
        n6.setId("id3");

        Correspondence c1 = new Correspondence.Builder().addNodeFromNet1(n1).addNodeFromNet1(n2).addNodeFromNet2(n3).build();
        Correspondence c2 = new Correspondence.Builder().addNodeFromNet1(n4).addNodeFromNet1(n5).addNodeFromNet2(n6).build();
        Alignment a = new Alignment.Builder().add(n1,n3).add(n2,n3).build();
        Correspondence c3 = new Correspondence.Builder().addNodeFromNet1(n4).addNodeFromNet2(n6).build();

        // Equals function test
        Assert.assertTrue(c1.equals(c2));
        Assert.assertTrue(a.getCorrespondences().iterator().next().equals(c1));
        Assert.assertTrue(!c1.equals(c3));
        Assert.assertTrue(!c2.equals(c3));
        Assert.assertTrue(c2.equals(c1));
        Assert.assertTrue(c1.equals(a.getCorrespondences().iterator().next()));
        Assert.assertTrue(!c3.equals(c1));
        Assert.assertTrue(!c3.equals(c2));

        // HashCode Test
        Assert.assertTrue(c1.hashCode() == c2.hashCode());
        Assert.assertTrue(a.getCorrespondences().iterator().next().hashCode() == c1.hashCode());
        Assert.assertTrue(c1.hashCode() != c3.hashCode());
        Assert.assertTrue(c2.hashCode() != c3.hashCode());
    }

    @Test
    public void alignmentContainsTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id2");
        Node n3 = new Node("n3");
        n3.setId("id3");

        Node n4 = new Node("n1");
        n4.setId("id1");
        Node n5 = new Node("n2");
        n5.setId("id2");
        Node n6 = new Node("n3");
        n6.setId("id3");

        Alignment a = new Alignment.Builder().add(n1,n3).add(n2,n3).build();

        Correspondence c1 = new Correspondence.Builder().addNodeFromNet1(n4).addNodeFromNet1(n5).addNodeFromNet2(n6).build();
        Correspondence c2 = new Correspondence.Builder().addNodeFromNet1(n4).addNodeFromNet2(n6).build();


        Assert.assertTrue(a.getCorrespondences().iterator().next().equals(c1));
        Assert.assertTrue(a.getCorrespondences().iterator().next().hashCode() == c1.hashCode());

        //this does work
        Assert.assertTrue(a.contains(c1));

        // this doesnt work
        //Assert.assertTrue(a.getCorrespondences().contains(c1));

        //Assert.assertTrue(!a.getCorrespondences().contains(c2));



    }

}
