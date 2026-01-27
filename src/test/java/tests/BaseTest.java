package tests;

import dao.CommentDao;
import dao.PostDao;
import dao.UserDao;
import dao.UserMetaDao;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import utils.ApiRequestBuilder;
import utils.BaseAPIRequests;
import utils.DataCleanRegistry;
import utils.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class BaseTest {

    protected Connection connection;
    protected UserDao userDao;
    protected UserMetaDao userMetaDao;
    protected PostDao postDao;
    protected CommentDao commentDao;
    protected RequestSpecification requestSpecification;
    protected ApiRequestBuilder apiRequestBuilder;

    @BeforeClass
    public void setup() {
        requestSpecification = BaseAPIRequests.initRequestSpecification();
        connection = DatabaseManager.getConnection();
        userDao = new UserDao(connection);
        userMetaDao = new UserMetaDao(connection);
        postDao = new PostDao(connection);
        commentDao = new CommentDao(connection);
        apiRequestBuilder = new ApiRequestBuilder(requestSpecification);
    }

    @AfterMethod
    public void cleanupAfterTest() {
        DataCleanRegistry.cleanupAll(userDao, postDao, commentDao, userMetaDao);
    }

    @AfterClass
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
