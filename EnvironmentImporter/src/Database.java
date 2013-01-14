import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String DATABASE_ADDRESS = "155.69.151.100";
    private static final String DATABASE_PORT = "3306";
    private static final String DATABASE_NAME = "mc_statistician";
    private static final String USERNAME = "vaisagh";
    private static final String PASSWORD = "vaisaghviswanathan";

    private static final String[] queries =
            {"SELECT x, z " +
                    "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerlocationforanalysis as plfa " +
                    "where name = \"newgroundfloor\" " +
                    "and y <= 24 " +
                    "and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime",
                    "SELECT x, z " +
                            "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerlocationforanalysis as plfa " +
                            "where name = \"newsecondfloor2\"" +
                            "and y <= 33 and y > 24 " +
                            "and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime",
                    "SELECT x, z " +
                            "FROM mc_statistician.IdPlayerMapping as idp, mc_statistician.playerlocationforanalysis as plfa " +
                            "where name = \"newthirdfloor\" " +
                            "and y > 33 " +
                            "and plfa.uuid = idp.uuid and plfa.minTime = idp.startTime"};


    private Connection connection = null;
    private ArrayList<ArrayList<Point>> pointsForFloor;
    private ArrayList<Integer> floorWidths = new ArrayList<Integer>();
    private ArrayList<Integer> floorHeights = new ArrayList<Integer>();
    private ArrayList<Integer> floorMinXs = new ArrayList<Integer>();
    private ArrayList<Integer> floorMinYs = new ArrayList<Integer>();


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
            List<Map<String, String>> results = executeSynchQuery(query);
            ArrayList<Point> listOfPoints = new ArrayList<Point>();
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
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
}


