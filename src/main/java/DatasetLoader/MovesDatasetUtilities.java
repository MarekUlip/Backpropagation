package DatasetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovesDatasetUtilities {


    private static int minVal = 0;
    private static int maxVal = 800;

    public static List<List<List<Double>>> loadMoves(String fileName) throws IOException {
        if (fileName == null) {
            fileName = "moves.csv";
        }
        List<List<List<Double>>> dataset = new ArrayList<>();
        List<List<Double>> moves = new ArrayList<>();
        List<List<Double>> resultDest = new ArrayList<>();
        String row;
        BufferedReader csvReader = new BufferedReader(new FileReader(fileName));
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            List<Double> x = new ArrayList<>();
            List<Double> y = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                x.add(normalize(Double.parseDouble(data[i])));
            }
            for (int i = 4; i < 6; i++) {
                y.add(normalize(Double.parseDouble(data[i])));
            }
            moves.add(x);
            resultDest.add(y);
            // do something with the data
        }
        csvReader.close();
        dataset.add(moves);
        dataset.add(resultDest);
        return dataset;
    }

    private static double normalize(double value) {
        return (value - minVal) / (maxVal - minVal);
    }

    private static double denormalize(double value) {
        return value * (maxVal - minVal) + minVal;
    }

    public static void printListWithRealValues(List<Double> listToPrint) {
        List<Double> tmpList = denormalize(listToPrint);
        System.out.println(tmpList);
        tmpList.clear();
        tmpList = null;
    }

    public static List<Double> denormalize(List<Double> list){
        List<Double> tmpList = new ArrayList<>(); //so it would not affect provided list create a new one
        for (Double item : list) {
            tmpList.add(denormalize(item));
        }
        return tmpList;
    }

    public static List<Double> transferToInput(List<Double> prevInput, List<Double> output) {
        List<Double> nextInput = new ArrayList<>(prevInput.subList(2, 4));
        nextInput.addAll(output);
        return nextInput;
    }

    public static LineComponent getBasicLineComponent() {
        return new LineComponent(800, 800);

    }

    public static void showLinesGUI(LineComponent lineComponent) {
        Runnable r = new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, lineComponent);
                lineComponent.repaint();
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public static class LineComponent extends JComponent {

        ArrayList<Line2D.Double> lines;

        LineComponent(int width, int height) {
            super();
            setPreferredSize(new Dimension(width, height));
            lines = new ArrayList<Line2D.Double>();
        }

        public void addLine(List<Double> path) {
            Line2D.Double line = new Line2D.Double(
                    path.get(0),
                    path.get(1),
                    path.get(2),
                    path.get(3)
            );
            lines.add(line);
            //repaint();
        }

        public void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            Dimension d = getPreferredSize();
            g.setColor(Color.black);
            for (Line2D.Double line : lines) {
                g.drawLine(
                        (int) line.getX1(),
                        (int) line.getY1(),
                        (int) line.getX2(),
                        (int) line.getY2()
                );
            }
        }
    }
}
