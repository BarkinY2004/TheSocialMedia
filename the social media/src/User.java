import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Kullanıcı (User) sınıfı
public class User {
    private String username;
    private String password;
    private String wall;
    private List<User> friends;
    private MessageManager messageManager;
    private List<UserObserver> observers = new ArrayList<>(); //// gerekirse sil

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.friends = new ArrayList<>();
        this.observers =new ArrayList<>(); /// gerekirse sil
        this.messageManager = new BasicMessageManager(); // Varsayılan mesaj yöneticisi
    }
    public void setWall(String wall) {
        this.wall = wall;
    }

    public void addObserver(UserObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(UserObserver observer) {
        observers.remove(observer);
    }

    public void updateUser(String user2, String user1) {

        for (UserObserver observer : observers) {
            observer.update(user2, user1);
        }
    }
    public void removeLine(String current_user){
        for (UserObserver observer : observers) {
            observer.removeLine(current_user);
        }
    }


    public String getWall() {
        return wall;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void setFriendList(List<User> friends) {
        this.friends = friends;
    }

    public void setMessagingSystem(MessageManager messageManager) {
        this.messageManager = messageManager;
    }


    public void sendFriendRequest(User otherUser) {
        String requester = otherUser.getUsername();
        String recipient = this.getUsername();

        // Kontrol için arkadas.txt dosyasını oku
        boolean alreadyFriends = checkIfFriends(requester, recipient) || checkIfFriends(recipient, requester);

        if (alreadyFriends) {
            JOptionPane.showMessageDialog(null, "Zaten arkadaşsınız!");
            return; // Zaten arkadaş ise işlemi bitir
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("istekler.txt", true))) {
            writer.write(requester + ":" + recipient + "*\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(null, "Arkadaşlık isteği gönderildi!");
    }

    // İki kullanıcının arkadaş olup olmadığını kontrol et
    public boolean checkIfFriends(String user1, String user2) {
        try (BufferedReader reader = new BufferedReader(new FileReader("arkadas.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String firstUser = parts[0];
                    String secondUser = parts[1];
                    if (firstUser.equals(user1) && secondUser.equals(user2)) {
                        return true; // İlk kullanıcı ve ikinci kullanıcı arkadaş ise true döndür
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void processFriendRequests() {
        try (BufferedReader reader = new BufferedReader(new FileReader("istekler.txt"))) {
            String line;
            List<String> newLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(this.username)) {
                    String[] requests = parts[1].split("\\*");
                    for (String request : requests) {
                        String requester = request.trim();
                        int choice = JOptionPane.showConfirmDialog(null, requester + " sizi arkadaş olarak eklemek istiyor. Kabul ediyor musunuz?", "Arkadaşlık İsteği", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            this.addFriend(requester);
                        }
                    }
                } else {
                    newLines.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("istekler.txt"))) {
                for (String newLine : newLines) {
                    writer.write(newLine + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void addFriend(String friendUsername) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("arkadas.txt", true))) {
            writer.write(this.username + ":" + friendUsername + "\n");
            this.friends.add(new User(friendUsername, ""));
            writer.write(friendUsername + ":" +  this.username+ "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<User> getFriends() {
        return friends;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
