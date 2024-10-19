import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// Kullanıcı Fabrika (Factory) sınıfı
public class UserFactory {
    public static User createUser(String username, String password) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(username + ":" + password + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new User(username, password);
    }
    @Test
    public void testSocialMedia() {
        UserFactory FACTORY2 = new UserFactory();
        User user1 = FACTORY2.createUser("emma", "1234");
        UserFactory factory = new UserFactory();
        User user =factory.createUser("john","1234");
        assertEquals("john",user.getUsername());
        assertEquals("emma",user1.getUsername());
    }
    @Test
    public void testSocialMedia2() {
        User test_user = new User("test", "1234");
        boolean control = test_user.checkIfFriends("user3","user2");
        assertTrue(control);
    }
}
