package bpm.ippm.ilp;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Correspondence;
import bpm.ippm.alignment.Result;
import bpm.ippm.profile.AbstractProfile;
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

// not usable results
@Deprecated
public class RelaxedILP3 extends AbstractILP {
    public RelaxedILP3(){

    }

    /**
     * Compute the Relaxed 1:1 *LP* behavior/label simialrity match.
     * Variables are contineous and the linking between x and y is split into two functions
     * Hint on Objective function is used.
     * We make use of the symmetry of the matrix
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

        GRBVar[][] x = new GRBVar[nodesNet1][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                x[i][j] = model.addVar(0.0, 1.0,0.0, GRB.CONTINUOUS, "x_"+i+"_"+j);
            }
        }
        
        GRBVar[][][][] y = new GRBVar[nodesNet1][nodesNet1][nodesNet2][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int k = i; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        y[i][k][j][l] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "y_" + i + "_" + k+"_"+j+"_"+l);
                    }
                }
            }
        }

        GRBVar sum = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_y");
        GRBVar sum_x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_x");

        // Objective weighted between behavioral correspondance and label similarity
        // Behavioral part
        GRBLinExpr behavior = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int k = i; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        //by reducing the number of variables, the total sum gets lower too, therefore we fix it by weighting
                        //every correct entry which is not on the diagonal twice (because of the symmetry of the matrix)
                        //TODO somehow the result is not yet the same as in Relaxed1
                        if(i!=k && j!=l) {
                            behavior.addTerm(2.0 / (min * min ), y[i][k][j][l]);
                        }else{
                            behavior.addTerm(1.0 / (min * min), y[i][k][j][l]);
                        }
                    }
                }
            }
        }

        // Label Similarity Part
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

        //setup model

        // hint expressions
        //model.addConstr(obj,GRB.LESS_EQUAL,1.0, "objective hint");
        //model.addConstr(label,GRB.LESS_EQUAL,1.0, "objective hint");
        //model.addConstr(behavior,GRB.LESS_EQUAL,1.0, "objective hint");


        // matching from at most one constraint
        for (int i = 0; i< nodesNet1; i++){
            GRBLinExpr con1 = new GRBLinExpr();
            con1.clear();
            for (int j = 0; j< nodesNet2; j++){
                con1.addTerm(1, x[i][j]);
            }
            model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // matching to at most 1 constraint
        for (int j = 0; j< nodesNet2; j++){
            GRBLinExpr con2 = new GRBLinExpr();
            con2.clear();
            for (int i = 0; i< nodesNet1; i++){
                con2.addTerm(1, x[i][j]);
            }
            model.addConstr(con2, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // linking between similar entries in the F matrices and the mapping
        for (int i = 0; i< nodesNet1; i++){
            for (int k = i; k < nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++){
                    for (int l = 0; l < nodesNet2; l++) {
                        if (relNet1.getRelationForEntities(nodeNet1[i], nodeNet1[k]).equals(relNet2.getRelationForEntities(nodeNet2[j], nodeNet2[l]))) {
                            GRBLinExpr con3 = new GRBLinExpr();
                            con3.clear();
                            con3.addTerm(-1, x[i][j]);
                            con3.addTerm(1, y[i][k][j][l]);
                            model.addConstr(con3, GRB.LESS_EQUAL, 0, "linking");

                            GRBLinExpr con4 = new GRBLinExpr();
                            con4.clear();
                            con4.addTerm(1, y[i][k][j][l]);
                            con4.addTerm(-1, x[k][l]);
                            model.addConstr(con4, GRB.LESS_EQUAL, 0, "linking");
                        } else {
                            GRBLinExpr con5 = new GRBLinExpr();
                            con5.clear();
                            con5.addTerm(1, y[i][k][j][l]);
                            model.addConstr(con5, GRB.EQUAL, 0, "zero setter");
                        }
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
            model.addConstr(conPre, GRB.EQUAL, 1, "pre matched");
        }

        // Optimize model
        model.optimize();

        //print alignment
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++) {
                if(PRINT_ENABLED) System.out.println(x[i][j].get(GRB.StringAttr.VarName) + " " + x[i][j].get(GRB.DoubleAttr.X));
            }
        }

        //Create Results
        Alignment.Builder builder = new Alignment.Builder();
        for (int i = 0; i< nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                if (x[i][j].get(GRB.DoubleAttr.X) >= 0.0001){
                    //TODO test what works better!
                    //builder.add(nodeNet1[i],nodeNet2[j]);
                    builder.addCorrespondence(new Correspondence.Builder().addNodeFromNet1(nodeNet1[i]).addNodeFromNet2(nodeNet2[j]).withLikelihood(x[i][j].get(GRB.DoubleAttr.X)).build());
                }
            }
        }
        Result res = new Result(model.get(GRB.DoubleAttr.ObjVal),builder.build(name), -1);
        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }

}
