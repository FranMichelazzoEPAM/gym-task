package gym.service;

public interface UserService {
    void changePassword(String username, String oldPassword, String newPassword);
}
