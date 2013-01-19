package gui;

import com.google.common.collect.ListMultimap;
import database.Database;
import database.StatisticChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/18/13
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class VertexStatisticsDialog extends JDialog implements ActionListener {

    private JComboBox<String> statisticChoiceList;
    private JComboBox<String> nameList = new JComboBox<String>();
    private JComboBox<String> attemptList = new JComboBox<String>();

    private final JPanel mainPanel = new JPanel();

    private final JPanel buttonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");

    public VertexStatisticsDialog() {


        this.setTitle("Vertex Statistics Options");
        this.setResizable(false);
        this.setSize(400, 200);

        this.initializeMainPanel();


        this.initialiseButtonPanel();

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initializeMainPanel() {
        initializeStatisticChoiceBox();

        initializeUserComboBox();

        mainPanel.setLayout(new GridLayout(4, 2));
        mainPanel.add(new JLabel("User :"));
        mainPanel.add(nameList);
        mainPanel.add(new JLabel("Attempt No.:"));
        mainPanel.add(attemptList);
        mainPanel.add(new JLabel("Choose Statistic:"));
        mainPanel.add(statisticChoiceList);
    }

    private JComboBox<String> initializeStatisticChoiceBox() {
        String[] statisticChoices = {"Visit Frequency", "Visit Duration"};

        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        statisticChoiceList = new JComboBox(statisticChoices);
        statisticChoiceList.setSelectedIndex(0);
        statisticChoiceList.addActionListener(this);
        statisticChoiceList.setEditable(false);
        return statisticChoiceList;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == nameList) {
            initializeAttemptGroup((String) nameList.getSelectedItem());
            System.out.println((String) nameList.getSelectedItem());
            mainPanel.revalidate();
        }

        if (event.getSource() == bOk) {
            this.getStatistics((String) nameList.getSelectedItem(), (String) attemptList.getSelectedItem(), (String) statisticChoiceList.getSelectedItem());


            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }


    }

    private void getStatistics(String name, String attemptNumber, String statisticsChoice) {
        String dataName = name + attemptNumber;
        StatisticChoice choice;
        if (statisticsChoice.equals("Visit Frequency")) {
            choice = StatisticChoice.VERTEX_VISIT_FREQUENCY;
        } else if (statisticsChoice.equals("Visit Duration")) {
            choice = StatisticChoice.TIME_SPENT_PER_VERTEX;
        } else {
            choice = null;
            assert false;
        }


        display(((NetworkModel) NetworkModel.instance()).getVertexDataFor(dataName, choice));

    }

    private void display(HashMap<String, HashMap<String, Number>> data) {
        System.out.println("VertexName \t ExplorationPhase \t Task1 \t Task2 \t Task3 \t Total");
        for (String vertex : data.keySet()) {
            String name = vertex;
            Number exploration = data.get(vertex).get(Phase.EXPLORATION.toString()) == null ? 0 : data.get(vertex).get(Phase.EXPLORATION.toString());
            Number task1 = data.get(vertex).get(Phase.TASK_1.toString()) == null ? 0 : data.get(vertex).get(Phase.TASK_1.toString());
            Number task2 = data.get(vertex).get(Phase.TASK_2.toString()) == null ? 0 : data.get(vertex).get(Phase.TASK_2.toString());
            Number task3 = data.get(vertex).get(Phase.TASK_3.toString()) == null ? 0 : data.get(vertex).get(Phase.TASK_3.toString());


            long total = exploration.longValue() + task1.longValue() + task2.longValue() + task3.longValue();
            System.out.println(name + "\t" + exploration + "\t" + task1 + "\t" + task2 + "\t" + task3 + "\t" + total);


        }
    }


    private void initialiseButtonPanel() {
        bOk.setPreferredSize(new Dimension(100, 20));
        bCancel.setPreferredSize(new Dimension(100, 20));

        bOk.addActionListener(this);
        bCancel.addActionListener(this);

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(bOk);
        buttonPanel.add(bCancel);
    }

    public void initializeUserComboBox() {
        ListMultimap<String, Integer> dataNamesAndAttempts = Database.getInstance().getDataNameAndAttempts();

        nameList.removeAllItems();
        for (String name : dataNamesAndAttempts.keySet()) {
            nameList.addItem(name);


        }
        nameList = new JComboBox<String>(dataNamesAndAttempts.keySet().toArray(new String[1]));
        nameList.setSelectedIndex(0);

        nameList.addActionListener(this);
        nameList.setEditable(false);
        initializeAttemptGroup((String) nameList.getSelectedItem());
    }

    private void initializeAttemptGroup(String selectedItem) {
        ListMultimap<String, Integer> dataNamesAndAttempts = Database.getInstance().getDataNameAndAttempts();


        attemptList.removeAllItems();
        for (Integer i : dataNamesAndAttempts.get(selectedItem)) {
            attemptList.addItem(i.toString());


        }


        attemptList.setSelectedIndex(0);

        attemptList.addActionListener(this);
        attemptList.setEditable(false);

        attemptList.revalidate();
    }

}
