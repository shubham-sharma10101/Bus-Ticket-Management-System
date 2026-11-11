import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends JFrame {

    // ==========================================
    // DATABASE CONFIGURATION
    // ==========================================
    private static final String DB_HOST = "localhost:3306";
    private static final String DB_NAME = "krisna_travels";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Shubhamsharma9782+-"; // Your MySQL Password

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Modern Commercial Travel Palette (Crimson Red, Midnight Slate & Warm Gold)
    private static final Color BRAND_RED = new Color(217, 35, 45);          // Vibrant Bus Red
    private static final Color BRAND_RED_DARK = new Color(153, 27, 27);     // Deep Burgundy
    private static final Color BRAND_NAVY = new Color(15, 23, 42);          // Midnight Slate
    private static final Color BRAND_NAVY_LIGHT = new Color(30, 41, 59);    // Header Slate
    private static final Color BRAND_GOLD = new Color(245, 158, 11);         // Amber Star Gold
    private static final Color BRAND_GREEN = new Color(16, 185, 129);       // Emerald Success
    private static final Color BRAND_BG = new Color(248, 250, 252);         // Soft Canvas Light
    private static final Color BRAND_CARD = Color.WHITE;
    private static final Color BRAND_TEXT = new Color(15, 23, 42);
    private static final Color BRAND_MUTED = new Color(100, 116, 139);
    private static final Color BRAND_BORDER = new Color(226, 232, 240);

    private Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
            conn.setAutoCommit(true);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverUrl = "jdbc:mysql://" + DB_HOST + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection rootConn = DriverManager.getConnection(serverUrl, DB_USER, DB_PASS);
                 Statement stmt = rootConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            try (Connection conn = connectDB();
                 Statement stmt = conn.createStatement()) {
                if (conn == null) return;

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "full_name VARCHAR(100) NOT NULL,"
                        + "email VARCHAR(100) UNIQUE NOT NULL,"
                        + "phone VARCHAR(20) NOT NULL,"
                        + "password VARCHAR(100) NOT NULL,"
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS buses ("
                        + "bus_id VARCHAR(50) PRIMARY KEY,"
                        + "operator VARCHAR(100) NOT NULL,"
                        + "source VARCHAR(100) NOT NULL,"
                        + "destination VARCHAR(100) NOT NULL,"
                        + "departure_time VARCHAR(50) NOT NULL,"
                        + "bus_type VARCHAR(50) NOT NULL,"
                        + "total_seats INT NOT NULL,"
                        + "fare DOUBLE NOT NULL,"
                        + "amenities VARCHAR(255)"
                        + ")");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tickets ("
                        + "ticket_id VARCHAR(50) PRIMARY KEY,"
                        + "passenger_name VARCHAR(100) NOT NULL,"
                        + "bus_id VARCHAR(50) NOT NULL,"
                        + "seat_no VARCHAR(10) NOT NULL,"
                        + "pickup_point VARCHAR(100),"
                        + "total_fare DOUBLE NOT NULL,"
                        + "category VARCHAR(50),"
                        + "booking_date_time VARCHAR(50),"
                        + "status VARCHAR(20) DEFAULT 'CONFIRMED',"
                        + "FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE"
                        + ")");

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        stmt.executeUpdate("INSERT INTO users (full_name, email, phone, password) "
                                + "VALUES ('Administrator', 'admin@bus.com', '9876543210', 'admin123')");
                    }
                }

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM buses")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        stmt.executeUpdate("INSERT INTO buses VALUES "
                                + "('KT-101', 'Krisna Royal Sleeper (2+1)', 'Delhi', 'Jaipur', '06:00 AM', 'Volvo 9600 Luxury Sleeper', 20, 25.00, '❄️ AC, 📶 High-Speed WiFi, ⚡ USB, 🥤 Snacks'),"
                                + "('KT-102', 'Krisna Express Liner', 'Mumbai', 'Pune', '08:30 AM', 'Mercedes-Benz SuperCoach', 20, 18.50, '❄️ AC, 🥤 Free Mineral Water, 💺 Recliner'),"
                                + "('KT-103', 'Southern Crown Transit', 'Bangalore', 'Chennai', '10:00 PM', 'Scania Luxury Semi-Sleeper', 20, 22.00, '📶 WiFi, ❄️ AC, 🛌 Clean Blankets'),"
                                + "('KT-104', 'Highway King Cruiser', 'Hyderabad', 'Vijayawada', '02:00 PM', 'BharatBenz Glider AC', 20, 15.00, '❄️ AC, 📺 Personal Screen, ⚡ Charging'),"
                                + "('KT-105', 'Taj Heritage Express', 'Delhi', 'Agra', '07:15 AM', 'Volvo B11R Multi-Axle', 20, 12.00, '❄️ AC, 🥤 Free Water, 💺 Calf Support')");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Setup Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class Bus {
        String id, operator, source, destination, departureTime, busType, amenities;
        int totalSeats, availableSeats;
        double fare;

        Bus(String id, String operator, String source, String destination, String departureTime, String busType, int totalSeats, double fare, String amenities) {
            this.id = id;
            this.operator = operator;
            this.source = source;
            this.destination = destination;
            this.departureTime = departureTime;
            this.busType = busType;
            this.totalSeats = totalSeats;
            this.availableSeats = totalSeats;
            this.fare = fare;
            this.amenities = amenities;
        }

        @Override
        public String toString() {
            return id + " | " + operator + " (" + source + " -> " + destination + ") [$" + String.format("%.2f", fare) + " | " + availableSeats + " seats]";
        }
    }

    static class Ticket {
        String ticketId, passengerName, busId, seatNo, pickupPoint, bookingDateTime, category, status;
        double totalFare;

        Ticket(String ticketId, String passengerName, String busId, String seatNo, String pickupPoint, double totalFare, String category, String bookingDateTime) {
            this.ticketId = ticketId;
            this.passengerName = passengerName;
            this.busId = busId;
            this.seatNo = seatNo;
            this.pickupPoint = pickupPoint;
            this.totalFare = totalFare;
            this.category = category;
            this.bookingDateTime = bookingDateTime;
            this.status = "CONFIRMED";
        }
    }

    private final List<Bus> busList = new ArrayList<>();
    private final List<Ticket> ticketList = new ArrayList<>();

    private CardLayout mainCardLayout;
    private JPanel mainContainer;

    private CardLayout portalCardLayout;
    private JPanel portalCardsPanel;
    private final List<ModernNavButton> navButtons = new ArrayList<>();

    private JPanel busCardsContainer;
    private JTable ticketTable;
    private DefaultTableModel ticketTableModel;
    private JComboBox<String> sourceSearchCombo, destSearchCombo, sortCombo, pickupCombo, categoryCombo;
    private JComboBox<Bus> busSelectCombo;
    private JTextField nameField, loginUserField, regNameField, regEmailField, regPhoneField;
    private JPasswordField loginPassField, regPassField;
    private JLabel fareDisplayLbl, statRevenueLbl, statBookingsLbl, statBusesLbl, selectedSeatStatusLbl, userGreetingLbl, routeTimelineLbl;
    private JPanel seatGridPanel;

    private String selectedSeatNumber = "";
    private JButton currentlySelectedSeatButton = null;
    private String currentUser = "Guest Passenger";

    public Main() {
        setTitle("KRISNA TRAVELS - Luxury Intercity Transit & Sleeper Reservation");
        setMinimumSize(new Dimension(1180, 840));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeDatabase();

        mainCardLayout = new CardLayout();
        mainContainer = new JPanel(mainCardLayout);

        mainContainer.add(createFullScreenLoginPanel(), "LOGIN_PAGE");
        mainContainer.add(createRegistrationPanel(), "REGISTRATION_PAGE");
        mainContainer.add(createMainPortalPanel(), "MAIN_PORTAL");

        setContentPane(mainContainer);
        mainCardLayout.show(mainContainer, "LOGIN_PAGE");
    }

    // ================= ARTWORK & PICTURE PANELS =================

    // Sunset Mountain & Highway Visual with a Luxury Volvo Coach
    static class TravelHeroArtworkPanel extends JPanel {
        TravelHeroArtworkPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(480, 520));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Sunset Sky Gradient (Rich Coral Red -> Twilight Purple)
            GradientPaint sky = new GradientPaint(0, 0, new Color(225, 29, 72), 0, h, new Color(30, 27, 75));
            g2.setPaint(sky);
            g2.fillRoundRect(0, 0, w, h, 28, 28);

            // 2. Glowing Golden Sunset Sun
            g2.setColor(new Color(254, 240, 138, 180));
            g2.fillOval(w - 140, 40, 90, 90);
            g2.setColor(new Color(254, 240, 138, 70));
            g2.fillOval(w - 160, 20, 130, 130);

            // 3. Layered Mountain Silhouettes
            g2.setColor(new Color(76, 29, 149, 130));
            int[] mx1 = {0, w / 3, (w * 2) / 3, w, w, 0};
            int[] my1 = {h - 160, h - 230, h - 180, h - 250, h, h};
            g2.fillPolygon(mx1, my1, 6);

            g2.setColor(new Color(49, 46, 129, 210));
            int[] mx2 = {0, w / 4, w / 2, (w * 3) / 4, w, w, 0};
            int[] my2 = {h - 140, h - 190, h - 150, h - 200, h - 130, h, h};
            g2.fillPolygon(mx2, my2, 7);

            // 4. Highway Road
            g2.setColor(new Color(15, 23, 42));
            g2.fillRect(0, h - 140, w, 140);

            // Road Divider Yellow Lines
            g2.setColor(new Color(245, 158, 11, 200));
            for (int rx = 10; rx < w; rx += 45) {
                g2.fillRect(rx, h - 60, 25, 4);
            }

            // 5. Luxury Volvo Coach Bus (Rendered with Details)
            int bx = 40, by = h - 135;

            // Bus Body Shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(bx + 10, by + 50, 360, 20, 14, 14);

            // Bus Metallic Crimson Body
            GradientPaint busPaint = new GradientPaint(bx, by, new Color(239, 68, 68), bx, by + 65, new Color(153, 27, 27));
            g2.setPaint(busPaint);
            g2.fillRoundRect(bx, by, 350, 64, 22, 22);
            g2.fillRect(bx + 40, by, 310, 64);

            // Streamlined Roof AC Unit
            g2.setColor(new Color(248, 113, 113));
            g2.fillRoundRect(bx + 60, by - 6, 220, 10, 6, 6);

            // Panoramic Tinted Windows
            GradientPaint glass = new GradientPaint(bx, by + 12, new Color(30, 41, 59), bx, by + 34, new Color(15, 23, 42));
            g2.setPaint(glass);
            g2.fillRoundRect(bx + 12, by + 12, 325, 24, 8, 8);

            // Window Glass Pillars
            g2.setColor(new Color(255, 255, 255, 140));
            for (int wx = bx + 45; wx < bx + 330; wx += 42) {
                g2.drawLine(wx, by + 12, wx, by + 36);
            }

            // Glowing Dual LED Headlamps & Beams
            g2.setColor(new Color(254, 240, 138));
            g2.fillOval(bx + 2, by + 46, 12, 10);
            g2.setColor(new Color(254, 240, 138, 70));
            g2.fillArc(bx - 35, by + 30, 48, 48, 140, 75);

            // Rear Red Tail Lamp
            g2.setColor(new Color(239, 68, 68));
            g2.fillRect(bx + 345, by + 44, 5, 14);

            // Golden Brand Ribbon
            g2.setColor(new Color(245, 158, 11));
            g2.fillRect(bx + 30, by + 40, 290, 4);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            g2.drawString("★ KRISNA ROYAL SLEEPER EXPRESS ★", bx + 65, by + 54);

            // Double Axle Wheels with Chrome Alloys
            paintAlloyWheel(g2, bx + 55, by + 50);
            paintAlloyWheel(g2, bx + 265, by + 50);
            paintAlloyWheel(g2, bx + 298, by + 50);

            // 6. Floating Glassmorphism Badge
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillRoundRect(25, 25, 250, 68, 16, 16);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.drawRoundRect(25, 25, 250, 68, 16, 16);

            g2.setColor(new Color(254, 240, 138));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("⭐⭐⭐⭐⭐ 4.9 / 5 Rating", 40, 50);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(Color.WHITE);
            g2.drawString("50,000+ Happy Intercity Travelers", 40, 70);

            g2.dispose();
        }

        private void paintAlloyWheel(Graphics2D g2, int wx, int wy) {
            g2.setColor(new Color(15, 23, 42));
            g2.fillOval(wx, wy, 28, 28);
            g2.setColor(new Color(226, 232, 240));
            g2.fillOval(wx + 5, wy + 5, 18, 18);
            g2.setColor(new Color(71, 85, 105));
            g2.fillOval(wx + 9, wy + 9, 10, 10);
        }
    }

    // Top Scenic Twilight Travel Banner
    static class TwilightHeroHeader extends JPanel {
        TwilightHeroHeader() {
            setPreferredSize(new Dimension(0, 105));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint sky = new GradientPaint(0, 0, BRAND_NAVY, w, h, new Color(49, 46, 129));
            g2.setPaint(sky);
            g2.fillRect(0, 0, w, h);

            // Subtle Golden Horizon Line
            g2.setColor(new Color(245, 158, 11, 80));
            g2.fillRect(0, h - 4, w, 4);

            g2.dispose();
        }
    }

    // Modern Rounded Container
    static class ModernCard extends JPanel {
        private final int radius;
        private final Color bgColor;
        ModernCard(int radius, Color bg) {
            this.radius = radius;
            this.bgColor = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(BRAND_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Interactive Pill Navigation Button
    static class ModernNavButton extends JButton {
        private boolean isActive = false;

        ModernNavButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 24, 10, 24));
        }

        void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                // Vibrant Active Crimson Pill
                g2.setColor(BRAND_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                setForeground(Color.WHITE);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                setForeground(Color.WHITE);
            } else {
                setForeground(new Color(203, 213, 225));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 3D Recliner Leather Coach Seat
    static class LeatherSeatButton extends JButton {
        private final String seatCode;
        private final boolean isWindow;
        private boolean isSelectedSeat = false;
        private boolean isBookedSeat = false;

        LeatherSeatButton(String seatCode, boolean isWindow) {
            this.seatCode = seatCode;
            this.isWindow = isWindow;
            setPreferredSize(new Dimension(54, 58));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setSeatState(boolean booked, boolean selected) {
            this.isBookedSeat = booked;
            this.isSelectedSeat = selected;
            setEnabled(!booked);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color seatBaseColor, borderColor, textColor;

            if (isBookedSeat) {
                seatBaseColor = new Color(226, 232, 240);
                borderColor = new Color(203, 213, 225);
                textColor = new Color(148, 163, 184);
            } else if (isSelectedSeat) {
                seatBaseColor = BRAND_GOLD;
                borderColor = new Color(217, 119, 6);
                textColor = Color.WHITE;
            } else if (getModel().isRollover()) {
                seatBaseColor = new Color(254, 226, 226);
                borderColor = BRAND_RED;
                textColor = BRAND_RED;
            } else {
                seatBaseColor = new Color(240, 253, 244);
                borderColor = new Color(74, 222, 128);
                textColor = new Color(22, 101, 52);
            }

            // Headrest
            g2.setColor(borderColor);
            g2.fillRoundRect(w / 4, 2, w / 2, 10, 6, 6);

            // Cushion
            g2.setColor(seatBaseColor);
            g2.fillRoundRect(6, 12, w - 12, h - 16, 10, 10);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(6, 12, w - 12, h - 16, 10, 10);

            // Armrests
            g2.setColor(borderColor);
            g2.fillRoundRect(2, 20, 5, h - 26, 4, 4);
            g2.fillRoundRect(w - 7, 20, 5, h - 26, 4, 4);

            // Seat Code
            g2.setColor(textColor);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(seatCode, (w - fm.stringWidth(seatCode)) / 2, 32);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String typeStr = isWindow ? "WIN" : "AIS";
            g2.drawString(typeStr, (w - g2.getFontMetrics().stringWidth(typeStr)) / 2, 45);

            g2.dispose();
        }
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BRAND_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleTextField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BRAND_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(BRAND_TEXT);
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(BRAND_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setSelectionBackground(new Color(254, 226, 226));
        table.setSelectionForeground(BRAND_TEXT);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    // ================= SPLIT-SCREEN VISUAL LOGIN =================

    private JPanel createFullScreenLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BRAND_NAVY, getWidth(), getHeight(), new Color(30, 27, 75));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        ModernCard mainCard = new ModernCard(24, BRAND_CARD);
        mainCard.setLayout(new GridLayout(1, 2, 35, 0));
        mainCard.setBorder(new EmptyBorder(35, 40, 35, 40));

        // Left Half: Hand-Crafted Travel Art
        TravelHeroArtworkPanel heroArt = new TravelHeroArtworkPanel();

        // Right Half: Modern Clean Login Form
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLbl = new JLabel("Passenger Portal Sign In", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(BRAND_RED);

        JLabel subTitle = new JLabel("Welcome back! Enter credentials to manage trips.", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subTitle.setForeground(BRAND_MUTED);

        loginUserField = new JTextField(16);
        styleTextField(loginUserField);
        loginUserField.setText("admin@bus.com");

        loginPassField = new JPasswordField(16);
        styleTextField(loginPassField);
        loginPassField.setText("admin123");

        JButton loginBtn = createStyledButton("Authenticate & Access ➔", BRAND_RED);
        JButton registerNavBtn = createStyledButton("New Passenger? Register Here", BRAND_GREEN);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; card.add(titleLbl, gbc);
        gbc.gridy = 1; card.add(subTitle, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; card.add(createFieldLabel("Email/User:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; card.add(loginUserField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; card.add(createFieldLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; card.add(loginPassField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; card.add(loginBtn, gbc);
        gbc.gridy = 5; card.add(registerNavBtn, gbc);

        loginBtn.addActionListener(e -> executeLogin());
        registerNavBtn.addActionListener(e -> mainCardLayout.show(mainContainer, "REGISTRATION_PAGE"));

        mainCard.add(heroArt);
        mainCard.add(card);
        wrapper.add(mainCard);
        return wrapper;
    }

    private JPanel createRegistrationPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BRAND_NAVY, getWidth(), getHeight(), new Color(30, 27, 75));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        ModernCard card = new ModernCard(20, BRAND_CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(35, 45, 35, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLbl = new JLabel("Passenger Registration", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(BRAND_RED);

        regNameField = new JTextField(16);
        styleTextField(regNameField);
        regEmailField = new JTextField(16);
        styleTextField(regEmailField);
        regPhoneField = new JTextField(16);
        styleTextField(regPhoneField);
        regPassField = new JPasswordField(16);
        styleTextField(regPassField);

        JButton registerBtn = createStyledButton("Confirm & Register", BRAND_GREEN);
        JButton backLoginBtn = createStyledButton("Back to Sign In", new Color(100, 116, 139));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; card.add(titleLbl, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; card.add(createFieldLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; card.add(regNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; card.add(createFieldLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; card.add(regEmailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; card.add(createFieldLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; card.add(regPhoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; card.add(createFieldLabel("Set Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; card.add(regPassField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; card.add(registerBtn, gbc);
        gbc.gridy = 6; card.add(backLoginBtn, gbc);

        registerBtn.addActionListener(e -> executeRegistration());
        backLoginBtn.addActionListener(e -> mainCardLayout.show(mainContainer, "LOGIN_PAGE"));

        wrapper.add(card);
        return wrapper;
    }

    private void executeRegistration() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String pass = new String(regPassField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "INSERT INTO users (full_name, email, phone, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectDB()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Connection failed. Please retry.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, pass);
                pstmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Welcome " + name + "! Account created successfully.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            mainCardLayout.show(mainContainer, "LOGIN_PAGE");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registration Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeLogin() {
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword()).trim();

        String query = "SELECT full_name FROM users WHERE email = ? AND password = ?";
        try (Connection conn = connectDB()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Unable to establish connection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentUser = rs.getString("full_name");
                        if (userGreetingLbl != null) {
                            userGreetingLbl.setText("Passenger: " + currentUser);
                        }
                        mainCardLayout.show(mainContainer, "MAIN_PORTAL");
                        loadTicketsFromDB();
                        refreshBusData();
                        updateDashboardStats();
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid Email or Password!", "Authentication Failure", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Login failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= INSIDE PORTAL WITH LUXURY DESIGN =================

    private JPanel createMainPortalPanel() {
        JPanel portal = new JPanel(new BorderLayout());
        portal.setBackground(BRAND_BG);

        // Twilight Header Banner
        TwilightHeroHeader heroHeader = new TwilightHeroHeader();
        heroHeader.setLayout(new BorderLayout());
        heroHeader.setBorder(new EmptyBorder(14, 30, 14, 30));

        // Brand Banner
        JPanel brandBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandBlock.setOpaque(false);

        JLabel brandMain = new JLabel("KRISNA");
        brandMain.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandMain.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("EXPRESS");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandSub.setForeground(BRAND_GOLD);

        JLabel brandDesc = new JLabel("  |  Luxury Sleeper Fleet & Intercity Express");
        brandDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandDesc.setForeground(new Color(203, 213, 225));

        brandBlock.add(brandMain);
        brandBlock.add(brandSub);
        brandBlock.add(brandDesc);

        // User Pill & Sign Out
        JPanel userBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userBlock.setOpaque(false);

        userGreetingLbl = new JLabel("Passenger: " + currentUser);
        userGreetingLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userGreetingLbl.setForeground(new Color(254, 240, 138));

        JButton logoutBtn = createStyledButton("Sign Out", new Color(30, 41, 59));
        logoutBtn.addActionListener(e -> mainCardLayout.show(mainContainer, "LOGIN_PAGE"));

        userBlock.add(userGreetingLbl);
        userBlock.add(logoutBtn);

        heroHeader.add(brandBlock, BorderLayout.WEST);
        heroHeader.add(userBlock, BorderLayout.EAST);

        // Custom High-End Pill Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        navBar.setBackground(BRAND_NAVY_LIGHT);
        navBar.setBorder(new EmptyBorder(4, 25, 6, 25));

        ModernNavButton btnCoaches = new ModernNavButton("🚌 Available Luxury Coaches");
        ModernNavButton btnSeats = new ModernNavButton("💺 2x2 Interactive Seat Map");
        ModernNavButton btnBookings = new ModernNavButton("🎫 My Bookings & Boarding Pass");
        ModernNavButton btnAnalytics = new ModernNavButton("📊 Fleet Analytics");

        navButtons.add(btnCoaches);
        navButtons.add(btnSeats);
        navButtons.add(btnBookings);
        navButtons.add(btnAnalytics);

        btnCoaches.setActive(true);

        navBar.add(btnCoaches);
        navBar.add(btnSeats);
        navBar.add(btnBookings);
        navBar.add(btnAnalytics);

        // Card Container for views
        portalCardLayout = new CardLayout();
        portalCardsPanel = new JPanel(portalCardLayout);
        portalCardsPanel.setBackground(BRAND_BG);

        portalCardsPanel.add(createBusPanel(), "PAGE_COACHES");
        portalCardsPanel.add(createBookingPanel(), "PAGE_SEATS");
        portalCardsPanel.add(createHistoryPanel(), "PAGE_BOOKINGS");
        portalCardsPanel.add(createDashboardPanel(), "PAGE_ANALYTICS");

        btnCoaches.addActionListener(e -> switchPage("PAGE_COACHES", btnCoaches, () -> refreshBusData()));
        btnSeats.addActionListener(e -> switchPage("PAGE_SEATS", btnSeats, () -> renderSeatGrid()));
        btnBookings.addActionListener(e -> switchPage("PAGE_BOOKINGS", btnBookings, () -> loadTicketsFromDB()));
        btnAnalytics.addActionListener(e -> switchPage("PAGE_ANALYTICS", btnAnalytics, () -> updateDashboardStats()));

        JPanel combinedHeader = new JPanel(new BorderLayout());
        combinedHeader.add(heroHeader, BorderLayout.NORTH);
        combinedHeader.add(navBar, BorderLayout.SOUTH);

        portal.add(combinedHeader, BorderLayout.NORTH);
        portal.add(portalCardsPanel, BorderLayout.CENTER);
        return portal;
    }

    private void switchPage(String pageName, ModernNavButton activeBtn, Runnable onSwitchAction) {
        for (ModernNavButton btn : navButtons) {
            btn.setActive(btn == activeBtn);
        }
        portalCardLayout.show(portalCardsPanel, pageName);
        if (onSwitchAction != null) {
            onSwitchAction.run();
        }
    }

    // ================= REAL TRAVEL BUS CARDS =================

    private JPanel createBusPanel() {
        JPanel mainWrapper = new JPanel(new BorderLayout(12, 12));
        mainWrapper.setBackground(BRAND_BG);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(15, 25, 5, 25));

        JPanel offersSection = createOffersSection();

        ModernCard searchCard = new ModernCard(12, BRAND_CARD);
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        sourceSearchCombo = new JComboBox<>();
        destSearchCombo = new JComboBox<>();
        sortCombo = new JComboBox<>(new String[]{"Sort by: Default", "Fare: Low to High", "Fare: High to Low", "Most Seats Available"});

        populateRouteCombos();

        JButton searchBtn = createStyledButton("Search Buses", BRAND_RED);
        searchBtn.addActionListener(e -> filterBusRoutes());

        searchCard.add(createFieldLabel("From:"));
        searchCard.add(sourceSearchCombo);
        searchCard.add(createFieldLabel("To:"));
        searchCard.add(destSearchCombo);
        searchCard.add(createFieldLabel("Sort:"));
        searchCard.add(sortCombo);
        searchCard.add(searchBtn);

        topContainer.add(offersSection, BorderLayout.NORTH);
        topContainer.add(searchCard, BorderLayout.SOUTH);

        busCardsContainer = new JPanel();
        busCardsContainer.setLayout(new BoxLayout(busCardsContainer, BoxLayout.Y_AXIS));
        busCardsContainer.setBackground(BRAND_BG);

        JScrollPane scrollPane = new JScrollPane(busCardsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 25));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainWrapper.add(topContainer, BorderLayout.NORTH);
        mainWrapper.add(scrollPane, BorderLayout.CENTER);
        return mainWrapper;
    }

    private JPanel createBusCard(Bus b) {
        ModernCard card = new ModernCard(16, BRAND_CARD);
        card.setLayout(new BorderLayout(15, 15));
        card.setMaximumSize(new Dimension(3000, 125));
        card.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Left Section: Operator & Rating
        JPanel left = new JPanel(new GridLayout(3, 1, 3, 3));
        left.setOpaque(false);

        JLabel opLbl = new JLabel(b.operator);
        opLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        opLbl.setForeground(BRAND_NAVY);

        JLabel coachLbl = new JLabel(b.busType);
        coachLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coachLbl.setForeground(BRAND_MUTED);

        JLabel ratingLbl = new JLabel("⭐⭐⭐⭐⭐ 4.9 • Executive Fleet");
        ratingLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLbl.setForeground(BRAND_GOLD);

        left.add(opLbl);
        left.add(coachLbl);
        left.add(ratingLbl);

        // Center Section: Route Timeline & Amenities
        JPanel center = new JPanel(new GridLayout(2, 1, 5, 5));
        center.setOpaque(false);

        JLabel routeTimeline = new JLabel(b.source + " (" + b.departureTime + ")  ────────▶  " + b.destination + "  (Direct Express)");
        routeTimeline.setFont(new Font("Segoe UI", Font.BOLD, 13));
        routeTimeline.setForeground(BRAND_TEXT);

        JLabel amenitiesLbl = new JLabel("Amenities: " + b.amenities);
        amenitiesLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        amenitiesLbl.setForeground(new Color(15, 118, 110));

        center.add(routeTimeline);
        center.add(amenitiesLbl);

        // Right Section: Fare & Book Button
        JPanel right = new JPanel(new GridLayout(2, 1, 4, 4));
        right.setOpaque(false);

        JLabel fareLbl = new JLabel("$" + String.format("%.2f", b.fare) + " / seat", SwingConstants.RIGHT);
        fareLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fareLbl.setForeground(BRAND_GREEN);

        JButton bookSeatBtn = createStyledButton("Select Seats ➔", BRAND_RED);
        bookSeatBtn.addActionListener(e -> {
            busSelectCombo.setSelectedItem(b);
            switchPage("PAGE_SEATS", navButtons.get(1), () -> renderSeatGrid());
        });

        right.add(fareLbl);
        right.add(bookSeatBtn);

        card.add(left, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private JPanel createOffersSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel offersHeader = new JLabel("EXCLUSIVE PASSENGER PROMOTIONS");
        offersHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        offersHeader.setForeground(BRAND_TEXT);

        JPanel cardsHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        cardsHolder.setOpaque(false);

        cardsHolder.add(createOfferCard("FIRST RIDE", "Flat 20% OFF on first trip", "KRISNA20", BRAND_RED));
        cardsHolder.add(createOfferCard("VOLVO PROMO", "Save $15 instantly on Volvo", "SUPERVOLVO", BRAND_GREEN));
        cardsHolder.add(createOfferCard("STUDENT SPECIAL", "Flat 30% OFF for students", "STUDENT30", new Color(99, 102, 241)));
        cardsHolder.add(createOfferCard("WEEKEND RUSH", "15% cashback on round trips", "WEEKEND15", BRAND_GOLD));

        JScrollPane scroll = new JScrollPane(cardsHolder, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        panel.add(offersHeader, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOfferCard(String title, String desc, String code, Color accentColor) {
        ModernCard card = new ModernCard(12, BRAND_CARD);
        card.setLayout(new BorderLayout(8, 8));
        card.setPreferredSize(new Dimension(240, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(accentColor);

        JLabel descLbl = new JLabel("<html>" + desc + "</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(BRAND_MUTED);

        JButton copyBtn = new JButton("Copy Code: " + code);
        copyBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        copyBtn.setBackground(new Color(241, 245, 249));
        copyBtn.setForeground(BRAND_TEXT);
        copyBtn.setFocusPainted(false);
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
            JOptionPane.showMessageDialog(this, "Promo Code '" + code + "' Copied to Clipboard!", "Offer Applied", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);
        card.add(copyBtn, BorderLayout.SOUTH);
        return card;
    }

    // ================= REALISTIC CABIN FLOORPLAN =================

    private JPanel createBookingPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BRAND_BG);

        ModernCard mainCard = new ModernCard(16, BRAND_CARD);
        mainCard.setLayout(new BorderLayout(25, 25));
        mainCard.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Left Panel: Real Coach Layout with Windshield & Driver Cabin
        JPanel leftPanel = new JPanel(new BorderLayout(12, 12));
        leftPanel.setOpaque(false);

        // Windshield & Driver Area
        JPanel driverCabinPanel = new JPanel(new BorderLayout());
        driverCabinPanel.setBackground(new Color(241, 245, 249));
        driverCabinPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel doorLabel = new JLabel("🚪 Passenger Boarding Door");
        doorLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        doorLabel.setForeground(BRAND_MUTED);

        JLabel steeringLabel = new JLabel("🕹️ Driver Cockpit & Steering");
        steeringLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        steeringLabel.setForeground(BRAND_RED);

        driverCabinPanel.add(doorLabel, BorderLayout.WEST);
        driverCabinPanel.add(steeringLabel, BorderLayout.EAST);

        seatGridPanel = new JPanel(new GridLayout(5, 5, 8, 8)); // 5 columns (Window, Aisle, [WALKWAY], Aisle, Window)
        seatGridPanel.setBackground(BRAND_CARD);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", new Color(74, 222, 128)));
        legendPanel.add(createLegendItem("Selected Seat", BRAND_GOLD));
        legendPanel.add(createLegendItem("Booked", new Color(203, 213, 225)));

        selectedSeatStatusLbl = new JLabel("Status: No Seat Selected", SwingConstants.CENTER);
        selectedSeatStatusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedSeatStatusLbl.setForeground(BRAND_RED);

        leftPanel.add(driverCabinPanel, BorderLayout.NORTH);
        leftPanel.add(seatGridPanel, BorderLayout.CENTER);

        JPanel southLeft = new JPanel(new GridLayout(2, 1, 4, 4));
        southLeft.setOpaque(false);
        southLeft.add(legendPanel);
        southLeft.add(selectedSeatStatusLbl);
        leftPanel.add(southLeft, BorderLayout.SOUTH);

        // Right Panel: Form details
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formTitle = new JLabel("Passenger Details & Checkout");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(BRAND_RED);

        nameField = new JTextField(15);
        styleTextField(nameField);

        busSelectCombo = new JComboBox<>();
        busSelectCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        busSelectCombo.addActionListener(e -> {
            renderSeatGrid();
            updateRouteTimeline();
        });

        routeTimelineLbl = new JLabel("Route: Select a bus to view timeline");
        routeTimelineLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        routeTimelineLbl.setForeground(BRAND_RED);

        pickupCombo = new JComboBox<>(new String[]{"Central ISBT Terminal (Platform 4)", "City Center Hub Junction", "Express Highway Toll Plaza"});
        pickupCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        categoryCombo = new JComboBox<>(new String[]{"Standard Regular", "Student Concession (30% OFF)", "Senior Citizen (40% OFF)", "Women Passenger (20% OFF)"});
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryCombo.addActionListener(e -> handleCategoryDiscount());

        fareDisplayLbl = new JLabel("$0.00");
        fareDisplayLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        fareDisplayLbl.setForeground(BRAND_GREEN);

        JButton checkoutBtn = createStyledButton("Proceed to Checkout & Pay 💳", BRAND_RED);
        checkoutBtn.addActionListener(e -> showPaymentGatewayDialog());

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; rightPanel.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; rightPanel.add(createFieldLabel("Choose Bus:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; rightPanel.add(busSelectCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; rightPanel.add(routeTimelineLbl, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 3; rightPanel.add(createFieldLabel("Passenger Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; rightPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; rightPanel.add(createFieldLabel("Boarding Station:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; rightPanel.add(pickupCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; rightPanel.add(createFieldLabel("Discount Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; rightPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 6; rightPanel.add(createFieldLabel("Total Payable:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; rightPanel.add(fareDisplayLbl, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; rightPanel.add(checkoutBtn, gbc);

        mainCard.add(leftPanel, BorderLayout.WEST);
        mainCard.add(rightPanel, BorderLayout.EAST);

        JScrollPane containerScroll = new JScrollPane(mainCard);
        containerScroll.setBorder(null);

        wrapper.add(containerScroll);
        return wrapper;
    }

    private void updateRouteTimeline() {
        Bus selectedBus = (Bus) busSelectCombo.getSelectedItem();
        if (selectedBus != null) {
            routeTimelineLbl.setText("<html><b>Live Timeline:</b> "
                    + selectedBus.source + " (" + selectedBus.departureTime + ") ──▶ Expressway Hub ──▶ "
                    + selectedBus.destination + "</html>");
        }
    }

    private void handleCategoryDiscount() {
        String selected = (String) categoryCombo.getSelectedItem();
        if (selected == null) return;

        if (selected.contains("Student")) {
            String cardNo = JOptionPane.showInputDialog(this, "Enter Student ID Card Number:", "Student Verification", JOptionPane.QUESTION_MESSAGE);
            if (cardNo == null || cardNo.trim().isEmpty()) {
                categoryCombo.setSelectedIndex(0);
                return;
            }
        } else if (selected.contains("Senior")) {
            String age = JOptionPane.showInputDialog(this, "Enter Passenger Age (60+ for concession):", "Senior Verification", JOptionPane.QUESTION_MESSAGE);
            if (age == null) {
                categoryCombo.setSelectedIndex(0);
                return;
            }
            try {
                if (Integer.parseInt(age.trim()) < 60) {
                    JOptionPane.showMessageDialog(this, "Ineligible for Senior Discount.", "Notice", JOptionPane.WARNING_MESSAGE);
                    categoryCombo.setSelectedIndex(0);
                    return;
                }
            } catch (NumberFormatException nfe) {
                categoryCombo.setSelectedIndex(0);
                return;
            }
        }
        calculateFare();
    }

    private void calculateFare() {
        Bus selectedBus = (Bus) busSelectCombo.getSelectedItem();
        if (selectedBus == null) return;

        double baseFare = selectedBus.fare;
        String category = (String) categoryCombo.getSelectedItem();

        if (category != null) {
            if (category.contains("Student")) baseFare *= 0.70;
            else if (category.contains("Senior")) baseFare *= 0.60;
            else if (category.contains("Women")) baseFare *= 0.80;
        }

        fareDisplayLbl.setText(String.format("$%.2f", baseFare));
    }

    private void renderSeatGrid() {
        seatGridPanel.removeAll();
        selectedSeatNumber = "";
        currentlySelectedSeatButton = null;
        selectedSeatStatusLbl.setText("Status: No Seat Selected");

        Bus selectedBus = (Bus) busSelectCombo.getSelectedItem();
        if (selectedBus == null) return;

        calculateFare();

        int seatCounter = 1;
        int totalRows = selectedBus.totalSeats / 4;

        for (int r = 1; r <= totalRows; r++) {
            for (int c = 1; c <= 5; c++) {
                if (c == 3) {
                    // Central Aisle Walkway
                    JLabel aisleLbl = new JLabel("│ AISLE │", SwingConstants.CENTER);
                    aisleLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    aisleLbl.setForeground(new Color(203, 213, 225));
                    seatGridPanel.add(aisleLbl);
                } else {
                    String seatNo = "S" + seatCounter;
                    boolean isWindow = (c == 1 || c == 5);
                    LeatherSeatButton seatBtn = new LeatherSeatButton(seatNo, isWindow);

                    boolean isBooked = isSeatBooked(selectedBus.id, seatNo);
                    seatBtn.setSeatState(isBooked, false);

                    if (!isBooked) {
                        seatBtn.addActionListener(e -> {
                            if (currentlySelectedSeatButton != null && currentlySelectedSeatButton instanceof LeatherSeatButton) {
                                ((LeatherSeatButton) currentlySelectedSeatButton).setSeatState(false, false);
                            }
                            currentlySelectedSeatButton = seatBtn;
                            seatBtn.setSeatState(false, true);
                            selectedSeatNumber = seatNo;
                            selectedSeatStatusLbl.setText("Status: Selected Seat " + seatNo + (isWindow ? " (Window View)" : " (Aisle Access)"));
                        });
                    }
                    seatGridPanel.add(seatBtn);
                    seatCounter++;
                }
            }
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private boolean isSeatBooked(String busId, String seatNo) {
        for (Ticket t : ticketList) {
            if (t.busId.equals(busId) && t.seatNo.equals(seatNo) && "CONFIRMED".equalsIgnoreCase(t.status)) {
                return true;
            }
        }
        return false;
    }

    // ================= SECURE CHECKOUT MODAL =================

    private void showPaymentGatewayDialog() {
        Bus selectedBus = (Bus) busSelectCombo.getSelectedItem();
        String passengerName = nameField.getText().trim();
        String pickup = (String) pickupCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();

        if (selectedBus == null || passengerName.isEmpty() || selectedSeatNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please:\n1. Choose a bus\n2. Enter passenger name\n3. Click on an available green seat (e.g. S1)",
                    "Missing Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double fare = selectedBus.fare;
        if (category != null) {
            if (category.contains("Student")) fare *= 0.70;
            else if (category.contains("Senior")) fare *= 0.60;
            else if (category.contains("Women")) fare *= 0.80;
        }

        JDialog checkoutModal = new JDialog(this, "Krisna Travels - Secure Checkout", true);
        checkoutModal.setSize(520, 520);
        checkoutModal.setLocationRelativeTo(this);
        checkoutModal.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BRAND_NAVY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLbl = new JLabel("🔒 256-Bit Encrypted Secure Checkout");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        headerLbl.setForeground(Color.WHITE);
        header.add(headerLbl);

        JTabbedPane payTabs = new JTabbedPane();
        payTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: UPI & QR Code
        JPanel upiPanel = new JPanel(new BorderLayout(10, 10));
        upiPanel.setBackground(Color.WHITE);
        upiPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.BLACK);
                for (int x = 20; x < 160; x += 10) {
                    for (int y = 20; y < 160; y += 10) {
                        if ((x * y) % 7 == 0 || (x < 60 && y < 60) || (x > 110 && y < 60) || (x < 60 && y > 110)) {
                            g2.fillRect(x, y, 9, 9);
                        }
                    }
                }
            }
        };
        qrCanvas.setPreferredSize(new Dimension(180, 180));

        JLabel upiInstruction = new JLabel("<html><center>Scan QR using any UPI App<br><b>GPay • PhonePe • Paytm • BHIM</b><br><br><span style='color:green;'>Amount: $" + String.format("%.2f", fare) + "</span></center></html>", SwingConstants.CENTER);
        upiInstruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        upiPanel.add(qrCanvas, BorderLayout.CENTER);
        upiPanel.add(upiInstruction, BorderLayout.SOUTH);

        // Tab 2: Credit / Debit Card
        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField cardNum = new JTextField("4532 •••• •••• 8891");
        styleTextField(cardNum);
        JTextField cardExpiry = new JTextField("12/28");
        styleTextField(cardExpiry);
        JTextField cardCVV = new JTextField("982");
        styleTextField(cardCVV);

        cardPanel.add(new JLabel("Card Number:"));
        cardPanel.add(cardNum);
        cardPanel.add(new JLabel("Valid Thru (MM/YY):"));
        cardPanel.add(cardExpiry);
        cardPanel.add(new JLabel("CVV Security Code:"));
        cardPanel.add(cardCVV);
        cardPanel.add(new JLabel("Amount Payable:"));
        JLabel amtLbl = new JLabel("$" + String.format("%.2f", fare));
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        amtLbl.setForeground(BRAND_GREEN);
        cardPanel.add(amtLbl);

        payTabs.addTab(" 📱 UPI / QR Code ", upiPanel);
        payTabs.addTab(" 💳 Debit / Credit Card ", cardPanel);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(241, 245, 249));

        JButton cancelBtn = createStyledButton("Cancel", new Color(100, 116, 139));
        JButton payBtn = createStyledButton("Authorize & Pay $" + String.format("%.2f", fare), BRAND_GREEN);

        cancelBtn.addActionListener(ev -> checkoutModal.dispose());

        double finalFare = fare;
        payBtn.addActionListener(ev -> {
            payBtn.setEnabled(false);
            payBtn.setText("Authorizing Transaction...");
            Timer t = new Timer(600, timerEv -> {
                checkoutModal.dispose();
                executeBookingRecord(selectedBus, passengerName, pickup, category, finalFare);
            });
            t.setRepeats(false);
            t.start();
        });

        footer.add(cancelBtn);
        footer.add(payBtn);

        checkoutModal.add(header, BorderLayout.NORTH);
        checkoutModal.add(payTabs, BorderLayout.CENTER);
        checkoutModal.add(footer, BorderLayout.SOUTH);
        checkoutModal.setVisible(true);
    }

    private void executeBookingRecord(Bus selectedBus, String passengerName, String pickup, String category, double fare) {
        String ticketId = "KT-" + (System.currentTimeMillis() % 1000000);
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "INSERT INTO tickets (ticket_id, passenger_name, bus_id, seat_no, pickup_point, total_fare, category, booking_date_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
        try (Connection conn = connectDB()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed! Please check connection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, ticketId);
                pstmt.setString(2, passengerName);
                pstmt.setString(3, selectedBus.id);
                pstmt.setString(4, selectedSeatNumber);
                pstmt.setString(5, pickup);
                pstmt.setDouble(6, fare);
                pstmt.setString(7, category);
                pstmt.setString(8, dateTime);
                int rows = pstmt.executeUpdate();
                if (rows == 1) {
                    JOptionPane.showMessageDialog(this,
                            "🎉 Payment Approved & Ticket Issued!\n\n"
                                    + "Ticket ID: " + ticketId + "\n"
                                    + "Passenger: " + passengerName + "\n"
                                    + "Bus: " + selectedBus.operator + "\n"
                                    + "Seat: " + selectedSeatNumber + "\n"
                                    + "Total Paid: $" + String.format("%.2f", fare),
                            "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

                    nameField.setText("");
                    selectedSeatNumber = "";
                    currentlySelectedSeatButton = null;

                    loadTicketsFromDB();
                    refreshBusData();
                    updateDashboardStats();
                    renderSeatGrid();

                    switchPage("PAGE_BOOKINGS", navButtons.get(2), () -> loadTicketsFromDB());
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= BOARDING PASS VIEW =================

    private JPanel createHistoryPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BRAND_BG);
        wrapper.setBorder(new EmptyBorder(15, 25, 15, 25));

        ticketTableModel = new DefaultTableModel(new String[]{"Ticket ID", "Passenger Name", "Bus ID", "Seat No", "Category", "Fare Paid ($)", "Date & Time", "Status"}, 0);
        ticketTable = new JTable(ticketTableModel);
        styleTable(ticketTable);

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BRAND_BORDER, 1));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setOpaque(false);

        JButton viewPassBtn = createStyledButton("🎫 View Luxury Boarding Pass", BRAND_RED);
        JButton refreshBtn = createStyledButton("🔄 Refresh List", new Color(79, 70, 229));
        JButton cancelBtn = createStyledButton("Cancel Selected Ticket", new Color(225, 29, 72));
        JButton printBtn = createStyledButton("Export Receipt (.txt)", new Color(30, 41, 59));

        viewPassBtn.addActionListener(e -> showLuxuryBoardingPass());
        refreshBtn.addActionListener(e -> {
            loadTicketsFromDB();
            JOptionPane.showMessageDialog(this, "Bookings list updated! Total " + ticketList.size() + " active ticket(s).", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });
        cancelBtn.addActionListener(e -> cancelTicket());
        printBtn.addActionListener(e -> exportReceipt());

        actionPanel.add(viewPassBtn);
        actionPanel.add(refreshBtn);
        actionPanel.add(cancelBtn);
        actionPanel.add(printBtn);

        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(actionPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private void showLuxuryBoardingPass() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please click on a ticket in the table first to view its Boarding Pass!", "Select Ticket", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedRow = ticketTable.convertRowIndexToModel(selectedRow);
        String ticketId = (String) ticketTableModel.getValueAt(selectedRow, 0);
        String name = (String) ticketTableModel.getValueAt(selectedRow, 1);
        String busId = (String) ticketTableModel.getValueAt(selectedRow, 2);
        String seatNo = (String) ticketTableModel.getValueAt(selectedRow, 3);
        String fare = (String) ticketTableModel.getValueAt(selectedRow, 5);
        String date = (String) ticketTableModel.getValueAt(selectedRow, 6);

        JDialog passModal = new JDialog(this, "E-Boarding Pass - Krisna Travels", true);
        passModal.setSize(560, 420);
        passModal.setLocationRelativeTo(this);
        passModal.setLayout(new BorderLayout());

        JPanel passCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(15, 15, w - 30, h - 30, 20, 20);

                g2.setColor(BRAND_RED);
                g2.fillRoundRect(15, 15, w - 30, 70, 20, 20);
                g2.fillRect(15, 55, w - 30, 30);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("KRISNA TRAVELS LUXURY BOARDING PASS", 35, 50);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString("CONFIRMED DIGITAL TRANSIT PASS • OFFICIAL BOARDING PASS", 35, 70);

                g2.setColor(BRAND_TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("PASSENGER NAME:", 40, 120);
                g2.drawString("BUS ID & COACH:", 40, 160);
                g2.drawString("BOOKING DATE & TIME:", 40, 200);

                g2.drawString("SEAT NO:", 320, 120);
                g2.drawString("FARE PAID:", 320, 160);
                g2.drawString("TICKET ID:", 320, 200);

                g2.setColor(BRAND_RED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                g2.drawString(name, 40, 140);
                g2.drawString(busId + " (Luxury Sleeper)", 40, 180);
                g2.drawString(date, 40, 220);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2.setColor(BRAND_GOLD);
                g2.drawString(seatNo, 320, 142);
                g2.setColor(BRAND_GREEN);
                g2.drawString(fare, 320, 182);
                g2.setColor(BRAND_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString(ticketId, 320, 220);

                Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
                g2.setStroke(dashed);
                g2.setColor(BRAND_BORDER);
                g2.drawLine(25, 255, w - 25, 255);

                g2.setColor(BRAND_BG);
                g2.fillOval(5, 245, 20, 20);
                g2.fillOval(w - 25, 245, 20, 20);

                // Barcode
                g2.setColor(Color.BLACK);
                for (int i = 40; i < w - 40; i += 7) {
                    int barW = (i % 3 == 0) ? 3 : 1;
                    g2.fillRect(i, 275, barW, 40);
                }
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g2.drawString("* " + ticketId + " * " + seatNo + " * VERIFIED PASS *", 140, 330);

                g2.dispose();
            }
        };
        passCard.setBackground(BRAND_BG);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton closeBtn = createStyledButton("Close Pass", BRAND_RED);
        closeBtn.addActionListener(ev -> passModal.dispose());
        footer.add(closeBtn);

        passModal.add(passCard, BorderLayout.CENTER);
        passModal.add(footer, BorderLayout.SOUTH);
        passModal.setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel wrapper = new JPanel(new GridLayout(1, 3, 20, 20));
        wrapper.setBackground(BRAND_BG);
        wrapper.setBorder(new EmptyBorder(30, 30, 30, 30));

        statRevenueLbl = new JLabel("$0.00", SwingConstants.CENTER);
        statBookingsLbl = new JLabel("0", SwingConstants.CENTER);
        statBusesLbl = new JLabel("0", SwingConstants.CENTER);

        wrapper.add(createStatCard("Total Revenue Generated", statRevenueLbl, BRAND_GREEN));
        wrapper.add(createStatCard("Active Confirmed Bookings", statBookingsLbl, BRAND_RED));
        wrapper.add(createStatCard("Active Fleet Routes", statBusesLbl, new Color(107, 114, 128)));

        return wrapper;
    }

    private JPanel createStatCard(String titleText, JLabel valueLabel, Color borderAccent) {
        ModernCard card = new ModernCard(16, BRAND_CARD);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, borderAccent),
                new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(BRAND_TEXT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(borderAccent);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(14, 14));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(box);
        p.add(lbl);
        return p;
    }

    private void populateRouteCombos() {
        Set<String> sources = new HashSet<>();
        Set<String> destinations = new HashSet<>();

        sources.add("All Sources");
        destinations.add("All Destinations");

        for (Bus b : busList) {
            sources.add(b.source);
            destinations.add(b.destination);
        }

        sourceSearchCombo.setModel(new DefaultComboBoxModel<>(sources.toArray(new String[0])));
        destSearchCombo.setModel(new DefaultComboBoxModel<>(destinations.toArray(new String[0])));
    }

    private void filterBusRoutes() {
        String src = (String) sourceSearchCombo.getSelectedItem();
        String dest = (String) destSearchCombo.getSelectedItem();
        String sortOption = (String) sortCombo.getSelectedItem();

        List<Bus> filtered = new ArrayList<>();
        for (Bus b : busList) {
            boolean matchSrc = "All Sources".equals(src) || b.source.equals(src);
            boolean matchDest = "All Destinations".equals(dest) || b.destination.equals(dest);

            if (matchSrc && matchDest) {
                filtered.add(b);
            }
        }

        if ("Fare: Low to High".equals(sortOption)) {
            filtered.sort(Comparator.comparingDouble(o -> o.fare));
        } else if ("Fare: High to Low".equals(sortOption)) {
            filtered.sort((o1, o2) -> Double.compare(o2.fare, o1.fare));
        } else if ("Most Seats Available".equals(sortOption)) {
            filtered.sort((o1, o2) -> Integer.compare(o2.availableSeats, o1.availableSeats));
        }

        busCardsContainer.removeAll();
        for (Bus b : filtered) {
            busCardsContainer.add(createBusCard(b));
            busCardsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        busCardsContainer.revalidate();
        busCardsContainer.repaint();
    }

    private void refreshBusData() {
        busList.clear();
        String query = "SELECT * FROM buses ORDER BY bus_id";

        try (Connection conn = connectDB()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String bId = rs.getString("bus_id");
                    int total = rs.getInt("total_seats");

                    int booked = 0;
                    for (Ticket t : ticketList) {
                        if (t.busId.equals(bId) && "CONFIRMED".equalsIgnoreCase(t.status)) {
                            booked++;
                        }
                    }

                    Bus b = new Bus(
                            bId, rs.getString("operator"),
                            rs.getString("source"), rs.getString("destination"),
                            rs.getString("departure_time"), rs.getString("bus_type"),
                            total, rs.getDouble("fare"),
                            rs.getString("amenities"));

                    b.availableSeats = Math.max(0, total - booked);
                    busList.add(b);
                }
            }
            busSelectCombo.setModel(new DefaultComboBoxModel<>(busList.toArray(new Bus[0])));
            populateRouteCombos();
            updateRouteTimeline();
            filterBusRoutes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load buses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTicketsFromDB() {
        ticketList.clear();
        ticketTableModel.setRowCount(0);
        String sql = "SELECT * FROM tickets ORDER BY booking_date_time DESC";

        try (Connection conn = connectDB()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Ticket t = new Ticket(
                            rs.getString("ticket_id"), rs.getString("passenger_name"),
                            rs.getString("bus_id"), rs.getString("seat_no"),
                            rs.getString("pickup_point"), rs.getDouble("total_fare"),
                            rs.getString("category"), rs.getString("booking_date_time"));
                    t.status = rs.getString("status");
                    ticketList.add(t);
                    ticketTableModel.addRow(new Object[]{
                            t.ticketId, t.passengerName,
                            t.busId, t.seatNo, t.category,
                            String.format("$%.2f", t.totalFare),
                            t.bookingDateTime, t.status
                    });
                }
            }
            ticketTableModel.fireTableDataChanged();
            if (ticketTable != null) {
                ticketTable.revalidate();
                ticketTable.repaint();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load tickets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelTicket() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table to cancel.", "No Ticket Selected", JOptionPane.ERROR_MESSAGE);
            return;
        }
        selectedRow = ticketTable.convertRowIndexToModel(selectedRow);
        String ticketId = (String) ticketTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel Ticket ID: " + ticketId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM tickets WHERE ticket_id = ?";
        try (Connection conn = connectDB()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, ticketId);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Ticket cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            loadTicketsFromDB();
            refreshBusData();
            updateDashboardStats();
            renderSeatGrid();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cancellation failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReceipt() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket to export receipt.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ticketId = (String) ticketTableModel.getValueAt(selectedRow, 0);
        String name = (String) ticketTableModel.getValueAt(selectedRow, 1);
        String busId = (String) ticketTableModel.getValueAt(selectedRow, 2);
        String seatNo = (String) ticketTableModel.getValueAt(selectedRow, 3);
        String fare = (String) ticketTableModel.getValueAt(selectedRow, 5);
        String date = (String) ticketTableModel.getValueAt(selectedRow, 6);

        try {
            File file = new File("Ticket_" + ticketId + ".txt");
            FileWriter writer = new FileWriter(file);
            writer.write("====================================================\n");
            writer.write("             KRISNA TRAVELS E-TICKET RECEIPT        \n");
            writer.write("====================================================\n");
            writer.write("Ticket ID       : " + ticketId + "\n");
            writer.write("Passenger Name  : " + name + "\n");
            writer.write("Bus Reference   : " + busId + "\n");
            writer.write("Seat Number     : " + seatNo + "\n");
            writer.write("Total Fare Paid : " + fare + "\n");
            writer.write("Issue Date/Time : " + date + "\n");
            writer.write("Booking Status  : CONFIRMED (Verified)\n");
            writer.write("====================================================\n");
            writer.write("Thank you for traveling with Krisna Travels!\n");
            writer.close();

            JOptionPane.showMessageDialog(this, "Receipt Saved Successfully as:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to Export Receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDashboardStats() {
        double totalRev = 0;
        for (Ticket t : ticketList) {
            totalRev += t.totalFare;
        }
        statRevenueLbl.setText(String.format("$%.2f", totalRev));
        statBookingsLbl.setText(String.valueOf(ticketList.size()));
        statBusesLbl.setText(String.valueOf(busList.size()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}