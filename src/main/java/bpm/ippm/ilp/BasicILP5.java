package bpm.ippm.ilp;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Correspondence;
import bpm.ippm.alignment.Result;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.profile.Relation;
import bpm.ippm.similarity.Matrix;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.Arrays;
import java.util.Set;

import static bpm.ippm.matcher.Pipeline.PRINT_ENABLED;


/**
 * This is the implementation with the identification function inside the target function directly.
 * We make use of the relational similarity score and do not only rely on equal relations!
 * ONLY this can handle the Log based approach!
 */
public class BasicILP5 extends AbstractILP {
    public BasicILP5(){

    }

    /**
     * Compute the basic 1:1 ILP behavior/label simialrity match.
     * like BASIC but:
     * The identification for similar behavioral relation is now not encoded in y directly anymore.
     * y describes if s is matched to t and s' is matched to t'. The information that the relation between both is equal/similar
     * comes in the target function into play!
     * Additionally we do not only regard the question "equal relation or not", but use the weighting defined in
     * AbstractRelationalProfile.getRelationSimilarity
     * @param relNet1 Profile of Net 1
     * @param relNet2 Profile of Net 2
     * @param net1 Net 1
     * @param net2 Net 2
     * @return
     * @throws GRBException
     */
    @Override
    public Result solve(AbstractProfile relNet1, AbstractProfile relNet2, Set<Transition> net1, Set<Transition> net2, Matrix matrix, Alignment preAlignment, String name) throws GRBException {
        //setup variables
        Transition[] nodeNet1 =  net1.toArray(new Transition[net1.size()]);
        Transition[] nodeNet2 =  net2.toArray(new Transition[net2.size()]);
        int nodesNet1 = nodeNet1.length;
        int nodesNet2 = nodeNet2.length;
        int min = Math.min(nodesNet1,nodesNet2);

        // X(i,j) == 1 iff i is matched to j
        GRBVar[][] x = new GRBVar[nodesNet1][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                x[i][j] = model.addVar(0.0, 1.0,0.0, GRB.BINARY, "x_"+i+"_"+j);
            }
        }

        // Y(i,k,j,l) == 1 iff i matched to j and k matched to l
        GRBVar[][][][] y = new GRBVar[nodesNet1][nodesNet1][nodesNet2][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        y[i][k][j][l] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_" + i + "_" + j+"_"+k+"_"+l);
                    }
                }
            }
        }

        // Objective weighted between behavioral correspondance and label similarity
        // Behavioral part with non-binary identification function of target function
        GRBLinExpr behavior = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        // compute the relational similarity
                        Relation rel1 = relNet1.getRelationForEntities(nodeNet1[i], nodeNet1[k]);
                        Relation rel2 = relNet2.getRelationForEntities(nodeNet2[j], nodeNet2[l]);
                        double relSim = relNet1.getRelationSimilarity(rel1,rel2);
                        // when relational sim is zero we dont add it to the target function
                        if(relSim > 0) {
                            behavior.addTerm(1.0 * relSim / (min * min), y[i][k][j][l]);
                        }
                    }
                }
            }
        }

        // Label Similarity Part of target function
        GRBLinExpr label = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                label.addTerm(matrix.between(nodeNet1[i],nodeNet2[j])/(min), x[i][j]);
            }
        }
        GRBLinExpr obj = new GRBLinExpr();
        obj.multAdd(this.similarityWeight, behavior);
        obj.multAdd(1-this.similarityWeight, label);

        model.setObjective(obj, GRB.MAXIMIZE);

        //further restricting the ILP. This is not changing the optimal solution but shrinks the searchspace further.
        model.addConstr(obj,GRB.LESS_EQUAL,1.0, "objective hint");
        model.addConstr(label,GRB.LESS_EQUAL,1.0, "objective hint");
        model.addConstr(behavior,GRB.LESS_EQUAL,1.0, "objective hint");


        // matching from at most one constraint
        for (int i = 0; i< nodesNet1; i++){
            GRBLinExpr con1 = new GRBLinExpr();
            con1.clear();
            for (int j = 0; j< nodesNet2; j++){
                con1.addTerm(1, x[i][j]);
            }
            model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // matching to at most one constraint
        for (int j = 0; j< nodesNet2; j++){
            GRBLinExpr con2 = new GRBLinExpr();
            con2.clear();
            for (int i = 0; i< nodesNet1; i++){
                con2.addTerm(1, x[i][j]);
            }
            model.addConstr(con2, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // linking between X and Y
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k < nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++){
                    for (int l = 0; l < nodesNet2; l++) {
                        GRBLinExpr con3 = new GRBLinExpr();
                        con3.clear();
                        con3.addTerm(2, y[i][k][j][l]);
                        con3.addTerm(-1, x[i][j]);
                        con3.addTerm(-1, x[k][l]);
                        model.addConstr(con3, GRB.LESS_EQUAL, 0, "linking");

                    }
                }
            }
        }

        // add prematches
        for (Correspondence c: preAlignment.getCorrespondences()){
            Node n1 =  c.getNet1Nodes().iterator().next();
            Node n2 =  c.getNet2Nodes().iterator().next();
            int i = Arrays.asList(nodeNet1).indexOf(n1);
            int j = Arrays.asList(nodeNet2).indexOf(n2);
            GRBLinExpr conPre = new GRBLinExpr();
            conPre.addTerm(1,x[i][j]);
            model.addConstr(conPre, GRB.GREATER_EQUAL, 1, "pre matched");
        }


        // Optimize model
        model.optimize();

        //print alignment
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++) {
                if(PRINT_ENABLED) System.out.println(x[i][j].get(GRB.StringAttr.VarName) + " " +
                        x[i][j].get(GRB.DoubleAttr.X) +": "+nodeNet1[i].getLabel()+ "("+nodeNet1[i].getId()+")"
                        +" - "+ nodeNet2[j].getLabel()+ "("+nodeNet2[j].getId()+")");
            }
        }

        // construct the alignment from the ILP solution
        Alignment.Builder builder = new Alignment.Builder();
        for (int i = 0; i< nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                if( Math.abs(x[i][j].get(GRB.DoubleAttr.X) - 1.0) < 0.0001){

                    //compute confidence
                    double mixedLikelihood = 0.0;
                    for (int k =0; k< nodesNet1; k++){
                        for (int l = 0; l< nodesNet2; l++){
                            // compute the relational similarity
                            Relation rel1 = relNet1.getRelationForEntities(nodeNet1[i], nodeNet1[k]);
                            Relation rel2 = relNet2.getRelationForEntities(nodeNet2[j], nodeNet2[l]);
                            double relSim = relNet1.getRelationSimilarity(rel1,rel2);
                            // when relational sim is zero we dont add it to the target function
                            if(relSim  > 0) {
                                  mixedLikelihood += relSim *x[i][j].get(GRB.DoubleAttr.X) * x[k][l].get(GRB.DoubleAttr.X);
                            }
                        }
                    }
                    mixedLikelihood = mixedLikelihood *similarityWeight/min;
                    mixedLikelihood += (1-similarityWeight) * matrix.between(nodeNet1[i],nodeNet2[j]);
                    builder.addCorrespondence(new Correspondence.Builder().addNodeFromNet1(nodeNet1[i]).addNodeFromNet2(nodeNet2[j]).withLikelihood(mixedLikelihood).build());
                }
            }
        }

        //create the Result object
        Result res = new Result(model.get(GRB.DoubleAttr.ObjVal),builder.build(name), model.get(GRB.DoubleAttr.MIPGap));

        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }



}
