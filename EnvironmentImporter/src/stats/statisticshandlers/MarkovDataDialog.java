package stats.statisticshandlers;

import randomwalk.RandomWalkOrganizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 4/12/13
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarkovDataDialog extends JFrame implements ChangeListener, ActionListener {


    public enum HeatMapType {
        INDIVIDUAL, COMPARISON
    }

    public enum GraphType {
        HUMAN_DATA, RANDOM_WALK
    }

    public enum HumanType {
        DIRECT, N_FROM_M
    }


    JButton generateHeatMapButton = new JButton("Generate Heat Map");
    JButton calculateCoverageForHopsButton = new JButton("Calculate Coverage");
    JButton calculateHopsForCoverage = new JButton("Calculate Hops");
    private JComboBox<HeatMapType> heatMapTypeJComboBox = new JComboBox<HeatMapType>(HeatMapType.values());
    private JPanel middlePanel = new JPanel();
    private JSlider orderSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 16, 1);
    private JLabel orderLabel = new JLabel("1");

    private JSlider hopsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 350, 300);
    private JLabel hopsLabel = new JLabel("300");

    private JSlider coverageSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 100, 60);
    private JLabel coverageLabel = new JLabel("60% ");


    private HeatMapType heatMapType;
    private JComboBox<GraphType> graph1TypeComboBox = new JComboBox<GraphType>(GraphType.values());
    private JComboBox<GraphType> graph2TypeComboBox = new JComboBox<GraphType>(GraphType.values());
    private JComboBox<HumanType> graph1HumanTypeComboBox = new JComboBox<HumanType>(HumanType.values());
    private JComboBox<HumanType> graph2HumanTypeComboBox = new JComboBox<HumanType>(HumanType.values());
    private JComboBox<RandomWalkOrganizer.RandomWalkType> graph1RandomWalkTypeComboBox = new JComboBox<RandomWalkOrganizer.RandomWalkType>(RandomWalkOrganizer.RandomWalkType.values());
    private JComboBox<RandomWalkOrganizer.RandomWalkType> graph2RandomWalkTypeComboBox = new JComboBox<RandomWalkOrganizer.RandomWalkType>(RandomWalkOrganizer.RandomWalkType.values());
    private JSlider graph1mSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 16, 1);
    private JSlider graph2mSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 16, 1);
    private JLabel graph1mValueLabel = new JLabel();
    private JLabel graph2mValueLabel = new JLabel();

    public MarkovDataDialog(final MarkovDataStatisticHandler markovDataStatisticHandler) {
        graph1TypeComboBox.addActionListener(this);
        graph1HumanTypeComboBox.addActionListener(this);
        graph1RandomWalkTypeComboBox.addActionListener(this);
        graph1mSlider.addChangeListener(this);

        graph2TypeComboBox.addActionListener(this);
        graph2HumanTypeComboBox.addActionListener(this);
        graph2RandomWalkTypeComboBox.addActionListener(this);
        graph2mSlider.addChangeListener(this);


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLayout(new BorderLayout());
                JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
                JPanel detailsPanel = new JPanel(new GridLayout(4, 2));

                detailsPanel.add(new JLabel("Choose Order"));
                JPanel orderSliderPanel = new JPanel(new BorderLayout());
                orderSliderPanel.add(orderSlider, BorderLayout.CENTER);
                orderSliderPanel.add(orderLabel, BorderLayout.WEST);
                detailsPanel.add(orderSliderPanel);


                detailsPanel.add(new JLabel("Type"));
                detailsPanel.add(heatMapTypeJComboBox); // Alone or comparison?

                detailsPanel.add(new JLabel("Hops required"));
                JPanel hopsSliderPanel = new JPanel(new BorderLayout());
                hopsSliderPanel.add(hopsSlider, BorderLayout.CENTER);
                hopsSliderPanel.add(hopsLabel, BorderLayout.WEST);
                detailsPanel.add(hopsSliderPanel);

                detailsPanel.add(new JLabel("Coverage required"));
                JPanel coverageSliderPanel = new JPanel(new BorderLayout());
                coverageSliderPanel.add(coverageSlider, BorderLayout.CENTER);
                coverageSliderPanel.add(coverageLabel, BorderLayout.WEST);
                detailsPanel.add(coverageSliderPanel);


                heatMapTypeJComboBox.setSelectedIndex(0);
                heatMapType = heatMapTypeJComboBox.getItemAt(0);


                orderSlider.addChangeListener(MarkovDataDialog.this);
                coverageSlider.addChangeListener(MarkovDataDialog.this);
                hopsSlider.addChangeListener(MarkovDataDialog.this);
                heatMapTypeJComboBox.addActionListener(MarkovDataDialog.this);
                generateHeatMapButton.addActionListener(markovDataStatisticHandler);

                calculateCoverageForHopsButton.addActionListener(markovDataStatisticHandler);
                calculateHopsForCoverage.addActionListener(markovDataStatisticHandler);


                buttonPanel.add(generateHeatMapButton);
                buttonPanel.add(calculateCoverageForHopsButton);
                buttonPanel.add(calculateHopsForCoverage);


                add(detailsPanel, BorderLayout.NORTH);
                add(buttonPanel, BorderLayout.SOUTH);


                generateMiddlePanel();
                JPanel actualMiddlePanel = new JPanel(new BorderLayout());
                actualMiddlePanel.add(middlePanel, BorderLayout.NORTH);
                add(actualMiddlePanel, BorderLayout.CENTER);

                setLocation(200, 150);
                setSize(650, 400);
                setVisible(true);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setTitle("Heat map parameters");

            }
        });

    }


    private void generateMiddlePanel() {
        final HeatMapType localHeatMapTypeReference = this.heatMapType;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                middlePanel.removeAll();

                switch (localHeatMapTypeReference) {

                    case COMPARISON:

                        middlePanel.setLayout(new GridLayout(4, 4));
                        middlePanel.add(new JLabel("Graph 1 type")); //Human data, random walk
                        middlePanel.add(graph1TypeComboBox);
                        graph2TypeComboBox.setSelectedIndex(0);

                        middlePanel.add(new JLabel("Graph 2 type")); //Human data, random walk
                        middlePanel.add(graph2TypeComboBox);
                        graph2TypeComboBox.setSelectedIndex(1);

                        middlePanel.add(new JLabel("Human data type")); //human data type;
                        middlePanel.add(graph1HumanTypeComboBox);
                        graph1HumanTypeComboBox.setSelectedIndex(0);
                        graph1HumanTypeComboBox.setEnabled(true);

                        middlePanel.add(new JLabel("Human data type")); //human data type;
                        middlePanel.add(graph2HumanTypeComboBox);
                        graph2HumanTypeComboBox.setEnabled(false);

                        middlePanel.add(new JLabel("m value"));
                        JPanel graph1mSliderPanel = new JPanel(new BorderLayout());
                        graph1mSliderPanel.add(graph1mSlider, BorderLayout.CENTER);
                        graph1mSliderPanel.add(graph1mValueLabel, BorderLayout.WEST);
                        middlePanel.add(graph1mSliderPanel);
                        graph1mSlider.setEnabled(false);


                        middlePanel.add(new JLabel("m value"));
                        JPanel graph2mSliderPanel = new JPanel(new BorderLayout());
                        graph2mSliderPanel.add(graph2mSlider, BorderLayout.CENTER);
                        graph2mSliderPanel.add(graph2mValueLabel, BorderLayout.WEST);
                        middlePanel.add(graph2mSliderPanel);
                        graph2mSlider.setEnabled(false);

                        middlePanel.add(new JLabel("random walk type"));
                        middlePanel.add(graph1RandomWalkTypeComboBox);
                        graph1RandomWalkTypeComboBox.setEnabled(false);

                        middlePanel.add(new JLabel("random walk type"));
                        middlePanel.add(graph2RandomWalkTypeComboBox);
                        graph2RandomWalkTypeComboBox.setEnabled(true);


                        break;
                    case INDIVIDUAL:
                        middlePanel.setLayout(new GridLayout(4, 2));
                        middlePanel.add(new JLabel("Graph type")); //Human data, random walk
                        middlePanel.add(graph1TypeComboBox);
                        graph2TypeComboBox.setSelectedIndex(0);// default human

                        middlePanel.add(new JLabel("Human data type")); //human data type;
                        middlePanel.add(graph1HumanTypeComboBox);
                        graph1HumanTypeComboBox.setSelectedIndex(0);
                        graph1HumanTypeComboBox.setEnabled(true);


                        middlePanel.add(new JLabel("m value"));
                        JPanel graphmSliderPanel = new JPanel(new BorderLayout());
                        graphmSliderPanel.add(graph1mSlider, BorderLayout.CENTER);
                        graphmSliderPanel.add(graph1mValueLabel, BorderLayout.WEST);
                        middlePanel.add(graphmSliderPanel);
                        graph1mSlider.setEnabled(false);

                        middlePanel.add(new JLabel("random walk type"));
                        middlePanel.add(graph1RandomWalkTypeComboBox);
                        graph1RandomWalkTypeComboBox.setEnabled(false);

                        break;
                }
                if (getOrder() > 1) {
                    if (graph1mSlider.isEnabled()) {
                        graph1mSlider.setValue(1);
                        graph1mValueLabel.setText("1");
                        graph1mSlider.setEnabled(true);
                        graph1mSlider.setMaximum(getOrder() - 1);
                    } else if (graph2mSlider.isEnabled()) {
                        graph2mSlider.setValue(1);
                        graph1mValueLabel.setText("2");
                        graph2mSlider.setEnabled(true);
                        graph2mSlider.setMaximum(getOrder() - 1);
                    }
                } else {
                    graph1mSlider.setEnabled(false);
                    graph1mValueLabel.setText("");
                    graph1HumanTypeComboBox.setEnabled(false);
                    graph2mSlider.setEnabled(false);
                    graph2mValueLabel.setText("");
                    graph2HumanTypeComboBox.setEnabled(false);
                }


                revalidate();

            }
        });

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == orderSlider) {
            orderLabel.setText(String.valueOf(orderSlider.getValue()));
            if (!orderSlider.getValueIsAdjusting()) {
//                if (getOrder() != orderSlider.getValue()) {


                    generateMiddlePanel();
//                }
            }

        } else if (e.getSource() == graph1mSlider) {
            if (!graph1mSlider.getValueIsAdjusting()) {
                if (graph1mSlider.getValue() >= getOrder()) {
                    graph1mSlider.setValue(getOrder() - 1);
                }

            }
            graph1mValueLabel.setText(String.valueOf(graph1mSlider.getValue()));
        } else if (e.getSource() == graph2mSlider) {
            if (!graph2mSlider.getValueIsAdjusting()) {
                if (graph2mSlider.getValue() >= getOrder()) {
                    graph2mSlider.setValue(getOrder() - 1);
                }

            }
            graph2mValueLabel.setText(String.valueOf(graph2mSlider.getValue()));
        } else if (e.getSource() == hopsSlider) {
            if (!hopsSlider.getValueIsAdjusting()) {
                if (getOrder() > hopsSlider.getValue()) {
                    hopsSlider.setValue(getOrder() + 1);


//                    generateMiddlePanel();
                }
            }
            hopsLabel.setText(String.valueOf(hopsSlider.getValue()));
        } else if (e.getSource() == coverageSlider) {

            coverageLabel.setText(String.valueOf(coverageSlider.getValue()) + "% ");

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == heatMapTypeJComboBox) {
            heatMapType = heatMapTypeJComboBox.getItemAt(heatMapTypeJComboBox.getSelectedIndex());
            generateMiddlePanel();
            if (heatMapType == HeatMapType.INDIVIDUAL) {
                calculateCoverageForHopsButton.setEnabled(true);
                calculateHopsForCoverage.setEnabled(true);
                hopsSlider.setEnabled(true);
                coverageSlider.setEnabled(true);
            } else {
                calculateCoverageForHopsButton.setEnabled(false);
                calculateHopsForCoverage.setEnabled(false);
                hopsSlider.setEnabled(false);
                coverageSlider.setEnabled(false);
            }
        } else if (e.getSource() == graph1HumanTypeComboBox) {
            HumanType type = graph1HumanTypeComboBox.getItemAt(graph1HumanTypeComboBox.getSelectedIndex());
            switch (type) {
                case N_FROM_M:
                    if (getOrder() > 1) {
                        graph1mSlider.setValue(1);
                        graph1mSlider.setEnabled(true);
                        graph1mValueLabel.setText("1");
                    }else{
                        graph1mValueLabel.setText("");
                    }
                    break;
                case DIRECT:
                    graph1mSlider.setEnabled(false);
                    graph1mValueLabel.setText("");
                    break;
            }
        } else if (e.getSource() == graph1TypeComboBox) {
            GraphType type = graph1TypeComboBox.getItemAt(graph1TypeComboBox.getSelectedIndex());
            switch (type) {

                case HUMAN_DATA:
                    graph1HumanTypeComboBox.setEnabled(true);
                    graph1RandomWalkTypeComboBox.setEnabled(false);
                    break;
                case RANDOM_WALK:
                    graph1HumanTypeComboBox.setEnabled(false);
                    graph1mSlider.setEnabled(false);
                    graph1RandomWalkTypeComboBox.setEnabled(true);

                    break;
            }
        } else if (e.getSource() == graph2HumanTypeComboBox) {
            HumanType type = graph2HumanTypeComboBox.getItemAt(graph2HumanTypeComboBox.getSelectedIndex());
            switch (type) {
                case N_FROM_M:
                    if (getOrder() > 1) {
                        graph2mSlider.setValue(1);
                        graph2mValueLabel.setText("1");
                        graph2mSlider.setEnabled(true);
                    }
                    graph2mValueLabel.setText("");
                    break;
                case DIRECT:
                    graph2mSlider.setEnabled(false);
                    graph2mValueLabel.setText("");
                    break;
            }
        } else if (e.getSource() == graph2TypeComboBox) {
            GraphType type = graph2TypeComboBox.getItemAt(graph2TypeComboBox.getSelectedIndex());
            switch (type) {

                case HUMAN_DATA:
                    graph2HumanTypeComboBox.setEnabled(true);
                    graph2RandomWalkTypeComboBox.setEnabled(false);
                    break;
                case RANDOM_WALK:
                    graph2HumanTypeComboBox.setEnabled(false);
                    graph2mSlider.setEnabled(false);
                    graph2RandomWalkTypeComboBox.setEnabled(true);

                    break;
            }
        }
    }

    public HeatMapType getHeatMapType() {
        return heatMapType;
    }

    public GraphType getGraph1Type() {
        return graph1TypeComboBox.getItemAt(graph1TypeComboBox.getSelectedIndex());
    }

    public GraphType getGraph2Type() {
        return graph2TypeComboBox.getItemAt(graph2TypeComboBox.getSelectedIndex());
    }


    public HumanType getGraph1HumanType() {
        return graph1HumanTypeComboBox.getItemAt(graph1HumanTypeComboBox.getSelectedIndex());
    }

    public HumanType getGraph2HumanType() {
        return graph2HumanTypeComboBox.getItemAt(graph2HumanTypeComboBox.getSelectedIndex());
    }

    public RandomWalkOrganizer.RandomWalkType getGraph1RandomWalkType() {
        return graph1RandomWalkTypeComboBox.getItemAt(graph1RandomWalkTypeComboBox.getSelectedIndex());
    }

    public RandomWalkOrganizer.RandomWalkType getGraph2RandomWalkType() {
        return graph2RandomWalkTypeComboBox.getItemAt(graph2RandomWalkTypeComboBox.getSelectedIndex());
    }

    public int getGraph1m() {
        return graph1mSlider.getValue();
    }

    public int getGraph2m() {
        return graph2mSlider.getValue();
    }

    public int getOrder() {
        return Integer.parseInt(orderLabel.getText());
    }

    public int getCoverageRequired() {
        return coverageSlider.getValue();
    }


    public int getHopsRequired() {
        return hopsSlider.getValue();
    }


}
