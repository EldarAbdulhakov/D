package utils;

import dao.CommentDao;
import dao.PostDao;
import dao.UserDao;
import dao.UserMetaDao;

import java.util.ArrayList;
import java.util.List;

public class DataCleanRegistry {

    private static final ThreadLocal<List<Integer>> createdCommentIds =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<List<Integer>> createdPostIds =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<List<Integer>> createdUserIds =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<List<Integer>> createdUserMetaIds =
            ThreadLocal.withInitial(ArrayList::new);

    public static void addComment(Integer commentId) {
        if (commentId != null && commentId != -1) {
            createdCommentIds.get().add(commentId);
        }
    }

    public static void addPost(Integer postId) {
        if (postId != null && postId != 0) {
            createdPostIds.get().add(postId);
        }
    }

    public static void addUser(Integer userId) {
        if (userId != null && userId != 0) {
            createdUserIds.get().add(userId);
        }
    }

    public static void addUserMeta(Integer userMetaId) {
        if (userMetaId != null && userMetaId != 0) {
            createdUserMetaIds.get().add(userMetaId);
        }
    }

    public static void cleanupAll(UserDao userDao, PostDao postDao, CommentDao commentDao, UserMetaDao userMetaDao) {
        List<RuntimeException> errors = new ArrayList<>();

        for (Integer commentId : createdCommentIds.get()) {
            try {
                commentDao.deleteComment(commentId);
            } catch (Exception e) {
                errors.add(new RuntimeException("Failed to delete comment " + commentId, e));
            }
        }
        createdCommentIds.get().clear();

        for (Integer postId : createdPostIds.get()) {
            try {
                postDao.deletePost(postId);
            } catch (Exception e) {
                errors.add(new RuntimeException("Failed to delete post " + postId, e));
            }
        }
        createdPostIds.get().clear();

        for (Integer userId : createdUserMetaIds.get()) {
            try {
                userMetaDao.deleteUserMeta(userId);
            } catch (Exception e) {
                errors.add(new RuntimeException("Failed to delete user meta for user " + userId, e));
            }
        }
        createdUserMetaIds.get().clear();

        for (Integer userId : createdUserIds.get()) {
            try {
                userDao.deleteUser(userId);
            } catch (Exception e) {
                errors.add(new RuntimeException("Failed to delete user " + userId, e));
            }
        }
        createdUserIds.get().clear();

        clear();

        if (!errors.isEmpty()) {
            RuntimeException cleanupException =
                    new RuntimeException("Cleanup finished with errors");
            errors.forEach(cleanupException::addSuppressed);
            throw cleanupException;
        }
    }

    public static void clear() {
        createdCommentIds.remove();
        createdPostIds.remove();
        createdUserIds.remove();
        createdUserMetaIds.remove();
    }
}
