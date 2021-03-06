package bpm.ippm;

import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.matcher.Pipeline;
import bpm.ippm.profile.AbstractProfile;

import bpm.ippm.profile.AlphaRelations;
import bpm.ippm.profile.Relation;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Testing of the Alpha Relations Profile Implementation
 */
public class AlphaRelationTest {
    @Test
    public void AlphaRelationBasicTest(){
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("Name 2");
        t2.setLabel("Name 2");

        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("Name 3");
        t3.setLabel("Name 3");


        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);
        Assert.assertSame(alpha.getRelationForEntities(t1,t2).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
    }

    /**
     * Check if it can deal with tau transitions
     */
    @Test
    public void AlphaRelationTauTest() {
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");


        //TAU transition.
        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("t2");
        t2.setLabel("");

        //TAU transition.
        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("t3");
        t3.setLabel("");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("Name 4");
        t4.setLabel("Name 4");

        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");
        Place p5 = new Place("p5");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);
        net1.addPlace(p5);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);
        net1.addEdge(p4,t4);
        net1.addEdge(t4,p5);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);
        Assert.assertSame(alpha.getRelationForEntities(t1,t2).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t4).getType(), Relation.RelationType.ALPHA_ORDER);
    }

    /**
     * Check if it can deal with loops
     */
    @Test
    public void AlphaRelationLoopTest() {
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("t2");
        t2.setLabel("Name 2");

        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("Name 3");
        t3.setLabel("Name 3");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("Name 4");
        t4.setLabel("Name 4");

        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");


        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);

        net1.addEdge(p3,t4);
        net1.addEdge(t4,p2);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);

        Assert.assertSame(alpha.getRelationForEntities(t1,t2).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3).getType(), Relation.RelationType.ALPHA_ORDER);

        Assert.assertSame(alpha.getRelationForEntities(t2,t4).getType(), Relation.RelationType.ALPHA_INTERLEAVING);
        Assert.assertSame(alpha.getRelationForEntities(t4,t2).getType(), Relation.RelationType.ALPHA_INTERLEAVING);

        Assert.assertSame(alpha.getRelationForEntities(t1,t4).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t4,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);

        Assert.assertSame(alpha.getRelationForEntities(t2,t2).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t4,t4).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
    }

    /**
     * Check if loops are handled correctly (2)
     */
    @Test
    public void AlphaRelationTwoTransitionLoopTest() {
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("Name 2");
        t2.setLabel("Name 2");

        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("Name 3");
        t3.setLabel("Name 3");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("Name 4");
        t4.setLabel("Name 4");

        Transition t5 = new Transition();
        t5.setId("t5");
        t5.setName("Name 5");
        t5.setLabel("Name 5");

        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);
        net1.addTransition(t5);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");
        Place p5 = new Place("p5");


        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);
        net1.addPlace(p5);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);

        net1.addEdge(p3,t4);
        net1.addEdge(t4,p5);
        net1.addEdge(p5,t5);
        net1.addEdge(t5,p2);


        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);

        Assert.assertSame(alpha.getRelationForEntities(t1,t2).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3).getType(), Relation.RelationType.ALPHA_ORDER);

        Assert.assertSame(alpha.getRelationForEntities(t2,t4).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t4,t5).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t5,t2).getType(), Relation.RelationType.ALPHA_ORDER);

        Assert.assertSame(alpha.getRelationForEntities(t1,t4).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t5).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);

        Assert.assertSame(alpha.getRelationForEntities(t1,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t4,t3).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);

        Assert.assertSame(alpha.getRelationForEntities(t2,t2).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t4,t4).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
    }

    /**
     * Check if concurrency is handled well
     */
    @Test
    public void AlphaConcurrentTest() {
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("Name 2");
        t2.setLabel("Name 2");

        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("Name 3");
        t3.setLabel("Name 3");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("Name 4");
        t4.setLabel("Name 4");

        Transition t5 = new Transition();
        t5.setId("t5");
        t5.setName("Name 5");
        t5.setLabel("Name 5");

        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);
        net1.addTransition(t5);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");
        Place p5 = new Place("p5");
        Place p6 = new Place("p6");
        Place p7 = new Place("p7");


        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);
        net1.addPlace(p5);
        net1.addPlace(p6);

        net1.addEdge(p1,t1);

        net1.addEdge(t1,p2);
        net1.addEdge(t1,p3);

        net1.addEdge(p2,t2);
        net1.addEdge(t2,p4); // End branch 1

        net1.addEdge(p3,t3);
        net1.addEdge(t3,p5);

        net1.addEdge(p5,t4);
        net1.addEdge(t4,p6); // End branch 2

        net1.addEdge(p4,t5);
        net1.addEdge(p6,t5);

        net1.addEdge(t5,p7);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);

        Assert.assertSame(alpha.getRelationForEntities(t1,t2).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t1,t4).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t5).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);

        Assert.assertSame(alpha.getRelationForEntities(t2,t3).getType(), Relation.RelationType.ALPHA_INTERLEAVING);
        Assert.assertSame(alpha.getRelationForEntities(t2,t4).getType(), Relation.RelationType.ALPHA_INTERLEAVING);

        Assert.assertSame(alpha.getRelationForEntities(t2,t5).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t3,t5).getType(), Relation.RelationType.ALPHA_EXCLUSIVE);

        Assert.assertSame(alpha.getRelationForEntities(t2,t5).getType(), Relation.RelationType.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t4,t5).getType(), Relation.RelationType.ALPHA_ORDER);
    }

    /**
     * Real test on two SAP models
     */
    @Test
    public void realSAP(){
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/sap/sap12s.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/sap/sap12t.pnml").getFile());

        //Behavior only test
        Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(0.3).withILPTimeLimit(1).withProfile(AbstractProfile.Profile.ARP).Build();
        p1.run(f1,f2);
    }

    /**
     * Real test on two Uni models
     */
    @Test
    public void failedUni(){
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/uni/Cologne.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/uni/Wuerzburg.pnml").getFile());

        //Behavior only test
        Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.SYMMETRIC).atSimilarityWeight(0.3).withILPTimeLimit(1).withProfile(AbstractProfile.Profile.ARP).Build();
        p1.run(f1,f2);
    }

}
