package tests;

import dao.UserDao;
import dao.UserMetaDao;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import utils.BaseAPIRequests;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class BaseTest {

    protected Connection connection;
    protected UserDao userDao;
    protected UserMetaDao userMetaDao;
    protected RequestSpecification requestSpecification;

    @BeforeClass
    public void setup() throws SQLException {
        requestSpecification = BaseAPIRequests.initRequestSpecification();
        connection = DatabaseManager.getConnection();
        userDao = new UserDao(connection);
        userMetaDao = new UserMetaDao(connection);
    }

    @AfterClass
    public void tearDown() throws SQLException {
        connection.close();
    }
}
