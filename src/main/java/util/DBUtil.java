package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtil {

    public static void execute(Connection conn, String sql) {
        execute(conn, sql, null);
    }

    // 执行sql语句
    public static void execute(Connection conn, String sql, List<Object> list) {
        PreparedStatement pstmt = null;

        // 编译sql语句
        try {
            pstmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 传递数据
        if (list != null) {
            try {
                for (int i = 0; i < list.size(); i++) {
                    pstmt.setObject(i + 1, list.get(i));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 执行sql语句
        try {
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Map<String, String>> query(Connection conn, String sql) {
        return query(conn, sql, null);
    }

    // 执行sql语句，返回结果集
    public static List<Map<String, String>> query(Connection conn, String sql, List<Object> list) {
        List<Map<String, String>> resultList = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 编译sql语句
        try {
            pstmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 传递数据
        if (list != null) {
            try {
                for (int i = 0; i < list.size(); i++) {
                    pstmt.setObject(i + 1, list.get(i));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 执行sql语句
        try {
            rs = pstmt.executeQuery();
            System.out.println(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 整理结果集
        if (rs != null) {
            try {
                resultList = new ArrayList<>();
                ResultSetMetaData metaData = rs.getMetaData();
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        String key = metaData.getColumnName(i + 1);
                        String value = rs.getString(i + 1);
                        map.put(key, value);
                    }
                    resultList.add(map);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 关闭资源
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }

}
