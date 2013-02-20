package stats.chartdisplays;

import gui.NetworkModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 2/1/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphDetailsChartDisplay extends ChartDisplay<HashMap<String, String>> {


    @Override
    public void display(HashMap<String, String> data) {


        JPanel panel = createPanel(data);

        createNewFrameAndSetLocation();

        currentFrame.setTitle(this.getTitle());
        currentFrame.setContentPane(panel);
        currentFrame.setVisible(true);
        currentFrame.setSize(new Dimension(650, 300));
    }

    private JPanel createPanel(HashMap<String, String> data) {
        JPanel mainPanel = new JPanel(
                new GridLayout(
                        ((data.keySet().size()+1) / 2),
                        2));
        for (String key : data.keySet()) {
            mainPanel.add(new JLabel(key));

            mainPanel.add(new JLabel(data.get(key)));
        }

        return mainPanel;

    }


}