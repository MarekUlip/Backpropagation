package DatasetLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarDataLoader {
    public static List<List<List<Double>>> loadData(String path) throws ParserConfigurationException, IOException, SAXException {
        if(path == null){
            path= "dataset.xml";
        }
        File file = new File(path);
//an instance of factory that gives a document builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//an instance of builder to parse the specified xml file
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList trainSetElements = doc.getElementsByTagName("trainSetElement");
        List<List<Double>> dataset = new ArrayList<>();
        List<List<Double>> targets = new ArrayList<>();
        for(int i = 0; i < trainSetElements.getLength(); i++){
            //Node node = trainSetElements.item(i);
            NodeList nodeElements = trainSetElements.item(i).getChildNodes();
            List<Double> trainValues = new ArrayList<>();
            List<Double> target = new ArrayList<>();
            for(int j = 0; j < nodeElements.getLength();j++){
                Node node = nodeElements.item(j);
                if(node.getNodeName().equals("inputs")){
                    Element element = (Element) node;
                    NodeList values = element.getElementsByTagName("value");
                    for(int k = 0; k < values.getLength(); k++){
                        trainValues.add(Double.valueOf(values.item(k).getTextContent()));
                    }
                    dataset.add(trainValues);
                }
                if(node.getNodeName().equals("outputs")){
                    Element element = (Element) node;
                    NodeList values = element.getElementsByTagName("value");
                    for(int k = 0; k < values.getLength(); k++){
                        target.add(Double.valueOf(values.item(k).getTextContent()));
                    }
                    targets.add(target);
                }
            }
        }
        List<List<List<Double>>> datas = new ArrayList<>();
        datas.add(dataset);
        datas.add(targets);
        return datas;
    }
}
