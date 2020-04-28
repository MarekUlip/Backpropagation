import Client.DriverInterface;
import Client.RaceConnector;
import DatasetLoader.*;
import Network.Layer;
import Network.Model;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class Run {
    public static void main(String[] args) {
        /*List<String> names = new ArrayList<>();
        names.add("Marek");
        names.add("Steve");
        names.add("Jeffrey");
        names.add("Chose");*/
        try {
            //printDataset("dataset-no-obstacles-experimental.xml");
            //trainCar();
            //runServerRace(args);
            //testRun(args,2,false,"carModel.json","car-dataset.xml");
            learnMoves();
            //doctorTest();
            /*for(String name : names){
                Thread.sleep(500);
                new Thread(() -> {
                    try {
                        runRealServerRace(args,name,"carModel");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }*/
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void testRun(String[] args, int raceType, boolean train, String modelName, String datasetName) throws ParserConfigurationException, SAXException, IOException {
        if (train){
            trainCar(modelName,datasetName);
        }
        if(raceType == 0) {
            runServerRace(args, modelName);
        } else if(raceType == 1) {
            runRealServerRace(args,"Marek",modelName);
        } else{
            runDistantServerRace(args,"MarekUlip", modelName);
        }
    }

    public static void learnMoves() throws IOException {
        Model model = new Model();
        int layerSize = 12;
        Layer inputLayer = new Layer(4,layerSize);
        Layer hiddenLayer = new Layer(layerSize,layerSize);
        Layer outputLayer = new Layer(layerSize,2);
        model.addLayer(inputLayer);
        model.addLayer(hiddenLayer);
        model.addLayer(outputLayer);
        int seed = 5;
        List<List<List<Double>>> dataset = MovesDatasetUtilities.loadMoves(null);
        List<List<Double>> x = dataset.get(0);
        List<List<Double>> y = dataset.get(1);
        Collections.shuffle(x,new Random(seed));
        Collections.shuffle(y, new Random(seed));
        int bound = (int)(x.size()*0.9);
        List<List<Double>> xTrain = x.subList(0,bound);
        List<List<Double>> xTest = x.subList(bound,x.size());
        List<List<Double>> yTrain = y.subList(0,bound);
        List<List<Double>> yTest = y.subList(bound,x.size());
        model.train(xTrain,yTrain,2000,0.1);
        for(int i = 0; i < 20; i++){
            MovesDatasetUtilities.printListWithRealValues(model.predict(x.get(i)));
            MovesDatasetUtilities.printListWithRealValues(y.get(i));
        }
        double totalError = 0;
        for(int i = 0; i < yTest.size();i++){
            totalError += calculateOutputError(model.predict(xTest.get(0)),yTest.get(0));
        }
        System.out.println("Total error is " + totalError);
        System.out.println("**********************************************************");
        List<Double> testX = xTest.get(5);
        List<Double> output;
        MovesDatasetUtilities.LineComponent lineComponent = MovesDatasetUtilities.getBasicLineComponent();
        lineComponent.addLine(MovesDatasetUtilities.denormalize(testX));
        for(int i = 0; i<200; i++){
            output = model.predict(testX);
            MovesDatasetUtilities.printListWithRealValues(testX);
            MovesDatasetUtilities.printListWithRealValues(output);
            testX = MovesDatasetUtilities.transferToInput(testX,output);
            lineComponent.addLine(MovesDatasetUtilities.denormalize(testX));
        }
        MovesDatasetUtilities.showLinesGUI(lineComponent);
    }

    private static double calculateOutputError(List<Double> predicted, List<Double> expected){
        if(predicted.size()!=expected.size()){
            System.out.println("Not matching output sizes.");
        }
        double error = 0;
        for(int i = 0; i<predicted.size(); i++){
            error += 0.5 * Math.pow(predicted.get(i)-expected.get(i),2);
        }
        return error;
    }

    public static void trainCar(String modelName, String datasetName) throws IOException, SAXException, ParserConfigurationException {
        Model model = new Model();
        int layerSize = 6;
        Layer inputLayer = new Layer(28,layerSize);
        //Layer hiddenLayer = new Layer(3,6);
        Layer outputLayer = new Layer(layerSize,2);
        model.addLayer(inputLayer);
        //model.addLayer(hiddenLayer);
        model.addLayer(outputLayer);
        List<List<List<Double>>> dataset = CarDataLoader.loadData(datasetName);
        List<List<Double>> x = dataset.get(0);
        List<List<Double>> y = dataset.get(1);
        model.train(x,y,3000,0.1);

        System.out.println("Training done. Saving model.");
        saveModel(model,modelName);
    }

    public static void runRealServerRace(String[] args, String name, String modelName) throws IOException {
        Model model = loadModel(modelName);
        String host = "localhost";
        int port = 9460;
        String raceName = "Race";
        String driverName = name;
        String carType = null;
        RaceConnector raceConnector = null;
        if (args.length < 4) {
            // kontrola argumentu programu
            raceConnector = new RaceConnector(host, port, null);
            System.err.println("argumenty: server port nazev_zavodu jmeno_ridice [typ_auta]");
            //List<String> raceList =  raceConnector.listRaces();
            //raceName = raceList.get(new Random().nextInt(raceList.size()));
            //raceName = "zavod";
            List<String> carList =  raceConnector.listCars(raceName);
            System.out.println(carList);
            carType = "Fabia";
            //driverName += "MarekUlip";//"_" + carType;
//			host = JOptionPane.showInputDialog("Host:", host);
//			port = Integer.parseInt(JOptionPane.showInputDialog("Port:", Integer.toString(port)));
//			raceName = JOptionPane.showInputDialog("Race name:", raceName);
//			driverName = JOptionPane.showInputDialog("Driver name:", driverName);
        } else {
            // nacteni parametu
            host = args[0];
            port = Integer.parseInt(args[1]);
            raceName = args[2];
            driverName = args[3];
            if(args.length > 4){
                carType = args[4];
            }
            raceConnector = new RaceConnector(host, port, null);
        }
        // vytvoreni klienta
        raceConnector.setDriver(new DriverInterface() {

            @Override
            public HashMap<String, Float> drive(HashMap<String, Float> values) {
                HashMap<String, Float> responses = new HashMap<String, Float>();
                List<Double> input = convertToDoubleList(values);
                List<Double> output = model.predict(input);
                /*System.out.println(1-output.get(0));
                System.out.println(output.get(0));
                output.set(0,1-output.get(0));*/
                // pokud je v levo jede doprava, jinak do leva
                responses.put("wheel", output.get(0).floatValue());
                // maximalni zrychleni
                responses.put("acc", output.get(1).floatValue());
                /*float distance0 = values.get("distance0");

                // pokud je v levo jede doprava, jinak do leva
                if (distance0 < 0.5) {
                    responses.put("wheel", 0.8f);
                } else {
                    responses.put("wheel", 0.2f);
                }
                responses.put("acc", 1f);*/
                return responses;
            }
        });
        raceConnector.start(raceName, driverName, carType);
    }

    public static void runDistantServerRace(String[] args, String name, String modelName) throws IOException {
        Model model = loadModel(modelName);
        String host = "java.cs.vsb.cz";
        int port = 9460;
        String raceName = "r";
        String driverName = name;
        String carType = null;
        RaceConnector raceConnector = null;
        if (args.length < 4) {
            // kontrola argumentu programu
            raceConnector = new RaceConnector(host, port, null);
            System.err.println("argumenty: server port nazev_zavodu jmeno_ridice [typ_auta]");
            //List<String> raceList =  raceConnector.listRaces();
            //raceName = raceList.get(new Random().nextInt(raceList.size()));
            //raceName = "zavod";
            List<String> carList =  raceConnector.listCars(raceName);
            System.out.println(carList);
            carType = "Fabia";
            //driverName += "MarekUlip";//"_" + carType;
//			host = JOptionPane.showInputDialog("Host:", host);
//			port = Integer.parseInt(JOptionPane.showInputDialog("Port:", Integer.toString(port)));
//			raceName = JOptionPane.showInputDialog("Race name:", raceName);
//			driverName = JOptionPane.showInputDialog("Driver name:", driverName);
        } else {
            // nacteni parametu
            host = args[0];
            port = Integer.parseInt(args[1]);
            raceName = args[2];
            driverName = args[3];
            if(args.length > 4){
                carType = args[4];
            }
            raceConnector = new RaceConnector(host, port, null);
        }
        // vytvoreni klienta
        raceConnector.setDriver(new DriverInterface() {

            @Override
            public HashMap<String, Float> drive(HashMap<String, Float> values) {
                HashMap<String, Float> responses = new HashMap<String, Float>();
                List<Double> input = convertToDoubleList(values);
                List<Double> output = model.predict(input);
                /*System.out.println(1-output.get(0));
                System.out.println(output.get(0));
                output.set(0,1-output.get(0));*/
                // pokud je v levo jede doprava, jinak do leva
                responses.put("wheel", output.get(0).floatValue());
                // maximalni zrychleni
                responses.put("acc", output.get(1).floatValue());
                /*float distance0 = values.get("distance0");

                // pokud je v levo jede doprava, jinak do leva
                if (distance0 < 0.5) {
                    responses.put("wheel", 0.8f);
                } else {
                    responses.put("wheel", 0.2f);
                }
                responses.put("acc", 1f);*/
                return responses;
            }
        });
        raceConnector.start(raceName, driverName, carType);
    }

    public static void runServerRace(String[] args, String modelName) throws IOException {
        Model model = loadModel(modelName);
        String host = "localhost";
        int port = 9461;
        String raceName = "test";
        String driverName = "basic_client";
        String carType = null;
        RaceConnector raceConnector = null;
        if (args.length < 4) {
            // kontrola argumentu programu
            raceConnector = new RaceConnector(host, port, null);
            System.err.println("argumenty: server port nazev_zavodu jmeno_ridice [typ_auta]");
            //List<String> raceList =  raceConnector.listRaces();
            //raceName = raceList.get(new Random().nextInt(raceList.size()));
            //raceName = "zavod";
            List<String> carList =  raceConnector.listCars(raceName);
            System.out.println(carList);
            carType = "Fabia";
            driverName += "MarekUlip";//"_" + carType;
//			host = JOptionPane.showInputDialog("Host:", host);
//			port = Integer.parseInt(JOptionPane.showInputDialog("Port:", Integer.toString(port)));
//			raceName = JOptionPane.showInputDialog("Race name:", raceName);
//			driverName = JOptionPane.showInputDialog("Driver name:", driverName);
        } else {
            // nacteni parametu
            host = args[0];
            port = Integer.parseInt(args[1]);
            raceName = args[2];
            driverName = args[3];
            if(args.length > 4){
                carType = args[4];
            }
            raceConnector = new RaceConnector(host, port, null);
        }
        // vytvoreni klienta
        raceConnector.setDriver(new DriverInterface() {

            @Override
            public HashMap<String, Float> drive(HashMap<String, Float> values) {
                HashMap<String, Float> responses = new HashMap<String, Float>();
                List<Double> input = convertToDoubleList(values);
                List<Double> output = model.predict(input);
                responses.put("wheel", output.get(0).floatValue());
                responses.put("acc", output.get(1).floatValue());
                return responses;
            }
        });
        raceConnector.start(raceName, driverName, carType);
    }


    private static List<Double> convertToDoubleList(HashMap<String, Float> values){
        List<Double> inputs = new ArrayList<>();
        inputs.add(values.get("angle").doubleValue());
        inputs.add(values.get("speed").doubleValue());
        inputs.add(values.get("distance0").doubleValue());
        inputs.add(values.get("distance4").doubleValue());
        inputs.add(values.get("distance8").doubleValue());
        inputs.add(values.get("distance16").doubleValue());
        inputs.add(values.get("distance32").doubleValue());
        inputs.add(values.get("friction").doubleValue());
        inputs.add(values.get("skid").doubleValue());
        inputs.add(values.get("checkpoint").doubleValue());
        inputs.add(values.get("sensorFrontLeft").doubleValue());
        inputs.add(values.get("sensorFrontMiddleLeft").doubleValue());
        inputs.add(values.get("sensorFrontMiddleRight").doubleValue());
        inputs.add(values.get("sensorFrontRight").doubleValue());
        inputs.add(values.get("sensorFrontRightCorner1").doubleValue());
        inputs.add(values.get("sensorFrontRightCorner2").doubleValue());
        inputs.add(values.get("sensorRight1").doubleValue());
        inputs.add(values.get("sensorRight2").doubleValue());
        inputs.add(values.get("sensorRearRightCorner2").doubleValue());
        inputs.add(values.get("sensorRearRightCorner1").doubleValue());
        inputs.add(values.get("sensorRearRight").doubleValue());
        inputs.add(values.get("sensorRearLeft").doubleValue());
        inputs.add(values.get("sensorRearLeftCorner1").doubleValue());
        inputs.add(values.get("sensorRearLeftCorner2").doubleValue());
        inputs.add(values.get("sensorLeft1").doubleValue());
        inputs.add(values.get("sensorLeft2").doubleValue());
        inputs.add(values.get("sensorFrontLeftCorner1").doubleValue());
        inputs.add(values.get("sensorFrontLeftCorner2").doubleValue());
        return inputs;
    }

    public static void doctorTest(){
        Model model = new Model();
        Layer inputLayer = new Layer(5,4);
        Layer outputLayer = new Layer(4,3);
        model.addLayer(inputLayer);
        model.addLayer(outputLayer);
        BackpropagationNeuronNet net = loadNet();
        List<List<Double>> x = getTrainData(net.getTrainingSet());
        List<List<Double>> y = getTargets(net.getTrainingSet());
        List<List<Double>> test = getTestData(net.getTestSet());
        List<List<Double>> minMaxes = new ArrayList<>();
        for(InputDescription item: net.getInputDescriptions()){
            List<Double> minMax = new ArrayList<>();
            minMax.add(item.getMinimum());
            minMax.add(item.getMaximum());
            minMaxes.add(minMax);
        }
        normalizeAll(x,minMaxes);
        normalizeAll(test,minMaxes);
        model.train(x,y,5000,0.1);
        for(int i = 0; i< x.size(); i++){
            System.out.println(model.predict(x.get(i)));
            System.out.println(y.get(i));
        }
        for(List<Double> item:test){
            System.out.println(model.predict(item));
        }
    }

    private static void normalizeAll(List<List<Double>> values, List<List<Double>> minMaxes){
        for(List<Double> item : values){
            for(int i = 0; i < item.size(); i++){
                item.set(i,normalize(item.get(i),minMaxes.get(i).get(0),minMaxes.get(i).get(1)));
            }
        }
    }

    private static double normalize(double value, double minVal, double maxVal){
        return (value - minVal) / (maxVal - minVal);
    }

    public static void saveModel(Model model, String modelName){
        if(modelName == null){
            modelName = "carNeuralNetwork.json";
        }
        Gson gson = new Gson();
        try {
            Writer writer = new FileWriter(modelName);
            gson.toJson(model, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Model loadModel(String modelPath){
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(modelPath),Model.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static BackpropagationNeuronNet loadNet(){
        BackpropagationNeuronNet net = null;
        try {
            Reader reader = new FileReader("lekar.xml");
            /*int data = reader.read();
            while (data != -1) {
                System.out.print((char) data);
                data = reader.read();
            }*/
            net = BackpropagationNeuronNet.readFromXml(reader);
            reader.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return net;
    }

    public static List<List<Double>> getTrainData(List<TrainSetElement> elements){
        List<List<Double>> train = new ArrayList<>();
        for(TestSetElement element : elements){
            List<Double> item = new ArrayList<>();
            for(double num: element.getInputs()){
                item.add(num);
            }
            train.add(item);
        }
        return train;
    }

    public static List<List<Double>> getTestData(List<TestSetElement> elements){
        List<List<Double>> train = new ArrayList<>();
        for(TestSetElement element : elements){
            List<Double> item = new ArrayList<>();
            for(double num: element.getInputs()){
                item.add(num);
            }
            train.add(item);
        }
        return train;
    }

    public static List<List<Double>> getTargets(List<TrainSetElement> elements){
        List<List<Double>> targets = new ArrayList<>();
        for(TrainSetElement element : elements){
            List<Double> item = new ArrayList<>();
            for(double num: element.getOutput()){
                item.add(num);
            }
            targets.add(item);
        }
        return targets;
    }
}
