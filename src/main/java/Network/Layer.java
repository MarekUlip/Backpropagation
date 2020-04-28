package Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Layer {
    private List<List<Double>> weights = null;
    private List<Double> deltas = null;
    private Random random = new Random();
    private int numberOfNeurons;
    private int inputSize;
    public Layer(int inputSize, int numberOfNeurons){
        this.inputSize = inputSize;
        this.setNumberOfNeurons(numberOfNeurons);
        initWeights();
    }

    private void initWeights(){
        this.numberOfNeurons = getNumberOfNeurons();
        this.setWeights(new ArrayList<>(numberOfNeurons));
        for(int i = 0; i < this.getNumberOfNeurons(); i++){
            this.getWeights().add(new ArrayList<>());
            for(int j = 0;j<this.inputSize+1;j++){
                this.getWeights().get(i).add(this.random.nextDouble());
            }
        }
    }

    public List<Double> predict(List<Double> x){
        List<Double> prediction = new ArrayList<>();
        double tmpRes;
        for (List<Double> weight : getWeights()) {
            tmpRes = weight.get(weight.size()-1);
            for (int j = 0; j < this.inputSize-1; j++) {
                tmpRes += x.get(j) * weight.get(j);
            }
            prediction.add(activation(tmpRes));
        }
        return prediction;
    }

    private double activation(double value){
        return 1 / (1 + Math.exp(-value));
    }

    public double activationDerivation(double value){
        return value * (1 - value);
    }

    public int getNumberOfNeurons() {
        return numberOfNeurons;
    }

    public void setNumberOfNeurons(int numberOfNeurons) {
        this.numberOfNeurons = numberOfNeurons;
        this.deltas = new ArrayList<>(Collections.nCopies(this.numberOfNeurons,0.0));
        initWeights();
    }

    public List<List<Double>> getWeights() {
        return weights;
    }

    public void setWeights(List<List<Double>> weights) {
        this.weights = weights;
    }

    public List<Double> getDeltas() {
        return deltas;
    }

    public void setDeltas(List<Double> deltas) {
        this.deltas = deltas;
    }
}
