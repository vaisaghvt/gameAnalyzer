package gui;

import modelcomponents.ModelFloor;
import modelcomponents.ModelStaircase;
import modelcomponents.ModelStaircaseGroup;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class ObjectPanelStaircaseGroup extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private class StaircaseModel implements TableModel {
        private final HashSet<TableModelListener> listener;

        private final ArrayList<ModelStaircase> staircases = new ArrayList<ModelStaircase>();
        private final HashMap<ModelStaircase, Integer> floorMapping = new HashMap<ModelStaircase, Integer>();
        private final HashMap<ModelStaircase, String> mapping1 = new HashMap<ModelStaircase, String>();
        private final Map<String, Set<ModelStaircase>> mapping2 = new HashMap<String, Set<ModelStaircase>>();

        public StaircaseModel(Document document) {
            HashMap<Integer, ModelStaircase> idMapping = new HashMap<Integer, ModelStaircase>();
            for (int i = 0; i < document.numberOfFloors(); ++i) {
                ModelFloor floor = document.getFloor(i);

                for (ModelStaircase staircase : floor.getStaircases()) {
                    staircases.add(staircase);
                    floorMapping.put(staircase, i);
                    idMapping.put(staircase.getId(), staircase);
                }
            }

            Map<String, ModelStaircaseGroup> groups = document.getStaircaseGroups();
            for (String groupName : groups.keySet()) {
                ModelStaircaseGroup group = groups.get(groupName);

                HashSet<ModelStaircase> set = new HashSet<ModelStaircase>();
                mapping2.put(groupName, set);

                for (int id : group.getStaircaseIds()) {
                    ModelStaircase staircase = idMapping.get(id);
                    mapping1.put(staircase, groupName);
                    set.add(staircase);
                }
            }
            listener = new HashSet<TableModelListener>();
        }

        public HashSet<String> groupNames() {
            return new HashSet<String>(mapping2.keySet());
        }

        @Override
        public void addTableModelListener(TableModelListener listener) {
            this.listener.add(listener);
        }

        @Override
        public void removeTableModelListener(TableModelListener listener) {
            this.listener.remove(listener);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 0:
                    return Integer.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
            }
            return null;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Id";
                case 1:
                    return "Floor";
                case 2:
                    return "Area";
                case 3:
                    return "Group";
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return staircases.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            ModelStaircase staircase = staircases.get(row);

            int mnx = (int) Math.min(staircase.getCorner0().getX(), staircase.getCorner1().getX());
            int mny = (int) Math.min(staircase.getCorner0().getY(), staircase.getCorner1().getY());
            int mxx = (int) Math.max(staircase.getCorner0().getX(), staircase.getCorner1().getX());
            int mxy = (int) Math.max(staircase.getCorner0().getY(), staircase.getCorner1().getY());


            switch (col) {
                case 0:
                    return staircase.getId();
                case 1:
                    return floorMapping.get(staircase);
                case 2:
                    return "(" + mnx + "," + mny + ")-(" + mxx + "," + mxy + ")";
                case 3:
                    return mapping1.containsKey(staircase) ? mapping1.get(staircase) : "(none)";
            }
            return null;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 3) {
                ModelStaircase staircase = staircases.get(row);
                String groupName = (String) value;

                String oldGroupName = mapping1.get(staircase);
                if (oldGroupName != null) {
                    mapping1.remove(staircase);

                    Set<ModelStaircase> group = mapping2.get(oldGroupName);
                    group.remove(staircase);

                    if (group.isEmpty()) {
                        mapping2.remove(oldGroupName);
                    }
                }

                if (!groupName.equals("(none)")) {
                    mapping1.put(staircase, groupName);

                    Set<ModelStaircase> group = mapping2.get(groupName);
                    if (group == null) {
                        group = new HashSet<ModelStaircase>();
                        mapping2.put(groupName, group);
                    }

                    group.add(staircase);
                }

                updateGroupNames();
            }
        }

        public boolean isCellEditable(int row, int col) {
            return col == 3;
        }
    }

    private Document document = null;

    private StaircaseModel model = null;
    private JTable table = null;
    private final JComboBox comboBox = new JComboBox();

    private final JPanel buttonPanel = new JPanel();
    private final JButton bOk = new JButton("Ok");
    private final JButton bCancel = new JButton("Cancel");


    public ObjectPanelStaircaseGroup(Document document) {
        this.document = document;

        this.setTitle("Staircase Group Settings");
        this.setResizable(false);
        this.setSize(400, 200);

        this.model = new StaircaseModel(document);
        this.table = new JTable(model);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(50);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(150);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(150);

        this.comboBox.setEditable(true);
        this.table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
        this.updateGroupNames();

        JScrollPane scrollPane = new JScrollPane(table);
        this.table.setFillsViewportHeight(true);

        this.initialiseButtonPanel();

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == bOk) {
            this.document.updateStaircaseGroups(model.mapping2);

            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }
    }

    private void updateGroupNames() {
        comboBox.removeAllItems();
        comboBox.addItem("(none)");

        for (String groupName : model.groupNames()) {
            comboBox.addItem(groupName);
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
}
