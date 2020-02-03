package bpm.profile;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;

public class BP extends AbstractProfile {
    BehaviouralProfile<NetSystem, Node> relations;


    public BP(NetSystem net){
        BPCreatorNet creator = BPCreatorNet.getInstance();
        relations = creator.deriveRelationSet(net);
    }

    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        RelSetType rel = relations.getRelationForEntities(n1, n2);
        switch(rel){
            case Order:
                return Relation.BP_ORDER;
            case Exclusive:
                return Relation.BP_EXCLUSIVE;
            case Interleaving:
                return Relation.BP_INTERLEAVING;
            case ReverseOrder:
                return Relation.BP_REVERSE_ORDER;
            case None:
                return Relation.NONE;
        }
        return null;
    }

    @Override
    public Result filterTemporaryTransitions(Result result) {
        // no temporary transitions were added, therefore nothing needs to be removed
        return result;
    }

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2, Node n1, Node n2, Node m1, Node m2) {
        return getRelationSimilarity(r1,r2);
    }

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        if (r1 == r2) {
            return 1.0;
        }else if(r1.equals(Relation.BP_ORDER) || (r1.equals(Relation.BP_REVERSE_ORDER) && r2.equals(Relation.BP_INTERLEAVING))){
            return 0.5; // when it is order instead of interleaving we can argue that half of the assumption is true
        }else if(r1.equals(Relation.BP_INTERLEAVING) && (r2.equals(Relation.BP_ORDER) || r2.equals(Relation.BP_REVERSE_ORDER))){
            return 0.5; // when it is order instead of interleaving we can argue that half of the assumption is true
        }else {
            return 0.0;
        }
    }

    @Override
    public String toString(){
        return relations.toString();
    }


}
