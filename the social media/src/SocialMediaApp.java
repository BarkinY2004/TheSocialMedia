import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

//////////////////////////////////////////////////////////////////////////////////////




// kullanıcı adlarını  ve  grup adlarını tutar
class GrupManager {
    private ArrayList<String> kullaniciAdlari;
    private String grupAdi;

    public GrupManager() {
        kullaniciAdlari = new ArrayList<>();
    }

    public void setGrupAdi(String grupAdi) {
        this.grupAdi = grupAdi;
    }

    public void addKullanici(String kullaniciAdi) {
        kullaniciAdlari.add(kullaniciAdi);
    }

    public ArrayList<String> getKullaniciAdlari() {
        return kullaniciAdlari;
    }

    public String getGrupAdi() {
        return grupAdi;
    }

    public void clearKullaniciAdlari() {
        kullaniciAdlari.clear();
    }
}

// girilen bilgileri alır
class KullaniciManager {
    public String getGrupAdi() {
        JTextField grupAdiField = new JTextField();
        int result = JOptionPane.showConfirmDialog(null, grupAdiField, "Grup Adını Girin:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return grupAdiField.getText();
        }
        return null;
    }

    public String getKullaniciAdi() {
        JTextField kullaniciAdiField = new JTextField();
        int result = JOptionPane.showConfirmDialog(null, kullaniciAdiField, "Kullanıcı Adı Ekleyin:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return kullaniciAdiField.getText();
        }
        return null;
    }
}

// dosyaya girile biligileri yazdırır ve okur
class DosyaManager {
    private static final String DOSYA_ADI = "grup.txt";

    public void dosyayaYaz(String grupAdi, ArrayList<String> kullaniciAdlari) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOSYA_ADI, true))) {
            writer.write(grupAdi + " : ");
            for (int i = 0; i < kullaniciAdlari.size(); i++) {
                writer.write(kullaniciAdlari.get(i));
                if (i < kullaniciAdlari.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> gruplariOku(String currentUsername) {
        ArrayList<String> gruplar = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DOSYA_ADI))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts.length == 2) {
                    String grupAdi = parts[0];
                    String[] kullanicilar = parts[1].split(", ");
                    for (String kullanici : kullanicilar) {
                        if (kullanici.equals(currentUsername)) {
                            gruplar.add(grupAdi + " : " + parts[1]);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gruplar;
    }
}

// facade design patterni uyguladık
class Facade {
    private GrupManager grupManager;
    private KullaniciManager kullaniciManager;
    private DosyaManager dosyaManager;
    private String currentUsername;

    public Facade(String currentUsername) {
        this.grupManager = new GrupManager();
        this.kullaniciManager = new KullaniciManager();
        this.dosyaManager = new DosyaManager();
        this.currentUsername = currentUsername;
    }

    public void grupOlustur() {
        String grupAdi = kullaniciManager.getGrupAdi();
        if (grupAdi != null) {
            grupManager.setGrupAdi(grupAdi);
            while (true) {
                String kullaniciAdi = kullaniciManager.getKullaniciAdi();
                if (kullaniciAdi != null) {
                    grupManager.addKullanici(kullaniciAdi);
                } else {
                    break;
                }
            }
            dosyaManager.dosyayaYaz(grupAdi, grupManager.getKullaniciAdlari());
            JOptionPane.showMessageDialog(null, "Grup başarıyla oluşturuldu ve dosyaya yazıldı.");
            grupManager.clearKullaniciAdlari();
        }
    }

    public void gruplariGoster() {
        ArrayList<String> gruplar = dosyaManager.gruplariOku(currentUsername);
        if (gruplar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Hiçbir grup bulunamadı veya uygun grup yok.");
        } else {
            StringBuilder mesaj = new StringBuilder("Kullanıcının Bulunduğu Gruplar:\n");
            for (String grup : gruplar) {
                mesaj.append(grup).append("\n");
            }
            JOptionPane.showMessageDialog(null, mesaj.toString());
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////

// mesaj gönderildi bildirimi için arayüz
interface MessageManager {
    void sendMessage(User sender, User recipient, String message);
}

// arayüzü uyguladık
class BasicMessageManager implements MessageManager {
    @Override
    public void sendMessage(User sender, User recipient, String message) {

        JOptionPane.showMessageDialog(null, "mesaj başarıyla gönderildi ");
    }
}

// engelleme işlemleri için class
class BlockedListManager {
    private static final String BLOCKED_FILE_PATH = "Engel.txt";

    public static void blockUser(User blocker, String blockedUsername) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BLOCKED_FILE_PATH, true))) {
            writer.write(blockedUsername + ":" + blocker.getUsername() + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isUserBlocked(User blocker, String blockedUsername) {
        try (BufferedReader reader = new BufferedReader(new FileReader(BLOCKED_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals( blocker.getUsername()) && parts[1].equals(blockedUsername)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}



// Observer arayüzü
interface UserObserver {
    void update(String user2, String user1);
    void removeLine(String current_user);
}

// Concrete Observer sınıfı
class NotificationManager implements UserObserver {
    private String currentUser;

    public NotificationManager(String currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void update(String user2, String user1) {
        try {
            // Check if the current user is the user1 from the line
            if (user2.equals(currentUser)|| user1.equals(currentUser)) {

                String notification = user2 + " : " + user1 + " profilini güncelledi.";
                writeNotification(notification);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNotification(String notification) throws IOException {
        FileWriter writer = new FileWriter("story.txt", true); // Append mode
        writer.write(notification + "\n");
        writer.close();

    }

    @Override
    public void removeLine(String current_user)  {
        try (BufferedReader reader = new BufferedReader(new FileReader("story.txt"))) {
            String line;
            List<String> newLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts.length == 2 && parts[0].equals(current_user)) {
                    String story_update = parts[1];
                    JOptionPane.showMessageDialog(null,story_update);
                } else {
                    newLines.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("story.txt"))) {
                for (String newLine : newLines) {
                    writer.write(newLine + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}

class ImagePanel extends JPanel {
    private Image backgroundImage;

    // Arka plan resminin dosya yolunu parametre olarak alan constructor
    public ImagePanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
    }

    // JPanel'in paintComponent metodunu geçersiz kıldık
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Arka plan resmini çizdik
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

// decerator pattern için arayüz
interface TextAreaDecorator {
    void writeTo(String fileName,User username) throws IOException;
}

// JTextArea'yı saran(wrap) decorator sınıfı ile arayüzü uyguladık
class FileWritingTextAreaDecorator implements TextAreaDecorator {
    private JTextArea textArea;
    private User currentUser;

    public FileWritingTextAreaDecorator(JTextArea textArea,User currentUser) {
        this.textArea = textArea;
        this.currentUser = currentUser;
    }

    // Dosyaya yazma işlevselliği eklenmiş JTextArea
    @Override

    public void writeTo(String fileName,User username) throws IOException {
        this.currentUser = username;


        try (BufferedReader reader = new BufferedReader(new FileReader("wall_content.txt"))) {
            String line;
            List<String> newLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username.getUsername())) {

                } else {
                    newLines.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("wall_content.txt"))) {
                for (String newLine : newLines) {
                    writer.write(newLine + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] content2 = textArea.getText().split("\n");
        String content = String.join(" ", content2);


        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(System.lineSeparator()); // Yeni satır ekle
            writer.write(currentUser.getUsername() + ":" + content); // Kullanıcı adını ve metni dosyaya yaz
        }

    }
}




// Kullanıcı için geliştirilen ve GUI ile extend edilen bir arayüz
// programın yapı taşı
public class SocialMediaApp extends JFrame {


    public void logMessage(User sender, User recipient, String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("message.txt", true))) {
            writer.write( recipient.getUsername() + " : " + sender.getUsername()+ " \"" + message + "\"\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void wall(User currentUser) {
        try (BufferedReader reader = new BufferedReader(new FileReader("wall_content.txt"))) {
            List<String> newLines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].trim().equals(currentUser.getUsername())) {
                    // Bu kullanıcıya yönlendirilmiş mesajı göster
                    String[] wallParts = parts[1].split("\"");
                    if (wallParts.length == 1) {
                        String senderUsername = wallParts[0].trim();
                        String wallContent = wallParts[0].trim();

                        currentUser.setWall(wallContent);
                    }

                }else {
                    // İstenmeyen satırı yeni listeye ekle
                    newLines.add(line);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void processMessages(User currentUser) {
        try (BufferedReader reader = new BufferedReader(new FileReader("message.txt"))) {
            List<String> newLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].trim().equals(currentUser.getUsername())) {
                    // Bu kullanıcıya yönlendirilmiş mesajı göster
                    String[] messageParts = parts[1].split("\"");
                    if (messageParts.length == 2) {
                        String senderUsername = messageParts[0].trim();
                        String messageContent = messageParts[1];
                        JOptionPane.showMessageDialog(null, senderUsername + " kullanıcısından mesaj: \"" + messageContent + "\"");

                    }
                }else {
                    // İstenmeyen satırı yeni listeye ekle
                    newLines.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("message.txt"))) {
                for (String newLine : newLines) {
                    writer.write(newLine + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private JTextField usernameField;
    private JPasswordField passwordField;
    private User currentUser;
    // görüntünün gelmesini saplayan metot
    // içerisinde show main menu çağrılarak giriş sonrası gelicek yeni sayfaya geçiş yapabiliyoruz
    public SocialMediaApp() {
        super("Social Media App");
        ;

        // Arka plan resminin dosya yolu
        String imagePath = "/Users/barkinyildirim/IdeaProjects/hello/photo.png";

        // ImagePanel oluşturun ve arka plan resmini ayarlayın
        ImagePanel imagePanel = new ImagePanel(imagePath);
        setContentPane(imagePanel); // JFrame'in içeriği olarak ImagePanel'i kullan

        JPanel loginPanel = new JPanel(new GridLayout(4, 2));
        loginPanel.setOpaque(false);

        JLabel kullanıcı = new JLabel("User Name:");
        kullanıcı.setForeground(Color.green);
        Font boldFont = new Font(kullanıcı.getFont().getName(), Font.BOLD, kullanıcı.getFont().getSize());
        kullanıcı.setFont(boldFont);
        loginPanel.add(kullanıcı);
        usernameField = new JTextField();
        loginPanel.add(usernameField);
        JLabel pass = new JLabel("Password:");
        pass.setForeground(Color.green);
        Font boldFont2 = new Font(pass.getFont().getName(), Font.BOLD, pass.getFont().getSize());
        pass.setFont(boldFont2);
        loginPanel.add(pass);
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton registerButton = new JButton("Sign Up");



        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username and password cannot be empty!");
                    return;
                }

                currentUser = UserFactory.createUser(username, password);
                JOptionPane.showMessageDialog(null, "\n" +
                        "Registration successful! New user has been created");
            }
        });
        loginPanel.add(registerButton);

        JButton loginButton = new JButton("Log in");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                currentUser = authenticateUser(username, password);

                if (currentUser != null) {
                    currentUser.processFriendRequests();
                    showMainMenu(currentUser);
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect username or password!");
                }
            }
        });
        loginPanel.add(loginButton);
        JButton about_me = new JButton("About me");

        about_me.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imagePath = "/Users/barkinyildirim/IdeaProjects/hello/admin.png"; // Resmin dosya yolu buraya yazılmalıdır

                ImageIcon imageIcon = new ImageIcon(imagePath); // Resmi yüklemek için ImageIcon kullanılır
                JLabel imageLabel = new JLabel(imageIcon);

                // Resim içeren popup penceresi oluştur
                JOptionPane.showMessageDialog(null, imageLabel, "'Mr.Robot'", JOptionPane.PLAIN_MESSAGE);
            }
        });
        loginPanel.add(about_me);




        add(loginPanel);
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private User authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String storedUsername = parts[0];
                    String storedPassword = parts[1];
                    if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        return new User(username, password);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    // bu metot ile eğer kullanıcı giriş yaparsa
    // yeni bir sayfa açılıyor
    // bu sayfada öncelikle eğer kullanıcıya bildirim geldiyse onlar gösteriliyor
    // ve kullanıcının işlem yapması için gerekli butonlar ve textarelar sayfanın belirli bölgelerine yerleştiriliyor
    private void showMainMenu(User currentUser) {
        getContentPane().removeAll();

        JPanel mainMenuPanel = new JPanel(new BorderLayout());

        JTextArea friendListArea = new JTextArea();
        friendListArea.setEditable(false);

        JTextArea wallArea = new JTextArea();
        wallArea.setEditable(true);

        JTextArea wallArea2 = new JTextArea();

        wallArea2.setEditable(false);

        updateFriendList(friendListArea, currentUser);
        JButton saveButton = new JButton("Confirm");



        mainMenuPanel.add(new JScrollPane(friendListArea), BorderLayout.CENTER);
        mainMenuPanel.add(new JScrollPane(wallArea), BorderLayout.NORTH);
        mainMenuPanel.add(new JScrollPane(wallArea2), BorderLayout.WEST);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


        NotificationManager notificationManager = new NotificationManager(currentUser.getUsername());
        currentUser.addObserver(notificationManager);
        currentUser.removeLine(currentUser.getUsername());

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                TextAreaDecorator decorator = new FileWritingTextAreaDecorator(wallArea,currentUser);
                try {
                    decorator.writeTo("wall_content.txt",currentUser);
                    wallArea2.setText("");
                    wall(currentUser);

                    wallArea2.append(currentUser.getWall());



                    // Read lines from arkadas.txt and process updates
                    BufferedReader reader = new BufferedReader(new FileReader("arkadas.txt"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String user1 = parts[0];
                            String user2 = parts[1];

                            // Check if user1 is the current user
                            if (user1.equals(currentUser.getUsername())) {

                                currentUser.updateUser(user2, user1);
                            }
                        }
                    }
                    reader.close();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "The text has been saved and read successfully!");
            }
        });
        wall(currentUser);

        wallArea2.append(currentUser.getWall() + "\n");


        JButton show_story = new JButton("show story");
        show_story.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog((Frame) null, "\n" +
                        "User Information", true); // 'true' ile pencerenin modal olmasını sağlar

                try {
                    JPanel panel = new JPanel(new BorderLayout());
                    JTextArea area = new JTextArea();
                    //mainMenuPanel.add(new JScrollPane(friendListArea), BorderLayout.CENTER);
                    panel.add(new JScrollPane(area), BorderLayout.CENTER);
                    dialog.add(panel);
                    dialog.setLocationRelativeTo(null);


                    // Pencere boyutunu ayarlayın
                    dialog.setSize(250, 100);



                    BufferedReader reader = new BufferedReader(new FileReader("wall_content.txt"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String user1 = parts[0];
                            String wall = parts[1];
                            for (User friend : currentUser.getFriends()) {
                                if (friend.getUsername().equals(user1) && !BlockedListManager.isUserBlocked(currentUser, user1)) {
                                    area.append(user1 + ":" + wall + "\n");
                                }
                            }
                            // Check if user1 is the current user

                        }
                    }
                    reader.close();
                }catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                // İçerik ekleyin



                // Pencereyi göster
                dialog.setVisible(true);
            }
        });





        JButton sendRequestButton = new JButton("Send Friend Request");
        sendRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFriendName = JOptionPane.showInputDialog(null, "Enter the username you want to send a friend request to:");
                if (selectedFriendName != null && !selectedFriendName.isEmpty()&& !selectedFriendName.equals(currentUser.getUsername())) {
                    if (BlockedListManager.isUserBlocked(currentUser, selectedFriendName)) {
                        JOptionPane.showMessageDialog(null, "This user has blocked you!");
                        return;
                    }
                    User selectedFriend = findUser(selectedFriendName);
                    if (selectedFriend != null) {
                        currentUser.sendFriendRequest(selectedFriend);
                    } else {
                        JOptionPane.showMessageDialog(null, "The specified user could not be found.");
                    }
                }
            }
        });

        bottomPanel.add(sendRequestButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(show_story);

        JButton sendMessageButton = new JButton("Send Message");
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFriendName = JOptionPane.showInputDialog(null, "Enter the username you want to send messages to:");
                if (selectedFriendName != null && !selectedFriendName.isEmpty()) {
                    if (BlockedListManager.isUserBlocked(currentUser, selectedFriendName)) {
                        JOptionPane.showMessageDialog(null, "\n" +
                                "This user has blocked you!");
                        return;
                    }
                    User selectedFriend = findUser(selectedFriendName);
                    if (selectedFriend != null) {
                        String message = JOptionPane.showInputDialog(null, "Enter your message:");
                        String[] options = {"Hello", "How are you?", "I'm fine, thank you!"};

                        // Hızlı cevaplarla birlikte dialog kutusunu gösterelim
                        int selectedOption = JOptionPane.showOptionDialog(null,
                                "Choose one of the quick answers below:",
                                "Quick Reply",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0]);

                        if (message != null && !message.isEmpty()) {
                            logMessage(currentUser, selectedFriend, message);
                            currentUser.getMessageManager().sendMessage(currentUser, selectedFriend, message);
                        }
                        if (selectedOption != JOptionPane.CLOSED_OPTION){
                            message = options[selectedOption];
                            logMessage(currentUser, selectedFriend, message);
                            currentUser.getMessageManager().sendMessage(currentUser, selectedFriend, message);

                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "The specified user could not be found.");
                    }
                }
            }
        });
        bottomPanel.add(sendMessageButton);

        JButton blockUserButton = new JButton("Block");
        blockUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String blockedUsername = JOptionPane.showInputDialog(null, "Enter the username you want to block:");
                if (blockedUsername != null && !blockedUsername.isEmpty()) {
                    if (findUser(blockedUsername) != null) {
                        BlockedListManager.blockUser(currentUser, blockedUsername);
                        JOptionPane.showMessageDialog(null, blockedUsername + " blocked!");
                    } else {
                        JOptionPane.showMessageDialog(null, "The specified user could not be found.");
                    }
                }
            }
        });
        bottomPanel.add(blockUserButton);
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Signing out");
                dispose();
                new SocialMediaApp();
            }
        });
        bottomPanel.add(logoutButton);


        Facade facade = new Facade(currentUser.getUsername());

        JButton grupOlusturButon = new JButton("Enter Group Name");
        grupOlusturButon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                facade.grupOlustur();
            }
        });
        bottomPanel.add(grupOlusturButon);
        JButton gruplariGosterButon = new JButton("Show Groups");
        gruplariGosterButon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                facade.gruplariGoster();
            }
        });
        bottomPanel.add(gruplariGosterButon);


        mainMenuPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainMenuPanel);
        revalidate();
        repaint();
        processMessages(currentUser);
    }

    private void updateFriendList(JTextArea friendListArea, User currentUser) {
        if (currentUser != null) {
            friendListArea.setText("Your friends:\n");
            try (BufferedReader reader = new BufferedReader(new FileReader("arkadas.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2 && parts[0].equals(currentUser.getUsername())) {
                        friendListArea.append("- " + parts[1] + "\n");
                        currentUser.getFriends().add(new User(parts[1],""));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private User findUser(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String storedUsername = parts[0];
                    if (storedUsername.equals(username)) {
                        return new User(storedUsername, parts[1]);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // ana metotumuz çağrıldı
                new SocialMediaApp();
            }
        });
    }
}



