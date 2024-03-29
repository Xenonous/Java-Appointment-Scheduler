package Controllers;
import C195.JDBC;
import DataAccess.AppointmentDataAccess;
import DataAccess.CustomerDataAccess;
import UML.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Dylan Franklin
 */
public class AddAppointmentFormController implements Initializable {
    Stage stage;
    Parent scene;

    private static final DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter specialDateTimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId localZoneID = ZoneId.systemDefault();
    private static final ZoneId utcZoneID = ZoneId.of("UTC");
    private static final ZoneId estZoneID = ZoneId.of("US/Eastern");

    /**
     * Checks to see if any attempted created appointment is overlapping an already existing appointment. 'isOverlapping' is
     *     a boolean that's used as the return statement.
     *
     * @param selectedCustomerID
     * @return
     * @throws SQLException
     */
    private boolean isOverlapping(int selectedCustomerID) throws SQLException {


        boolean isOverlapping =  false;

        String appointmentStartDateTime = startDateTimePicker.getValue() + " " + startTimeTextField.getText();
        String appointmentEndDateTime = endDateTimePicker.getValue() + " " + endTimeTextField.getText();

        LocalDateTime userStartDT  = LocalDateTime.parse(appointmentStartDateTime, datetimeDTF);
        LocalDateTime userEndDT  = LocalDateTime.parse(appointmentEndDateTime, datetimeDTF);

        String SQL = "SELECT START,END FROM APPOINTMENTS WHERE CUSTOMER_ID = " + selectedCustomerID;
        PreparedStatement ps = JDBC.getConnection().prepareStatement(SQL);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {


            LocalDateTime utcStartDT = LocalDateTime.parse(rs.getString(1), specialDateTimeDTF);
            LocalDateTime utcEndDT = LocalDateTime.parse(rs.getString(2), specialDateTimeDTF);

            ZonedDateTime startDT = utcStartDT.atZone(utcZoneID).withZoneSameInstant(localZoneID);
            ZonedDateTime endDT = utcEndDT.atZone(utcZoneID).withZoneSameInstant(localZoneID);


            /*  OLD CODE
            if(userStartDT.isAfter(ChronoLocalDateTime.from(startDT)) && userStartDT.isBefore(ChronoLocalDateTime.from(endDT))) {
                isOverlapping = true;
                break;
            }
            else if (userEndDT.isAfter(ChronoLocalDateTime.from(startDT)) && userEndDT.isBefore(ChronoLocalDateTime.from(endDT))) {
                isOverlapping = true;
                break;
            }
            else if (userStartDT.isEqual(ChronoLocalDateTime.from(startDT)) && userEndDT.isEqual(ChronoLocalDateTime.from(endDT))) {
                isOverlapping = true;
                break;
            }


             */

            if(isBetweenDateTime(userStartDT,startDT,endDT) || isBetweenDateTime(userEndDT,startDT,endDT)) {
                System.out.println("The requested timeframe is BETWEEN another appointment.");
                isOverlapping = true;
            }

            else {
                isOverlapping = false;
            }

        }

        return isOverlapping;
    }


    /**
     * Method used to compare three LocalTime values. Used to determine if the candidate (user input value), is between the start (8AM) and end (10PM) values.
     *
     * @param candidate
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetweenTime(LocalTime candidate, LocalTime start, LocalTime end) {
        return !candidate.isBefore(start) && !candidate.isAfter(end);
    }

    public static boolean isBetweenDateTime(LocalDateTime candidate, ZonedDateTime start, ZonedDateTime end) {
        return !candidate.isBefore(ChronoLocalDateTime.from(start)) && !candidate.isAfter(ChronoLocalDateTime.from(end));
    }


    /**
     * Gets the users requested appointment local start/end times and translates them to eastern time (EST).
     *     The business operates on EST, so the conversion is needed. Once all the information is gathered, it is sent
     *     to the "isBetween" method for further evaluation.
     *
     * @return
     */
    private boolean isBusinessHours() {

        String localAppointmentStartDateTime = startDateTimePicker.getValue() + " " + startTimeTextField.getText();
        String localAppointmentEndDateTime = endDateTimePicker.getValue() + " " + endTimeTextField.getText();

        System.out.println("LOCAL TIME: " + localAppointmentStartDateTime + " " + localAppointmentEndDateTime);

        LocalDateTime estStartDT = LocalDateTime.parse(localAppointmentStartDateTime, datetimeDTF);
        LocalDateTime estEndDT = LocalDateTime.parse(localAppointmentEndDateTime, datetimeDTF);

        ZonedDateTime localZoneStart = estStartDT.atZone(localZoneID).withZoneSameInstant(estZoneID);
        ZonedDateTime localZoneEnd = estEndDT.atZone(localZoneID).withZoneSameInstant(estZoneID);

        String ESTAppointmentStartDateTime = localZoneStart.format(datetimeDTF);
        String ESTAppointmentEndDateTime = localZoneEnd.format(datetimeDTF);

        System.out.println("EST TIME: " + ESTAppointmentStartDateTime + " " + ESTAppointmentEndDateTime);

        String ESTAppointmentStartTime = ESTAppointmentStartDateTime.substring(11,16);
        String ESTAppointmentEndTime = ESTAppointmentEndDateTime.substring(11,16);

        LocalTime startTime = LocalTime.parse(ESTAppointmentStartTime);
        LocalTime endTime = LocalTime.parse(ESTAppointmentEndTime);

        // System.out.println(isBetween(startTime, LocalTime.of(8, 0), LocalTime.of(22, 0)));
        // System.out.println(isBetween(endTime, LocalTime.of(8, 0), LocalTime.of(22, 0)));


        return isBetweenTime(startTime, LocalTime.of(8, 0), LocalTime.of(22, 0)) && isBetweenTime(endTime, LocalTime.of(8, 0), LocalTime.of(22, 0));
    }


    /**
     * Information text.
     */
    @FXML
    private Text addAppointmentText;

    @FXML
    private Text contactText;

    @FXML
    private Text customerIDText;

    @FXML
    private Text descriptionText;

    @FXML
    private Text idText;

    @FXML
    private Text locationText;

    @FXML
    private Text titleText;

    @FXML
    private Text userIDText;

    @FXML
    private Text typeText;

    @FXML
    private Text startDateText;

    @FXML
    private Text startTimeText;

    @FXML
    private Text endDateText;

    @FXML
    private Text endTimeText;

    /**
     * ComboBox used to collect the Customer ID that will be associated with the Appointment ID.
     */
    @FXML
    private ComboBox<Customer> customerIDComboBox;

    /**
     * TextField used to collect the Appointment description.
     */
    @FXML
    private TextField descriptionTextField;

    /**
     * TextField used to collect the Appointment ID (NOT USED).
     */
    @FXML
    private TextField idTextField;

    /**
     * TextField used to collect the Appointment location
     */
    @FXML
    private TextField locationTextField;

    /**
     * TextField used to collect the Appointment title.
     */
    @FXML
    private TextField titleTextField;

    /**
     * ComboBox used to collect the User ID that will be associated with the Appointment ID
     */
    @FXML
    private ComboBox<String> userIDComboBox;

    /**
     * TextField used to collect the Appointment type
     */
    @FXML
    private TextField typeTextField;

    /**
     * DatePicker used to collect the starting date of the Appointment.
     */
    @FXML
    private DatePicker startDateTimePicker;

    /**
     * TextField used to collect the starting time of the Appointment in 'HH:MM'.
     */
    @FXML
    private TextField startTimeTextField;

    /**
     * DatePicker used to collect the ending date of the Appointment
     */
    @FXML
    private DatePicker endDateTimePicker;

    /**
     * TextField used to collect the ending time of the Appointment in 'HH:MM'.
     */
    @FXML
    private TextField endTimeTextField;

    /**
     * ComboBox used to collect the Contact (name) associated with the Appointment.
     */
    @FXML
    private ComboBox<String> contactComboBox;

    /**
     * Returns the user to the 'AppointmentsForm.fxml' menu
     *
     * @param event
     * @throws IOException
     */
    @FXML
    void onActionBackAppointmentsForm(ActionEvent event) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(getClass().getResource("/fxml/AppointmentsForm.fxml"));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Logic checks to see if all TextFields are filled, ensures there's no overlapping appointments, and makes sure the requested appointment is within business hours.
     *     If there's an error regarding any of these, an alert will show telling the user what the error is. If all information is entered correctly,
     *     the appointment is translated to UTC and stored in the database and the user is sent back to the 'AppointmentsForm.fxml' menu.
     *
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    void onActionSaveAppointment(ActionEvent event) throws IOException, SQLException {

        int selectedCustomerID = Integer.parseInt(String.valueOf(customerIDComboBox.getSelectionModel().getSelectedItem()));


        if (isOverlapping(selectedCustomerID)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("OVERLAPPING APPOINTMENTS");
            alert.setContentText("The appointment being created is overlapping another appointment by the same customer, please adjust the appointment date/time accordingly.");
            alert.showAndWait();
        }


        else if (titleTextField.getText().isEmpty() || descriptionTextField.getText().isEmpty() || locationTextField.getText().isEmpty() || typeTextField.getText().isEmpty() || startDateTimePicker.toString().isEmpty() || startTimeTextField.getText().isEmpty() || endDateTimePicker.toString().isEmpty() || endTimeTextField.getText().isEmpty() || customerIDComboBox.getSelectionModel().isEmpty() || userIDComboBox.getSelectionModel().isEmpty() || contactComboBox.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("MISSING INFORMATION");
            alert.setContentText("Missing key information. Please double check that all fields are populated BESIDES appointment ID");
            alert.showAndWait();
        }

        else if (!isBusinessHours()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("APPOINTMENT OUTSIDE BUSINESS HOURS");
            alert.setContentText("The following appointment is being created outside of business hours. Please create an appointment between 08:00 AM - 10:00 PM (22:00) (EST) ");
            alert.showAndWait();
        }

        else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CONFIRMATION");
            alert.setHeaderText("Appointment Addition");
            alert.setContentText("Do you really want to ADD the following appointment to the database?");
            Optional<ButtonType> OKButton = alert.showAndWait();

            if (OKButton.isPresent() && OKButton.get() == ButtonType.OK) {

                try {


                    String contactName = String.valueOf(contactComboBox.getSelectionModel().getSelectedItem());
                    int selectedContactID = AppointmentDataAccess.getContactID(contactName);
                    int selectedUserID = Integer.parseInt(String.valueOf(userIDComboBox.getSelectionModel().getSelectedItem()));


                    String appointmentStartDateTime = startDateTimePicker.getValue() + " " + startTimeTextField.getText();
                    String appointmentEndDateTime = endDateTimePicker.getValue() + " " + endTimeTextField.getText();

                    // System.out.println(appointmentStartDateTime + " " + appointmentEndDateTime);

                    LocalDateTime utcStartDT = LocalDateTime.parse(appointmentStartDateTime, datetimeDTF);
                    LocalDateTime utcEndDT = LocalDateTime.parse(appointmentEndDateTime, datetimeDTF);

                    ZonedDateTime localZoneStart = utcStartDT.atZone(localZoneID).withZoneSameInstant(utcZoneID);
                    ZonedDateTime localZoneEnd = utcEndDT.atZone(localZoneID).withZoneSameInstant(utcZoneID);

                    String UTCAppointmentStartDateTime = localZoneStart.format(datetimeDTF);
                    String UTCAppointmentEndDateTime = localZoneEnd.format(datetimeDTF);

                    // System.out.println(UTCAppointmentStartDateTime + " " + UTCAppointmentEndDateTime);

                    String SQL = "INSERT INTO appointments (Appointment_ID, Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement ps = JDBC.getConnection().prepareStatement(SQL);
                    ps.setInt(1, AppointmentDataAccess.getNewAppointmentID());
                    ps.setString(2, titleTextField.getText());
                    ps.setString(3, descriptionTextField.getText());
                    ps.setString(4, locationTextField.getText());
                    ps.setString(5, typeTextField.getText());
                    ps.setString(6, UTCAppointmentStartDateTime);
                    ps.setString(7, UTCAppointmentEndDateTime);
                    ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(9, "admin");
                    ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(11, "admin");
                    ps.setInt(12, selectedCustomerID);
                    ps.setInt(13, selectedUserID);
                    ps.setInt(14, selectedContactID);
                    ps.execute();


                    stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    scene = FXMLLoader.load(getClass().getResource("/fxml/AppointmentsForm.fxml"));
                    stage.setScene(new Scene(scene));
                    stage.show();
                }

                catch(Exception ex) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("DATE/TIME FORMAT ERROR.");
                    alert.setContentText("Please make sure your start date/time AND end date/time are in the correct formats. \n" + "DATE = 'YYYY-MM-DD' \n" + "TIME = 'HH:MM'");
                    alert.showAndWait();
                }

            }
        }
    }

    /**
     * ComboBox setups.
     * LAMBDA EXPRESSION #3. Keeps the user from selecting past dates for scheduling appointment start/end times.
     * @param url
     * @param rb
     */
    public void initialize(URL url, ResourceBundle rb) {
        try {
            contactComboBox.setPromptText("Select Contact");
            contactComboBox.setItems(AppointmentDataAccess.getAllContacts());
            customerIDComboBox.setPromptText(("Select CustomerID"));
            customerIDComboBox.setItems(CustomerDataAccess.getAllCustomers());
            userIDComboBox.setPromptText(("Select UserID"));
            userIDComboBox.setItems(AppointmentDataAccess.getAllUsers());

            //Lambda for startDateTimePicker
            startDateTimePicker.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate currentDate = LocalDate.now();

                    setDisable(empty || date.compareTo(currentDate) < 0 );

                }
            });

            //Lambda for endDateTimePicker
            endDateTimePicker.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate currentDate = LocalDate.now();

                    setDisable(empty || date.compareTo(currentDate) < 0 );

                }
            });

        }

        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }
}
