package gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;
import database.Database;
import database.StatisticChoice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/18/13
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathStatisticsDialog extends JDialog implements ActionListener {

    private Collection<JCheckBox> dataNameList;


    private final JPanel mainPanel = new JPanel();
    private final JScrollPane pane;

    private final JPanel buttonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");
    private JComboBox statisticChoiceList;
    private JPanel topPanel = new JPanel();
    private JComboBox<Phase> phaseChoiceList;

    public PathStatisticsDialog() {


        this.dataNameList = new HashSet<JCheckBox>();
        this.setTitle("Vertex Statistics Options");

        this.setSize(800, 200);


        this.initialiseTopPanel();
        this.initializeMainPanel();


        this.initialiseButtonPanel();
        pane = new JScrollPane(mainPanel);
        pane.setVisible(true);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.NORTH);
        this.getContentPane().add(pane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.revalidate();

    }

    private void initialiseTopPanel() {
        initializeStatisticChoiceBox();
        initializePhaseChoiceBox();
        topPanel.setLayout(new GridLayout(2,2));
        topPanel.add(new JLabel("Choose Statistic:"));
        topPanel.add(statisticChoiceList);
        topPanel.add(new JLabel("Choose Phase:"));
        topPanel.add(phaseChoiceList);
    }

    private void initializePhaseChoiceBox() {


        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        phaseChoiceList = new JComboBox(Phase.values());
        phaseChoiceList.setSelectedIndex(0);
        phaseChoiceList.addActionListener(this);
        phaseChoiceList.setEditable(false);
    }

    private void initializeMainPanel() {
        initialiseDataNameList();
        mainPanel.setLayout(new GridLayout((!dataNameList.isEmpty() ? dataNameList.size() : 1), 3));
        for (JCheckBox box : dataNameList) {
            mainPanel.add(box);
        }
    }

    private void initializeStatisticChoiceBox() {
        String[] statisticChoices = {"Path Frequency"};

        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        statisticChoiceList = new JComboBox(statisticChoices);
        statisticChoiceList.setSelectedIndex(0);
        statisticChoiceList.addActionListener(this);
        statisticChoiceList.setEditable(false);

    }

    @Override
    public void actionPerformed(ActionEvent event) {


        if (event.getSource() == bOk) {
            this.getStatistics((Phase)phaseChoiceList.getSelectedItem(),(String) statisticChoiceList.getSelectedItem());


            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }


    }

    private void getStatistics(Phase phase, String statisticsChoice) {

        StatisticChoice choice;
        if (statisticsChoice.equals("Path Frequency")) {
            choice = StatisticChoice.PATH_FREQUENCY;
        } else {
            choice = null;
            assert false;
        }
        Collection<String> dataNames = new HashSet<String>();
        for (JCheckBox box : dataNameList) {
            if (box.isSelected()) {
                dataNames.add(box.getText());
            }
        }

        if (!dataNames.isEmpty()) {

            display(((NetworkModel) NetworkModel.instance()).getPathDataFor(dataNames,phase, choice));
        } else {
            System.out.println("No data to display!!");
        }

    }



    private void display(HashMultimap<String, String> data) {
        if(data==null){
            System.out.println("No Data!!");
            return;
        }
        System.out.println("Path \t Frequency \t Users");
        for (String path : data.keySet()) {


            System.out.println(path + "\t" + data.get(path).size() +"\t"+data.get(path) );


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

    public void initialiseDataNameList() {
        Collection<String> dataNames = Database.getInstance().getDataNames();

        this.dataNameList.clear();
        for (String name : dataNames) {
            this.dataNameList.add(new JCheckBox(name));


        }

    }



}
