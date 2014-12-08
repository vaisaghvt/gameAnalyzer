package database;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gui.Phase;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 4:10 PM
 * <p/>
 * This calss reads from the mysql database and initiates the document.
 */
public class Database {

    private static Database _singletonDB = null;
//    private static final String DATABASE_ADDRESS = "minecraftserverdb.cgxlckabowed.us-east-1.rds.amazonaws.com";
    private static final String DATABASE_ADDRESS = "localhost";
    private static final String DATABASE_PORT = "3306";
    private static final String DATABASE_NAME = "mc_statistician";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String[] queries =
            {"SELECT x, z " +
                    "FROM mc_statistician.playerLocationForAnalysis " +
                    "where uuid = (select uuid from  mc_statistician.IdPlayerMapping where name = \"newgroundfloor\") and minTime = (select startTime from  mc_statistician.IdPlayerMapping where name = \"newgroundfloor\")  and y <= 24 ",

                    "SELECT x, z " +
                            "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerLocationForAnalysis as plfa " +
                            "where name = \"newsecondfloor2\" " +
                            "and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime " +
                            "and y > 24 and y <= 33  "
                            ,
                    "SELECT x, z " +
                            "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerLocationForAnalysis as plfa " +
                            "where name = \"newthirdfloor\" " +
                            "and y > 33 " +
                            "and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime"};


    private Connection connection = null;
    private ArrayList<ArrayList<Point>> pointsForFloor;
    private ArrayList<Integer> floorWidths = new ArrayList<Integer>();
    private ArrayList<Integer> floorHeights = new ArrayList<Integer>();
    private ArrayList<Integer> floorMinXs = new ArrayList<Integer>();
    private ArrayList<Integer> floorMinYs = new ArrayList<Integer>();
    private Collection<String> dataNames;
    private ListMultimap<String, Integer> dataNameAttemptMap;


    public Database() throws ClassNotFoundException, DBConnectFail {
        if (Database._singletonDB != null) return;

        pointsForFloor = new ArrayList<ArrayList<Point>>();

        // Connect To DB And Hold info
        Class.forName("com.mysql.jdbc.Driver");
        this.ConnectToDB();
        this.initializeData();

        Database._singletonDB = this;
    }

    public static Database getInstance() {
        if (_singletonDB == null) {
            try {
                _singletonDB = new Database();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (DBConnectFail dbConnectFail) {
                dbConnectFail.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return _singletonDB;
    }

    private void initializeData() {
        for (String query : queries) {
            System.out.println(query);
            List<Map<String, String>> results = executeSynchQuery(query);
            ArrayList<Point> listOfPoints = new ArrayList<Point>();
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            System.out.println(results.size());
            for (Map<String, String> row : results) {
                int x = Integer.parseInt(row.get("x"));
                int y = Integer.parseInt(row.get("z"));
                minX = Math.min(x, minX);
                minY = Math.min(y, minY);
                maxX = Math.max(x, maxX);
                maxY = Math.max(y, maxY);
                listOfPoints.add(new Point(x, y));

            }
            pointsForFloor.add(listOfPoints);
            int width = maxX - minX;
            int height = maxY - minY;
            this.floorWidths.add(width);
            this.floorHeights.add(height);
            this.floorMinXs.add(minX);
            this.floorMinYs.add(minY);
        }
    }

    public List<HashMap<String, Number>> getMovementOfPlayer(String dataName, Collection<Phase> selectedPhases) {


        String exploreCompleteTime = getPhaseCompleteTime(Phase.EXPLORATION, dataName);
        String task1CompleteTime = getPhaseCompleteTime(Phase.TASK_1, dataName);
        String task2CompleteTime = getPhaseCompleteTime(Phase.TASK_2, dataName);
        String task3CompleteTime = getPhaseCompleteTime(Phase.TASK_3, dataName);

        StringBuffer whereQuery = new StringBuffer();

        boolean first = true;
        whereQuery.append(" and (");
        if (!selectedPhases.isEmpty()) {

            if (selectedPhases.contains(Phase.EXPLORATION)) {
                whereQuery.append(" (time<=");
                whereQuery.append(exploreCompleteTime);
                whereQuery.append(")");
                first = false;
            }
            if (selectedPhases.contains(Phase.TASK_1)) {
                if (!first) {
                    whereQuery.append(" or");
                }
                whereQuery.append(" (time>").append(exploreCompleteTime).append(" and time<=").append(task1CompleteTime).append(")");
                first = false;
            }
            if (selectedPhases.contains(Phase.TASK_2)) {
                if (!first) {
                    whereQuery.append(" or");
                }
                whereQuery.append(" (time>").append(task1CompleteTime).append(" and time<=").append(task2CompleteTime).append(")");
                first = false;
            }
            if (selectedPhases.contains(Phase.TASK_3)) {
                if (!first) {
                    whereQuery.append(" or");
                }
                whereQuery.append(" (time>").append(task2CompleteTime).append(" and time<=").append(task3CompleteTime).append(")");
                first = false;
            }
            whereQuery.append(")");
        }


        String positionQuery = "SELECT time, x, y, z " +
                "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerLocationForAnalysis as plfa " +
                "where name = \"" + dataName + "\" and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime" + whereQuery + ";";
        List<Map<String, String>> results = executeSynchQuery(positionQuery);

//        System.out.println(positionQuery);
        List<HashMap<String, Number>> listOfResults = new ArrayList<HashMap<String, Number>>();
        if (results != null) {
            for (Map<String, String> row : results) {
                Integer x = Integer.parseInt(row.get("x"));
                Integer y = Integer.parseInt(row.get("z"));
                Integer height = Integer.parseInt(row.get("y"));
                Integer floor = getFloorForHeight(height);
                Long time = Long.parseLong(row.get("time"));
                HashMap<String, Number> result = new HashMap<String, Number>();
                result.put("x", x);
                result.put("y", y);
                result.put("floor", floor);
                result.put("time", time);

                listOfResults.add(result);

            }
        }
        return listOfResults;

    }

    public String getPhaseStartTime(Phase phase, String dataName) {
        switch (phase) {

            case TASK_1:
                return getLeverOpenTime(dataName, 12);

            case TASK_2:
                return getLeverOpenTime(dataName, 13);

            case TASK_3:
                return getLeverOpenTime(dataName, 14);

            case EXPLORATION:
                return "0";

        }
        return null;
    }

    public String getPhaseCompleteTime(Phase phase, String dataName) {
        switch (phase) {

            case TASK_1:
                return getLeverOpenTime(dataName, 13);

            case TASK_2:
                return getLeverOpenTime(dataName, 14);

            case TASK_3:
                return getLeverOpenTime(dataName, 15);

            case EXPLORATION:
                return getLeverOpenTime(dataName, 12);

        }
        return null;
    }


    private String getLeverOpenTime(String dataName, int i) {
        String st;
        if (i > 12) {
            st = "select lo.leverID, time-st.startTime as t " +
                    "from mc_statistician.LeverOpenTime as lo, (select startTime from mc_statistician.IdPlayerMapping where name=\"" + dataName + "\") as st " +
                    "where name=\"" + dataName + "\" and leverID =" + i + ";";
        } else {
            st = "select lo.leverID, time-st.startTime as t " +
                    "from mc_statistician.LeverOpenTime as lo, " +
                    "(select startTime from mc_statistician.IdPlayerMapping " +
                    "where name=\"" + dataName + "\") as st " +
                    "where name=\"" + dataName + "\" and leverID NOT IN(13,14,15) ORDER BY t desc;";
        }
        List<Map<String, String>> results = executeSynchQuery(st);
//        assert results.size() == 1;

        return results.get(0).get("t");
    }


    private int getFloorForHeight(int height) {
        if (height > 33)
            return 2;
        else if (height <= 33 && height > 24)
            return 1;
        else
            return 0;
    }

    private void ConnectToDB() throws DBConnectFail {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + DATABASE_ADDRESS + ":" + DATABASE_PORT +
                    "/" + DATABASE_NAME, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new DBConnectFail(e);
        }
    }

    public List<Map<String, String>> executeSynchQuery(String sql) {
        List<Map<String, String>> ColData = new ArrayList<Map<String, String>>();

        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = this.connection.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                HashMap<String, String> rowToAdd = new HashMap<String, String>();
                for (int x = 1; x <= rs.getMetaData().getColumnCount(); ++x) {
                    rowToAdd.put(rs.getMetaData().getColumnName(x), rs.getString(x));
                }
                ColData.add(rowToAdd);
            }
        } catch (SQLException e) {

            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
        return ColData;
    }

    public ArrayList<Point> getImageValuesOfFloor(int i) {
        return this.pointsForFloor.get(i);
    }

    public int getNumberOfFloors() {
        return queries.length;
    }

    public int getWidthOfFloor(int i) {
        return this.floorWidths.get(i);
    }


    public int getHeightOfFloor(int i) {
        return this.floorHeights.get(i);
    }

    public int getMinXOfFloor(int i) {
        return this.floorMinXs.get(i);
    }

    public int getMinYOfFloor(int i) {
        return this.floorMinYs.get(i);
    }


    public Collection<String> getDataNames() {
        if (dataNames == null) {
            initializeDataNames();
        }
        return dataNames;
    }

    private void initializeDataNames() {
        this.dataNames = new ArrayList<String>();
        String positionQuery = "SELECT distinct name FROM mc_statistician.IdPlayerMapping where obsolete =0;";
        List<Map<String, String>> results = executeSynchQuery(positionQuery);


        for (Map<String, String> row : results) {
            String name = row.get("name");

            this.dataNames.add(name);

        }

    }

    public ListMultimap<String, Integer> getDataNameAndAttempts() {
        if (dataNameAttemptMap == null) {
            initializeDataNameAttemptMap();
        }
        return dataNameAttemptMap;
    }

    private void initializeDataNameAttemptMap() {
        if (dataNames == null) {
            initializeDataNames();
        }
        this.dataNameAttemptMap = ArrayListMultimap.create();
        for (String dataName : dataNames) {
            String name = getNameFromData(dataName);
            Integer attempt = Integer.parseInt(getNumberFromData(dataName));
            this.dataNameAttemptMap.put(name, attempt);


        }


    }

    private String getNameFromData(String dataName) {
        for (int i = 0; i < dataName.length(); i++) {
            if (Character.isDigit(dataName.charAt(i))) {
                return dataName.substring(0, i);
            }
        }
        assert false;
        return null;
    }

    private String getNumberFromData(String dataName) {
        for (int i = 0; i < dataName.length(); i++) {
            if (Character.isDigit(dataName.charAt(i))) {
                return dataName.substring(i, dataName.length());
            }
        }
        assert false;
        return null;
    }


}


