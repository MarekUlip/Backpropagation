package Network;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private List<Layer> layers = new ArrayList<>();
    private int numOfLayers = 0;
    private double learningRate = 0.1;
    private List<List<List<Double>>> allOutputs = new ArrayList<>();
    private List<List<Double>> allInputs = new ArrayList<>();
    private List<List<Double>> allTargets = new ArrayList<>();

    private void doBackpropagation(int index){
        List<Double> targets = this.allTargets.get(index);
        List<List<Double>> layerOutputs = this.allOutputs.get(index);
        for(int i = this.numOfLayers-1; i >= 0; i--){
            Layer layer = this.layers.get(i);
            List<Double> errors = new ArrayList<>();
            if(i != this.numOfLayers-1){
                for(int j = 0; j < layer.getNumberOfNeurons(); j++){
                    double error = 0;
                    List<List<Double>> weights = layers.get(i+1).getWeights();
                    List<Double> deltas = layers.get(i+1).getDeltas();
                    for(int k = 0; k < weights.size(); k++){
                        error += weights.get(k).get(j)*deltas.get(k);
                    }
                    errors.add(error);
                }
            } else{
                for(int j = 0; j < layer.getNumberOfNeurons(); j++){
                    errors.add(targets.get(j) - layerOutputs.get(i).get(j)); //TODO careful
                }
            }
            for (int j = 0; j < layer.getNumberOfNeurons(); j++){
                layer.getDeltas().set(j, errors.get(j) * layer.activationDerivation(layerOutputs.get(i).get(j)));
            }
        }
    }

    private void updateWeights(int index, double learningRate){
        List<Double> input = this.allInputs.get(index);
        List<List<Double>> outputs = this.allOutputs.get(index);
        for(int i = 0; i < numOfLayers; i++){
            Layer layer = layers.get(i);
            if(i != 0){
                input = outputs.get(i-1);
            }
            for(int j = 0; j < layer.getNumberOfNeurons(); j++){
                List<Double> neuron = layer.getWeights().get(j);
                for(int k = 0; k < input.size(); k++){
                    neuron.set(k, neuron.get(k) + learningRate * layer.getDeltas().get(j) * input.get(k));
                }
                neuron.set(neuron.size()-1,neuron.get(neuron.size()-1) + learningRate * layer.getDeltas().get(j));

            }
        }
    }



    private double calculateOutputError(List<Double> predicted, List<Double> expected){
        if(predicted.size()!=expected.size()){
            System.out.println("Not matching output sizes.");
        }
        double error = 0;
        for(int i = 0; i<predicted.size(); i++){
            error += 0.5 * Math.pow(predicted.get(i)-expected.get(i),2);
        }
        return error;
    }

    private void clearLists(){
        this.allOutputs.clear();
        this.allInputs.clear();
        this.allTargets.clear();
    }

    public void train(List<List<Double>> x, List<List<Double>> y, int epochs, double learningRate){
        //x = convertToNetworkPoints(x,0,100);
        for(int j = 0;j < epochs;j++) {
            double sumError = 0;
            for (int i = 0; i < x.size(); i++) {
                List<Double> layerOutput;
                List<List<Double>> layerOutputs = new ArrayList<>();
                //layerOutputs.add(x.get(i));
                layerOutput = x.get(i);
                for (Layer layer : layers) {
                    layerOutput = layer.predict(layerOutput);
                    layerOutputs.add(layerOutput);
                }
                //System.out.println(layerOutput);
                //System.out.println(y.get(i));
                sumError += calculateOutputError(layerOutput,y.get(i));
                this.allOutputs.add(layerOutputs);
                this.allInputs.add(x.get(i));
                this.allTargets.add(y.get(i));
                /*if (i % batchSize == 0) {
                    doBackpropagation();
                    clearLists();
                }*/
                //doBackpropagation();
                doBackpropagation(0);
                updateWeights(0,learningRate);
                clearLists();
            }
            if(sumError < 0.01){
                System.out.println("Error is good enough breaking at error " + sumError +" at epoch "+j);
                break;
            }
            //System.out.println(sumError);
            /*doBackpropagation();
            clearLists();*/
        }
    }


    public List<List<Double>> predictMulitple(List<List<Double>> x){
        List<List<Double>> outputs = new ArrayList<>();
        for(List<Double> item:x){
            outputs.add(predict(item));
        }
        return outputs;
    }

    public List<Double> predict(List<Double> x){
        List<Double> output = x;
        for(Layer layer: layers){
            output = layer.predict(output);
        }
        return output;
    }

    public void addLayer(Layer layer){
        getLayers().add(layer);
        this.numOfLayers+=1;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
        this.numOfLayers = layers.size();
        //layerOutputs.clear();
        /*for(Layer layer: layers){
            layerOutputs.add(new ArrayList<>());
        }*/
    }
}
