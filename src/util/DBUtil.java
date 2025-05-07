package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DBUtil {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("src/db.properties"));

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB 오류] Oracle 드라이버 로드 실패");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[DB 오류] 설정 파일 로드 실패");
            e.printStackTrace();
        }
    }

    // DB 연결
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DB 연결 실패] " + e.getMessage());
            return null;
        }
    }

    // 자원 해제 - Statement 포함
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("[DB 해제 오류] " + e.getMessage());
        }
    }

    // 자원 해제 - PreparedStatement 버전
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("[DB 해제 오류] " + e.getMessage());
        }
    }
}
