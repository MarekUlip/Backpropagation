package DatasetLoader;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@XmlRootElement
public class BackpropagationNeuronNet {

	private int layersCount;
	private int inputsCount;
	private ArrayList<InputDescription> inputDescriptions;
	private int[] neuronInLayersCount;
	private ArrayList<String> outputDescriptions;
	private double learningRate;
	private double lastStepInfluenceLearningRate;
	private ArrayList<TrainSetElement> trainingSet;
	private ArrayList<TestSetElement> testSet;
	private String name;
	private File fileName;
	public int getLayersCount() {
		return layersCount;
	}
	public void setLayersCount(int layersCount) {
		this.layersCount = layersCount;
	}
	public int getInputsCount() {
		return inputsCount;
	}
	public void setInputsCount(int inputsCount) {
		this.inputsCount = inputsCount;
	}
	@XmlElementWrapper(name="inputDescriptions")
	@XmlElement(name="inputDescription")
	public ArrayList<InputDescription> getInputDescriptions() {
		return inputDescriptions;
	}
	public void setInputDescriptions(ArrayList<InputDescription> inputDescriptions) {
		this.inputDescriptions = inputDescriptions;
	}
	@XmlElementWrapper(name="neuronInLayersCount")
	@XmlElement(name="neuronInLayerCount")
	public int[] getNeuronInLayersCount() {
		return neuronInLayersCount;
	}
	public void setNeuronInLayersCount(int[] neuronInLayersCount) {
		this.neuronInLayersCount = neuronInLayersCount;
	}
	@XmlElementWrapper(name="outputDescriptions")
	@XmlElement(name="outputDescription")
	public ArrayList<String> getOutputDescriptions() {
		return outputDescriptions;
	}
	public void setOutputDescriptions(ArrayList<String> outputDescription) {
		this.outputDescriptions = outputDescription;
	}
	public double getLearningRate() {
		return learningRate;
	}
	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}
	public double getLastStepInfluenceLearningRate() {
		return lastStepInfluenceLearningRate;
	}
	public void setLastStepInfluenceLearningRate(
			double lastStepInfluenceLearningRate) {
		this.lastStepInfluenceLearningRate = lastStepInfluenceLearningRate;
	}
	@XmlElementWrapper(name="trainSet")
	@XmlElement(name="trainSetElement")
	public ArrayList<TrainSetElement> getTrainingSet() {
		return trainingSet;
	}
	public void setTrainingSet(ArrayList<TrainSetElement> trainingSet) {
		this.trainingSet = trainingSet;
	}
	@XmlElementWrapper(name="testSet")
	@XmlElement(name="testSetElement")
	public ArrayList<TestSetElement> getTestSet() {
		return testSet;
	}
	public void setTestSet(ArrayList<TestSetElement> testSet) {
		this.testSet = testSet;
	}
	@XmlTransient
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlTransient
	public File getFileName() {
		return fileName;
	}
	public void setFileName(File fileName) {
		this.fileName = fileName;
	}
	public BackpropagationNeuronNet(int layersCount, int inputsCount,
                                    List<InputDescription> inputDescriptions, int[] neuronInLayersCount,
                                    List<String> outputDescription, double learningRate,
                                    double lastStepInfluenceLearningRate,
                                    List<TrainSetElement> trainingSet, List<TestSetElement> testSet) {
		super();
		this.layersCount = layersCount;
		this.inputsCount = inputsCount;
		this.inputDescriptions = new ArrayList<>(inputDescriptions);
		this.neuronInLayersCount = neuronInLayersCount;
		this.outputDescriptions = new ArrayList<>(outputDescription);
		this.learningRate = learningRate;
		this.lastStepInfluenceLearningRate = lastStepInfluenceLearningRate;
		if(trainingSet != null){
			this.trainingSet = new ArrayList<>(trainingSet);
		}
		if(testSet != null){
			this.testSet = new ArrayList<>(testSet);
		}
	}
	public BackpropagationNeuronNet() {
		this(3, 2, 
				Arrays.asList(
						new InputDescription("i1", 0, 1),
						new InputDescription("i2", 0, 1)
						),
				new int[]{5,3,1},
				Arrays.asList("output"),
				0.4,
				0.1,
				new ArrayList<TrainSetElement>(),
				new ArrayList<TestSetElement>()
		);
	}
	
	public static BackpropagationNeuronNet readFromXml(Reader input) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(BackpropagationNeuronNet.class);
		Unmarshaller m =  context.createUnmarshaller();
		return (BackpropagationNeuronNet)m.unmarshal(input);

	}

	public static BackpropagationNeuronNet readFromTxt(Reader input) throws IOException{
		StreamTokenizer streamTokenizer = new StreamTokenizer(input);
		streamTokenizer.commentChar('#');

		streamTokenizer.nextToken();
		int layers = (int) streamTokenizer.nval;

		streamTokenizer.nextToken();
		int inputsCount = (int) streamTokenizer.nval;

		ArrayList<InputDescription> inputDescriptions = new ArrayList<>();
//		String[] inputsNames = new String[inputsCount];
//		double[] inputsMin = new double[inputsCount];
//		double[] inputsMax = new double[inputsCount];
		for (int i = 0; i < inputsCount; i++) {
			streamTokenizer.nextToken();
			String inputsName = streamTokenizer.sval;
			streamTokenizer.nextToken();
			double inputMin = streamTokenizer.nval;
			streamTokenizer.nextToken();
			double inputMax = streamTokenizer.nval;
			inputDescriptions.add(new InputDescription(inputsName, inputMin, inputMax));
		}
		int[] neuronsCount = new int[layers];
		for (int i = 0; i < layers; i++) {
			streamTokenizer.nextToken();
			neuronsCount[i] = (int) streamTokenizer.nval;
		}
		String[] outputNames = new String[neuronsCount[neuronsCount.length - 1]];
		for (int i = 0; i < outputNames.length; i++) {
			streamTokenizer.nextToken();
			outputNames[i] = streamTokenizer.sval;
		}
		streamTokenizer.nextToken();
		double learningRate = streamTokenizer.nval;

		streamTokenizer.nextToken();
		double lastStepLearningRate = streamTokenizer.nval;

		streamTokenizer.nextToken();
		int numberOfElementsInTrainSet = (int) streamTokenizer.nval;
		ArrayList<TrainSetElement> trainSet = new ArrayList<>();
		for (int i = 0; i < numberOfElementsInTrainSet; i++) {
			double[] elementUserInputs = new double[inputsCount];
			double[] elementOutputs = new double[outputNames.length];
			for (int j = 0; j < inputsCount; j++) {
				streamTokenizer.nextToken();
				double value = streamTokenizer.nval;
				elementUserInputs[j] = value;
			}
			for (int k = 0; k < outputNames.length; k++) {
				streamTokenizer.nextToken();
				double value = streamTokenizer.nval;
				elementOutputs[k] = value;
			}
			trainSet.add(new TrainSetElement(elementUserInputs, elementOutputs));
		}

		streamTokenizer.nextToken();
		int numberOfElementsInTestSet = (int) streamTokenizer.nval;
		ArrayList<TestSetElement> testSet = new ArrayList<>();
		for (int i = 0; i < numberOfElementsInTrainSet; i++) {
			double[] elementUserInputs = new double[inputsCount];
			double[] elementOutputs = new double[outputNames.length];
			for (int j = 0; j < inputsCount; j++) {
				streamTokenizer.nextToken();
				double value = streamTokenizer.nval;
				elementUserInputs[j] = value;
			}
			testSet.add(new TestSetElement(elementUserInputs));
		}

		BackpropagationNeuronNet netConfig = new BackpropagationNeuronNet(
				layers, inputsCount, inputDescriptions, neuronsCount, 
				Arrays.asList(outputNames), learningRate, lastStepLearningRate, 
				trainSet, testSet);

		return netConfig;
	}
	
	public static BackpropagationNeuronNet load(){
		JFileChooser d = new JFileChooser();
		d.setDialogTitle("Load net config");
		d.setDialogType(JFileChooser.OPEN_DIALOG);
		d.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(".txt")) {
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "Neuron Net Description files";
			}
		});
		if (d.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (d.getSelectedFile().exists()) {
				FileReader fileReader = null;
				try {
					fileReader = new FileReader(d.getSelectedFile());
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				try {
					String netName = d.getSelectedFile().getName().substring(0, d.getSelectedFile().getName().lastIndexOf('.'));
					BackpropagationNeuronNet netConfig = readFromTxt(fileReader);
					netConfig.setName(netName);
					netConfig.setFileName(d.getSelectedFile());
					fileReader.close();
					return netConfig;
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} else{
				JOptionPane.showMessageDialog(null, "File " + d.getSelectedFile().getName() + "don't exists.", "Chyba", JOptionPane.OK_OPTION);
			}
		}
		return null;
	}
	
	public void storeToXML(File file) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(BackpropagationNeuronNet.class);
		Marshaller m =  context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		if(file == null){
			file = Paths.get(getFileName().getParentFile().getAbsolutePath(), getName()+".xml").toFile();
		}
		m.marshal(this, file);
		m.marshal(this, System.out);
	}

	/*public static void main(String[] args) {
		try {
			load().storeToXML(null);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
}
