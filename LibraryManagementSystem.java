
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

class Book {
    String isbn, title, author, topic;
    int availableCopies;

    Book(String isbn, String title, String author, String topic, int copies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.topic = topic;
        this.availableCopies = copies;
    }
}

class Student {
    String id, name, isbn;

    Student(String id, String name, String isbn) {
        this.id = id;
        this.name = name;
        this.isbn = isbn;
    }
}

class TopicNode {
    String topic;
    ArrayList<Book> books = new ArrayList<>();
    TopicNode left, right;

    TopicNode(String topic) {
        this.topic = topic;
    }
}

public class LibraryManagementSystem extends JFrame {

    HashMap<String, Book> books = new HashMap<>();
    HashMap<String, Student> issuedBooks = new HashMap<>();
    HashMap<String, Queue<Student>> waitingLists = new HashMap<>();

    Stack<String> recentlyReturned = new Stack<>();
    ArrayList<Book> bookList = new ArrayList<>();

    TopicNode root;

    Color navy = new Color(27, 39, 69);
    Color blue = new Color(63, 102, 220);
    Color background = new Color(245, 247, 251);
    Color white = Color.WHITE;
    Color green = new Color(35, 145, 100);

    CardLayout cardLayout = new CardLayout();
    JPanel content = new JPanel(cardLayout);

    DefaultTableModel tableModel;
    JTable bookTable;
    JLabel totalLabel, issuedLabel, waitingLabel, availableLabel;
    JTextField searchField;

    Font normal = new Font("SansSerif", Font.PLAIN, 14);
    Font bold = new Font("SansSerif", Font.BOLD, 22);

    public LibraryManagementSystem() {
        setTitle("BookHub | Library Management System");
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(background);

        add(createSidebar(), BorderLayout.WEST);
        add(createWorkspace(), BorderLayout.CENTER);

        refreshAll();
        showPage("Dashboard");
    }

    JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBackground(navy);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        JLabel logo = new JLabel("  BOOKHUB");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setForeground(white);
        sidebar.add(logo);

        JLabel sub = new JLabel("  Library Management");
        sub.setForeground(new Color(195, 205, 225));
        sub.setBorder(new EmptyBorder(8, 0, 30, 0));
        sidebar.add(sub);

        String[][] menu = {
            {"Dashboard", "Dashboard"},
            {"All Books", "All Books"},
            {"Add Book", "Add Book"},
            {"Issue Book", "Issue Book"},
            {"Return Book", "Return Book"},
            {"Waiting List", "Waiting List"},
            {"Recently Returned", "Recently Returned"},
            {"Search by Topic", "Search by Topic"}
        };

        for (String[] item : menu) {
            JButton button = new JButton(item[0]);
            button.setMaximumSize(new Dimension(200, 43));
            button.setPreferredSize(new Dimension(200, 43));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFont(normal);
            button.setForeground(white);
            button.setBackground(navy);
            button.setBorder(new EmptyBorder(10, 15, 10, 5));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                if (item[1].equals("Add Book")) {
                    addBookDialog();
                } else if (item[1].equals("Issue Book")) {
                    issueBookDialog();
                } else if (item[1].equals("Return Book")) {
                    returnBookDialog();
                } else {
                    refreshAll();
                    showPage(item[1]);
                }
            });

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Java + Data Structures");
        footer.setForeground(new Color(190, 200, 220));
        sidebar.add(footer);

        return sidebar;
    }

    JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(15, 15));
        workspace.setBackground(background);
        workspace.setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(background);

        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(navy);

        JButton admin = new JButton("Admin Portal");
        admin.setFont(normal);

        header.add(title, BorderLayout.WEST);
        header.add(admin, BorderLayout.EAST);

        content.setBackground(background);
        content.add(createDashboard(), "Dashboard");
        content.add(createBooksPage(), "All Books");
        content.add(createWaitingPage(), "Waiting List");
        content.add(createReturnedPage(), "Recently Returned");
        content.add(createTopicPage(), "Search by Topic");

        workspace.add(header, BorderLayout.NORTH);
        workspace.add(content, BorderLayout.CENTER);

        return workspace;
    }

    JPanel createDashboard() {
        JPanel page = new JPanel(new BorderLayout(15, 20));
        page.setBackground(background);

        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBackground(background);

        JLabel welcome = new JLabel("Welcome to BookHub!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 29));
        welcome.setForeground(navy);

        JLabel description = new JLabel(
            "Manage your library collection, borrowing and returns."
        );
        description.setFont(normal);
        description.setForeground(Color.GRAY);

        intro.add(welcome);
        intro.add(Box.createVerticalStrut(8));
        intro.add(description);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 12));
        stats.setBackground(background);

        totalLabel = new JLabel("0");
        issuedLabel = new JLabel("0");
        waitingLabel = new JLabel("0");
        availableLabel = new JLabel("0");

        stats.add(statCard("TOTAL BOOKS", totalLabel, blue));
        stats.add(statCard("ISSUED BOOKS", issuedLabel, green));
        stats.add(statCard("WAITING", waitingLabel, new Color(220, 145, 35)));
        stats.add(statCard("AVAILABLE", availableLabel, new Color(120, 85, 190)));

        JPanel lower = new JPanel(new BorderLayout(10, 15));
        lower.setBackground(background);

        JLabel quick = new JLabel("Quick Actions");
        quick.setFont(bold);
        quick.setForeground(navy);

        JPanel actions = new JPanel(new GridLayout(2, 2, 15, 15));
        actions.setBackground(background);

        actions.add(actionCard("Add a New Book",
            "Register a book in the catalogue", this::addBookDialog));

        actions.add(actionCard("Issue a Book",
            "Borrow a book using student details", this::issueBookDialog));

        actions.add(actionCard("Return a Book",
            "Return a previously issued book", this::returnBookDialog));

        actions.add(actionCard("Explore Topics",
            "Find books by subject", () -> showPage("Search by Topic")));

        lower.add(quick, BorderLayout.NORTH);
        lower.add(actions, BorderLayout.CENTER);

        page.add(intro, BorderLayout.NORTH);
        page.add(stats, BorderLayout.CENTER);
        page.add(lower, BorderLayout.SOUTH);

        return page;
    }

    JPanel statCard(String title, JLabel number, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(white);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238)),
            new EmptyBorder(15, 13, 15, 13)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 11));
        heading.setForeground(Color.GRAY);

        number.setFont(new Font("SansSerif", Font.BOLD, 29));
        number.setForeground(color);

        card.add(heading);
        card.add(Box.createVerticalStrut(12));
        card.add(number);

        return card;
    }

    JPanel actionCard(String title, String description, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        card.setBackground(white);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238)),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        heading.setForeground(navy);

        JLabel detail = new JLabel(description);
        detail.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detail.setForeground(Color.GRAY);

        JButton button = new JButton("Open");
        button.setBackground(blue);
        button.setForeground(white);
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());

        card.add(heading, BorderLayout.NORTH);
        card.add(detail, BorderLayout.CENTER);
        card.add(button, BorderLayout.SOUTH);

        return card;
    }

    JPanel createBooksPage() {
        JPanel page = new JPanel(new BorderLayout(10, 15));
        page.setBackground(background);

        JLabel heading = new JLabel("All Books");
        heading.setFont(bold);
        heading.setForeground(navy);

        searchField = new JTextField(20);
        searchField.setFont(normal);

        JButton search = new JButton("Search");
        search.addActionListener(e -> filterBooks());

        JButton reset = new JButton("Show All");
        reset.addActionListener(e -> {
            searchField.setText("");
            filterBooks();
        });

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBackground(background);
        top.add(heading, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(background);
        searchPanel.add(searchField);
        searchPanel.add(search);
        searchPanel.add(reset);

        top.add(searchPanel, BorderLayout.SOUTH);

        String[] columns = {
            "ISBN", "Title", "Author", "Topic", "Available Copies"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);
        bookTable.setFont(normal);
        bookTable.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 13)
        );
        bookTable.setSelectionBackground(new Color(215, 226, 250));

        JScrollPane scroll = new JScrollPane(bookTable);
        scroll.getViewport().setBackground(white);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(background);

        JButton add = new JButton("Add Book");
        add.addActionListener(e -> addBookDialog());
        bottom.add(add);

        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    JPanel createWaitingPage() {
        JPanel page = new JPanel(new BorderLayout(10, 15));
        page.setBackground(background);

        JLabel heading = new JLabel("Student Waiting Lists");
        heading.setFont(bold);
        heading.setForeground(navy);

        JTextArea area = new JTextArea();
        area.setFont(normal);
        area.setEditable(false);
        area.setMargin(new Insets(15, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(background);
        top.add(heading, BorderLayout.NORTH);

        page.add(top, BorderLayout.NORTH);
        page.add(new JScrollPane(area), BorderLayout.CENTER);

        page.putClientProperty("waitingArea", area);

        return page;
    }

    JPanel createReturnedPage() {
        JPanel page = new JPanel(new BorderLayout(10, 15));
        page.setBackground(background);

        JLabel heading = new JLabel("Recently Returned Books");
        heading.setFont(bold);
        heading.setForeground(navy);

        JTextArea area = new JTextArea();
        area.setFont(normal);
        area.setEditable(false);
        area.setMargin(new Insets(15, 15, 15, 15));

        page.add(heading, BorderLayout.NORTH);
        page.add(new JScrollPane(area), BorderLayout.CENTER);
        page.putClientProperty("returnedArea", area);

        return page;
    }

    JPanel createTopicPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(background);

        JLabel heading = new JLabel("Search Books by Topic");
        heading.setFont(bold);
        heading.setForeground(navy);

        JTextField topicField = new JTextField(20);
        JButton search = new JButton("Search Topic");
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setFont(normal);
        result.setMargin(new Insets(15, 15, 15, 15));

        search.addActionListener(e -> {
            String topic = topicField.getText().trim();

            if (topic.isEmpty()) {
                showMessage("Enter a topic to search.");
                return;
            }

            TopicNode node = findTopic(root, topic);
            if (node == null) {
                result.setText("No books found for topic: " + topic);
            } else {
                StringBuilder text = new StringBuilder();
                text.append("Topic: ").append(node.topic).append("\n\n");

                for (Book book : node.books) {
                    text.append(book.title)
                        .append("\nAuthor: ").append(book.author)
                        .append("\nISBN: ").append(book.isbn)
                        .append("\nAvailable copies: ")
                        .append(book.availableCopies)
                        .append("\n\n");
                }

                result.setText(text.toString());
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(background);
        top.add(topicField);
        top.add(search);

        JPanel north = new JPanel(new BorderLayout(10, 10));
        north.setBackground(background);
        north.add(heading, BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);

        page.add(north, BorderLayout.NORTH);
        page.add(new JScrollPane(result), BorderLayout.CENTER);

        return page;
    }

    void addBookDialog() {
        JTextField isbn = new JTextField();
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField topic = new JTextField();
        JTextField copies = new JTextField("1");

        Object[] fields = {
            "ISBN:", isbn,
            "Book Title:", title,
            "Author:", author,
            "Topic:", topic,
            "Number of Copies:", copies
        };

        int choice = JOptionPane.showConfirmDialog(
            this, fields, "Add New Book",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String id = isbn.getText().trim();
        String bookTitle = title.getText().trim();
        String bookAuthor = author.getText().trim();
        String bookTopic = topic.getText().trim();

        if (id.isEmpty() || bookTitle.isEmpty()
            || bookAuthor.isEmpty() || bookTopic.isEmpty()) {
            showMessage("Please fill in all fields.");
            return;
        }

        if (books.containsKey(id)) {
            showMessage("This ISBN already exists.");
            return;
        }

        int count;

        try {
            count = Integer.parseInt(copies.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Enter a valid number of copies.");
            return;
        }

        if (count <= 0) {
            showMessage("Copies must be greater than zero.");
            return;
        }

        Book book = new Book(id, bookTitle, bookAuthor, bookTopic, count);

        books.put(id, book);
        bookList.add(book);
        root = insertTopic(root, book);

        refreshAll();
        showMessage("Book added successfully!");
    }

    void issueBookDialog() {
        JTextField studentId = new JTextField();
        JTextField studentName = new JTextField();
        JTextField isbn = new JTextField();

        Object[] fields = {
            "Student ID:", studentId,
            "Student Name:", studentName,
            "Book ISBN:", isbn
        };

        int choice = JOptionPane.showConfirmDialog(
            this, fields, "Issue Book",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String id = studentId.getText().trim();
        String name = studentName.getText().trim();
        String bookId = isbn.getText().trim();

        if (id.isEmpty() || name.isEmpty() || bookId.isEmpty()) {
            showMessage("Please fill in all fields.");
            return;
        }

        Book book = books.get(bookId);

        if (book == null) {
            showMessage("Book ISBN not found.");
            return;
        }

        if (issuedBooks.containsKey(id)) {
            showMessage("This student already has a book issued.");
            return;
        }

        if (isWaiting(id)) {
            showMessage("This student is already on a waiting list.");
            return;
        }

        Student student = new Student(id, name, bookId);

        if (book.availableCopies > 0) {
            book.availableCopies--;
            issuedBooks.put(id, student);

            refreshAll();
            showMessage("Book issued successfully to " + name + ".");
        } else {
            Queue<Student> queue = waitingLists.computeIfAbsent(
                bookId, k -> new LinkedList<>()
            );

            queue.offer(student);

            refreshAll();
            showMessage("No copies available. Student added to the queue.");
        }
    }

    boolean isWaiting(String studentId) {
        for (Queue<Student> queue : waitingLists.values()) {
            for (Student student : queue) {
                if (student.id.equals(studentId)) {
                    return true;
                }
            }
        }
        return false;
    }

    void returnBookDialog() {
        String id = JOptionPane.showInputDialog(
            this, "Enter the student ID:", "Return Book",
            JOptionPane.QUESTION_MESSAGE
        );

        if (id == null || id.trim().isEmpty()) {
            return;
        }

        id = id.trim();
        Student student = issuedBooks.get(id);

        if (student == null) {
            showMessage("No issued book found for this student.");
            return;
        }

        Book book = books.get(student.isbn);

        if (book == null) {
            showMessage("Book record not found.");
            return;
        }

        issuedBooks.remove(id);

        recentlyReturned.push(
            book.title + " (ISBN: " + book.isbn + ") returned by "
            + student.name + " [" + student.id + "]"
        );

        Queue<Student> queue = waitingLists.get(book.isbn);

        if (queue != null && !queue.isEmpty()) {
            Student next = queue.poll();
            issuedBooks.put(next.id, next);

            if (queue.isEmpty()) {
                waitingLists.remove(book.isbn);
            }

            refreshAll();

            showMessage("Book returned successfully.\n"
                + "Automatically issued to the next waiting student: "
                + next.name + " (" + next.id + ").");
        } else {
            book.availableCopies++;

            refreshAll();
            showMessage("Book returned successfully.");
        }
    }

    TopicNode insertTopic(TopicNode node, Book book) {
        if (node == null) {
            node = new TopicNode(book.topic);
            node.books.add(book);
            return node;
        }

        int compare = book.topic.compareToIgnoreCase(node.topic);

        if (compare < 0) {
            node.left = insertTopic(node.left, book);
        } else if (compare > 0) {
            node.right = insertTopic(node.right, book);
        } else {
            node.books.add(book);
        }

        return node;
    }

    TopicNode findTopic(TopicNode node, String topic) {
        while (node != null) {
            int compare = topic.compareToIgnoreCase(node.topic);

            if (compare == 0) {
                return node;
            }

            if (compare < 0) {
                node = node.left;
            } else {
                node = node.right;
            }
        }

        return null;
    }

    void refreshAll() {
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(bookList.size()));
            issuedLabel.setText(String.valueOf(issuedBooks.size()));

            int waitingCount = 0;
            for (Queue<Student> queue : waitingLists.values()) {
                waitingCount += queue.size();
            }

            waitingLabel.setText(String.valueOf(waitingCount));

            int availableCount = 0;
            for (Book book : bookList) {
                availableCount += book.availableCopies;
            }

            availableLabel.setText(String.valueOf(availableCount));
        }

        if (tableModel != null) {
            filterBooks();
        }

        JPanel waitingPage = getPage("Waiting List");
        if (waitingPage != null) {
            JTextArea area = (JTextArea)
                waitingPage.getClientProperty("waitingArea");

            StringBuilder text = new StringBuilder();

            if (waitingLists.isEmpty()) {
                text.append("No students are currently waiting.");
            } else {
                for (String isbn : waitingLists.keySet()) {
                    Book book = books.get(isbn);

                    text.append("Book: ")
                        .append(book == null ? isbn : book.title)
                        .append(" (ISBN: ").append(isbn).append(")\n");

                    int position = 1;

                    for (Student student : waitingLists.get(isbn)) {
                        text.append(position++).append(". ")
                            .append(student.name)
                            .append(" | ID: ").append(student.id)
                            .append("\n");
                    }

                    text.append("\n");
                }
            }

            area.setText(text.toString());
        }

        JPanel returnedPage = getPage("Recently Returned");
        if (returnedPage != null) {
            JTextArea area = (JTextArea)
                returnedPage.getClientProperty("returnedArea");

            StringBuilder text = new StringBuilder();

            if (recentlyReturned.isEmpty()) {
                text.append("No books have been returned yet.");
            } else {
                for (int i = recentlyReturned.size() - 1; i >= 0; i--) {
                    text.append(i + 1).append(". ")
                        .append(recentlyReturned.get(i))
                        .append("\n\n");
                }
            }

            area.setText(text.toString());
        }
    }

    JPanel getPage(String name) {
        for (Component component : content.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;

                if (name.equals("Waiting List")
                    && panel.getClientProperty("waitingArea") != null) {
                    return panel;
                }

                if (name.equals("Recently Returned")
                    && panel.getClientProperty("returnedArea") != null) {
                    return panel;
                }
            }
        }

        return null;
    }

    void filterBooks() {
        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        String query = searchField == null
            ? "" : searchField.getText().trim().toLowerCase();

        for (Book book : bookList) {
            String data = (book.isbn + " " + book.title + " "
                + book.author + " " + book.topic).toLowerCase();

            if (data.contains(query)) {
                tableModel.addRow(new Object[] {
                    book.isbn,
                    book.title,
                    book.author,
                    book.topic,
                    book.availableCopies
                });
            }
        }
    }

    void showPage(String name) {
        refreshAll();
        cardLayout.show(content, name);
    }

    void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this, message, "BookHub",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystem app =
                new LibraryManagementSystem();
            app.setVisible(true);
        });
    }
}
