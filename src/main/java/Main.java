import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Main {
    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/zapchasty";
//    private static final String DEFAULT_URL = "jdbc:postgresql://116.203.55.188:5432/zapchasty";
    private static final String DEFAULT_USERNAME = "zapchasty";
    private static final String DEFAULT_PASSWORD = "zapchasty_GfhjkzYtn321";

    private static final String path = "e:\\part4\\part4_spec\\";
    private static final String[] brand_list = {"brother","canon","epson","hp","konica minolta","kyocera","lexmark","oki","panasonic","ricoh","riso","samsung","sharp","toshiba","xerox"};
//    private static final String[] brand_list = {"brother"};
    private static FileOutputStream saveFileStream;

    public static void main(String[] args) {
        String driver = ((args.length > 0) ? args[0] : DEFAULT_DRIVER);
        String url = ((args.length > 1) ? args[1] : DEFAULT_URL);
        String username = ((args.length > 2) ? args[2] : DEFAULT_USERNAME);
        String password = ((args.length > 3) ? args[3] : DEFAULT_PASSWORD);
        Connection connection = null;


        int brand_id = 0; // ID ПРОИЗВОДИТЕЛЯ
        String path_list = null;
        int fIndex;
        int model_id = 0;

        Date startTime = new Date();
        System.out.println("Запуск " + startTime.toString());

        try {
            saveFileStream = new FileOutputStream(path+"all_image_url", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = createConnection(driver, url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < brand_list.length; j++) {
            brand_id = j;
            path_list = path + brand_list[brand_id];
            System.out.println(++brand_id + ": " + path_list);

            try {
                File dir = new File(path_list); //path указывает на директорию
                File[] arrFiles = dir.listFiles();
                List<File> lst = Arrays.asList(arrFiles);

                System.out.println(lst.size()/2);
                fIndex = 0;

                for (File file : lst) {
                    try {
                        String fileName = file.getName();
                        fileName = fileName.replaceAll(".csv","");
                        if (fileName.contains("_images")){
                            fileName = fileName.replaceAll("_images","");

                            fileName = fileName.replaceAll("([a-zA-Z0-9]+ мм)? \\([a-zA-Z0-9]+\\)", "").trim();
                            fileName = fileName.replaceAll("[+]", "plus");
                            fileName = fileName.replaceAll("[/]", "_");

                            String nDetailName = fileName.replaceAll("(-)(?!.*-)", "");

                            List editModelIdParametrs = Arrays.asList(nDetailName);
                            model_id = getId(connection, editModelIdParametrs);

                            if (model_id == 0) {
                                List modelName = Arrays.asList(fileName);
                                model_id = getId(connection, modelName);
                            }

                            System.out.println(fileName + ": " + model_id);
                            try {
                                FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                                String strLine;
                                StringBuilder sb = new StringBuilder();

                                while ((strLine = bufferedReader.readLine()) != null) {
                                    strLine = strLine.replaceAll("https://static.1k.by","");
                                    strLine = "\"" + strLine + "\"";
//                                    sb.append(strLine).append(System.lineSeparator());
                                    sb.append(strLine).append(",");
//                                    saveFileStream.write(sb.toString().getBytes());
                                }
                                String outText = sb.toString().substring(0, sb.toString().length()-1);
                                outText = "["+outText+"]";



                                if (model_id != 0) {
                                    String addURL = "UPDATE all_models SET picture = to_json(?::json) WHERE id = ?";
                                    List urlParametrs = Arrays.asList(outText, model_id);
                                    update(connection, addURL, urlParametrs);
                                } else {
                                    System.out.println("MODEL_ID = "+model_id);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Date endTime = new Date();
        System.out.println("Завершение " + endTime.toString());
        System.out.println("Старт: " + startTime + ", Завершение: " + endTime.toString());
    }

    public static int getId(Connection connection,  List<Object> parameters) throws SQLException {
        int results = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
//            ps = connection.prepareStatement("SELECT id FROM detail_options WHERE name = ?;");
            ps = connection.prepareStatement("SELECT id FROM all_models WHERE name = ?;");
            int i = 0;

            for (Object parameter : parameters) {
                ps.setObject(++i, parameter);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                results = rs.getInt("id");
            }

        } finally {
            close(rs);
            close(ps);
        }
        return results;
    }

    public static int update(Connection connection, String sql, List<Object> parameters) throws SQLException {
        int numRowsUpdated = 0;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            int i = 0;
            for (Object parameter : parameters) {
                ps.setObject(++i, parameter);
            }
            System.out.println(ps.toString());
            numRowsUpdated = ps.executeUpdate();
        } finally {
            close(ps);
        }
        return numRowsUpdated;
    }

    public static Connection createConnection(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        if ((username == null) || (password == null) || (username.trim().length() == 0) || (password.trim().length() == 0)) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, username, password);
        }
    }

    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> map(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        try {
            if (rs != null) {
                ResultSetMetaData meta = rs.getMetaData();
                int numColumns = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<String, Object>();

                    for (int i = 1; i <= numColumns; ++i) {
                        String name = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(name, value);
                    }

                    results.add(row);
                }
            }
        } finally {
            close(rs);
        }

        return results;
    }

}
