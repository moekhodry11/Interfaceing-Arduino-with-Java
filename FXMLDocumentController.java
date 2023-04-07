
package arduinojava;


import com.fazecast.jSerialComm.SerialPort;      // To Send Data To Arduino       
import java.net.URL;                             // To Communicate Between Java and Arduino 
import java.util.ResourceBundle;                 // Class Loader and Control for FXML File 
import javafx.collections.FXCollections;         // Invoking FXCollections Class 
import javafx.collections.ObservableList;        // list that allows us to track changes
import javafx.fxml.FXML;                         // XML Format enable to compose javaFX GUIs In Fashion Way "Scene Builder" 
import javafx.fxml.Initializable;                

import javafx.scene.control.Button;              // Button Library 
import javafx.scene.control.ComboBox;            // ComboBox Library 
import javafx.scene.control.Label;               // Label  Library
import javafx.geometry.Insets;                   // Library that allow to add distances to Components and layouts
import javafx.scene.Scene;                       // Scene that Contains Our Java Project 
import javafx.scene.chart.CategoryAxis;          // Import Category Axis to use it as X-Axis On Line Chart  
import javafx.scene.chart.LineChart;             // Import LineChart Class to use it as LineChart for Potentiometer Values
import javafx.scene.chart.NumberAxis;            // Import Num Axis to use it as Y-Axis On Line Chart 
import javafx.scene.chart.XYChart;               // Allow To us To Use two Axes
import javafx.scene.control.ProgressBar;         // Allow Us to Use ProgressBar for Potentiometer Values
import javafx.scene.control.TextArea;            // Allow Us To Print On Console and Control LCD TexTField 
import javafx.scene.control.ToggleButton;        // Allow To Us to Control Arduino Pins 
import javafx.scene.image.Image;                 // Allow To Us To add Images (LED Logo , Console Logo ..etc)
import javafx.scene.image.ImageView;             // Allow To Us To add Images Like Arrow Image On Console
import javafx.scene.layout.AnchorPane;           // Main Pane on Our Project "Contain Background" 
import javafx.scene.layout.BorderPane;           
import javafx.scene.layout.StackPane;            
import javafx.stage.Stage;                       
import javafx.event.ActionEvent;                 


import java.io.BufferedReader;                            // Handling Stream Of Data "Reading"
import java.io.IOException;                               
import java.io.InputStream;                               // Import InputStream Class
import java.io.InputStreamReader;                         // Allow Us TO Recieve Stream OF data From Arduino
import java.text.SimpleDateFormat;                        // Formatting Date
import java.util.Date;                                    // Import Date Class
import java.util.concurrent.Executors;                    // Schedule Commands To run After Delay 
import java.util.concurrent.ScheduledExecutorService;     
import java.util.concurrent.TimeUnit;                     // Represents Time Durations At a Given Unit
import java.util.logging.Level;                           // Logging facilities 
import java.util.logging.Logger;                          
import javafx.application.Platform;                       // Make Alot Of Functions Quickly 



/**
 *
 * @author emadeddin
 */

public class FXMLDocumentController implements Initializable  {

        // Declaring Data Variables Used in Our Project 

    String txtF;
    String selectedPin;
    String data;
    double read;
    SerialPort spConnected;
    int arrowFlag=0;
    char digit;
    private ScheduledExecutorService scheduledExecutorService;
    boolean[] flag = new boolean[11];

    
    
    
    // Declaring Variables for Controllers
    
    @FXML
    private Button arrow;
    
    @FXML
    private Label serialConnection;
    
    @FXML
    private Label lcdControl;
    
    @FXML
    private Label pinControl;
    
    @FXML
    private Label consoleLabel;
        
    @FXML
    private Label AnalogReading;
    
    @FXML
    private Button ChartButton;

    
    @FXML
    private AnchorPane mainPane;
    
    @FXML
    private StackPane stack;
    
    @FXML
    private ImageView arrowImage;
        
    @FXML
    private Button clearButton;

    @FXML
    private Button writeButton;
    
    @FXML
    private Button disconnectButton;

    @FXML
    private TextArea textField;
    
    @FXML
    private Button connectButton;
    
    @FXML
    private ComboBox<String> pinCombo;
    
    @FXML
    private ToggleButton toggle;
    
    @FXML
    private ProgressBar progress;
    
    
    @FXML
    private TextArea console;
    
    @FXML
    private ComboBox<String> combo;
    

    
    /*
        THIS METHOD ALLOW US CONNECT ACTION TO CONNECT BUTTON TO ARDUINO
        Here We Use SerialPort Library to Detect Ports Connected To The Used Device 
        and Save It's Name In Variable "sp" and Begin TO Deal With it  
        
        After Being Connected We Begin To Enable All of Controllers "Text Fields , Buttons ..etc"
        We Also Make a Thread and Use InputStream And BufferReader Classes To Handle Data Coming From Arduino 
    */ 

    @FXML
    void ConnectPress(ActionEvent event) {
        
        SerialPort sp = SerialPort.getCommPorts()[combo.getItems().indexOf(combo.getValue())];
        if (sp == null) {
            combo.setPromptText("Choose COM");
            return;
        }
                       
        if (connect(sp)) {
            
            textField.setDisable(false);
            writeButton.setDisable(false);
            clearButton.setDisable(false);
            disconnectButton.setDisable(false);
            pinCombo.setDisable(false);
            printToConsole("Connected to "+ sp.getDescriptivePortName());
            spConnected = sp;
            spConnected.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            new Thread(() -> {

                while (true) {
                    try {
                        InputStream portInputStream = sp.getInputStream();
                        if (portInputStream == null) {
                            //   isRun = false;
                            break;
                        }
                        BufferedReader bufferedReader 
                                = new BufferedReader(new InputStreamReader(
                                                portInputStream));

                        data = bufferedReader.readLine();



                        // we are in a different thread so update the label on ui thread
                        Platform.runLater(() -> {
                            try{
                                read = Double.parseDouble(data)/970;
                                // 970 -> Max Value can be read by arduino
                            } catch(NumberFormatException ex){

                            } 
                            progress.setProgress(read);
                            AnalogReading.setText(String.format("%.2f",read*10000)+ " Ohm");
                       });

                        Thread.sleep(500);
                    } catch (IOException ex) {
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

        } else {
            //show error
            printToConsole("Cant Connect");
            spConnected = null;
        }
        
        
    }

    
    
    /*
        Action Write Button to Arduino LCD 
        In This Method We Send Data To Arduino TO Print It On Arduino LCD 
        We Use Indicator For Arduino To Tell That This Method To Write On LCD "ONLY" 
        This Indicator Is ASCII Code For Num "30" 
    */
    
    @FXML
    void WriteTo(ActionEvent event) {
        txtF = textField.getText();
        if(!"".equals(txtF)){
                txtF = Character.toString((char)30)+txtF+Character.toString((char)30);
                spConnected.writeBytes(txtF.getBytes(), txtF.length());
                printToConsole("Sending " + '"' + txtF.substring(1,txtF.length()-1) + '"' + " to arduino.");
                
        }
        else{
            printToConsole("Write Something in Text Field.\nIf you want to clear the LCD, please press Clear");
        }
    }
    
    
    /*
        This method is used to clear Arduino LCD BY Sending Char TO Arduino "|"
        And On Arduino Code We Make "|" To Clear Arduino LCD
    */
    @FXML
    void ClearArduino(ActionEvent event) {
        
        textField.setText("");
        printToConsole("Arduino LCD Cleared.");
        byte d = (byte)('|');
        spConnected.writeBytes(new byte[]{d}, 1);
  
    }

    
    /*
         Disconnect Button Event
         When We Pressed On This Button We Close Port Between Arduion And JavaFx
         And Disable All Controllers "Buttons , TextFields...etc"
    */
    @FXML
    void DisconnectPress(ActionEvent event) {
        
        
        if (spConnected != null && spConnected.isOpen()) {
            spConnected.closePort();
            spConnected = null;
        }
        
       
        printToConsole("Arduino Disconnected");
        textField.setText("");
        textField.setDisable(true);
        writeButton.setDisable(true);
        clearButton.setDisable(true);
        disconnectButton.setDisable(true);
        combo.valueProperty().set(null);
        pinCombo.setDisable(true);
        toggle.setVisible(false);
        progress.setVisible(false);

        AnalogReading.setVisible(false);
        ChartButton.setVisible(false);
        pinCombo.valueProperty().set(null);
        


        
        
    }
    
    
    /*
        SelectPin Method Mainly Selecting the pin we control from arduino
        first it stores the last character from the pin we selected (i.e. Pin 2 it stores '2')
        after storing the pin number as a character we start choosing pins and its properties.

        flag array, it stores the status of all the pins either it's HIGH or LOW.
    */
    @FXML
    void SelectPin(ActionEvent event) {
        selectedPin = pinCombo.getSelectionModel().getSelectedItem(); 
        if(selectedPin != null){
            digit = selectedPin.charAt(selectedPin.length()-1); 
            if(digit == '0'){
                    digit = 58;
                }
            if( digit<=58){
                toggle.setVisible(true);
                progress.setVisible(false);
                AnalogReading.setVisible(false);
                ChartButton.setVisible(false);
            }
            else if(digit == 108){
                toggle.setVisible(false);
                progress.setVisible(false);
                AnalogReading.setVisible(false);
                ChartButton.setVisible(false);
            }
            else if("Analog Reading Pin".equals(selectedPin)){
                toggle.setVisible(false);
                progress.setVisible(true);
                AnalogReading.setVisible(true);
                ChartButton.setVisible(true);
            }
            if(flag[digit-'0'] == false ){  
                toggle.setSelected(false);
                
                toggle.setStyle("-fx-background-color: #fb2c2c;");
                toggle.setText("LOW");
            }
            else if(flag[digit-'0'] == true ){
                toggle.setSelected(true);
                toggle.setText("HIGH");
                toggle.setStyle("-fx-background-color: #00ff00; -fx-text-fill: #000000;");
            }
        }
    }
    
    
    /*
        TogglePress Method Allows Us To Control State Of Pins HIGH /LOW 
        By Sending ASCII Code OF Num To Arduino And Arduino Code Recieve It And
        Begin To Deal With It.
    */
    @FXML
    void TogglePress(ActionEvent event) {
        // Set High - First Press
        if(toggle.isSelected()){

            try {
                
                byte d = (byte)(digit-30); // 20
                spConnected.writeBytes(new byte[]{d}, 1);
                flag[digit-'0'] = true;
                

            } catch (Exception ex) {
                    printToConsole("Failed Disable");
            } 
            toggle.setText("HIGH");
            toggle.setStyle("-fx-background-color: #00ff00; -fx-text-fill: #000000;");
            
            printToConsole(selectedPin+" Set To HIGH.");
        }
        
        // Set LOW Second press [Default]
        else{
            try {
                //arduino.sendData(Character.toString((char)(DataToArduino-40)));
                byte d = (byte)(digit-40);
                spConnected.writeBytes(new byte[]{d}, 1);
                flag[digit-'0'] = false;

          
            } catch (Exception ex) {
                 printToConsole("Failed Enable");
            } 
            toggle.setStyle("-fx-background-color: #fb2c2c;");
            toggle.setText("LOW");
            
            printToConsole(selectedPin+" Set To LOW.");
        }
        
    }

    /*
     Arrow Click Allows Us TO Hide/Show Console TextField  
    */
    @FXML
    void ArrowClick(ActionEvent event) {
        if(arrowFlag == 0){
            arrowImage.setRotate(-90);
            arrowFlag = 1;
            console.setPrefHeight(0);
            
             
        }
        else{
            arrowImage.setRotate(0);
            arrowFlag=0;
            console.setPrefHeight(200);
        }
    }
    
    /*

     ViewChart Method Allows Us To View LineChart To Describe Values Of Potentiometer "Var Resistance"
      We Use LineChart Function To Create LineChart and Create Series To Display Data Arduino Reading
      We Use Class "Date" To Get Current Time From PC's Time To Put It Into X-axis
      When Series Of Data > 10 Pixels We Begin To Remove The Begining Data So That We Can Display 
      It's Changes With Time Every Second.

    */
    @FXML
    void ViewChart(ActionEvent event) {
        
        Stage primaryStage = new Stage();
        primaryStage.setTitle("JavaFX Realtime Chart ");
        Image icon = new Image("char.png");
        
        primaryStage.getIcons().add(icon);
        BorderPane border = new BorderPane();
        StackPane stack = new StackPane();
        stack.setPadding(new Insets(10,10,10,10));

        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis(); // we are gonna plot against time
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); // axis animations are removed
        yAxis.setLabel("Value");
        yAxis.setAnimated(false); // axis animations are removed

        //creating the line chart with two axis created above
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Realtime JavaFX Charts");
        lineChart.setAnimated(false); // disable animations

        //defining a series to display data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Potentiometer Readings");

        // add series to chart
        lineChart.getData().add(series);
        Button closeChart = new Button("Close");
        
        closeChart.setMinHeight(40);
        closeChart.setMinWidth(60);
        stack.getChildren().add(closeChart);
        border.setCenter(lineChart);
        border.setBottom(stack);

        // setup scene
        Scene scene = new Scene(border, 800, 600);
        primaryStage.setScene(scene);

        // show the stage
        primaryStage.show();
        

        // this is used to display time in HH:mm:ss format
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // setup a scheduled executor to periodically put data into the chart
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // put dummy data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // Update the chart
            closeChart.setOnAction(e->{

            primaryStage.close();
            scheduledExecutorService.shutdownNow();
                
            });
            Platform.runLater(() -> {

                // get current time
                Date now = new Date();
                series.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), Integer.parseInt(data)));
                
                if (series.getData().size() > 10)
                    series.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
        
            
        
        
         
        
    }
    
    
    
    
    
    /*
        Initialize Function Is The First Method TO Invoke In Our Project
        And It Contains The First State To Each Controller , Adding Available COMS To PinComboBox
    */
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        

        ObservableList<String> pinList = FXCollections.observableArrayList("Analog Reading Pin");
        // Filling PinCombo Box
        for(int i=2; i<=10; i++){
            pinList.add("Pin "+i);
            
        }
        pinCombo.setItems(pinList);
        
        // Default Intialize of Items
        combo.setEditable(false);
        textField.setDisable(true);
        writeButton.setDisable(true);
        clearButton.setDisable(true);
        disconnectButton.setDisable(true);
        toggle.setVisible(false);
        progress.setVisible(false);
        pinCombo.setDisable(true);
        AnalogReading.setVisible(false);
        ChartButton.setVisible(false);
        progress.setStyle("-fx-accent: #00FF00;");
        connectButton.setDisable(true);
        console.setStyle("-fx-control-inner-background:black; -fx-text-fill: white; ");

        //set combobox click handler
        combo.setOnMouseClicked((eh) -> {
            portComboAction();
        });
 }  
    
  
  


    /*
          PortComboAction Method Allows Us To Detect Connceted Arduino COMS
          and Insert it into COM ComboBox 
    */
    private void portComboAction() {
        
        SerialPort[] cmPorts = SerialPort.getCommPorts();

        if (cmPorts.length < 1) {
            connectButton.setDisable(true);
            return;
        } 
        
        else {
            combo.getItems().clear();
            combo.setPromptText("Choose COM");
            combo.setDisable(false);
            connectButton.setDisable(false);
            
        }

        Platform.runLater(() -> {

            for (SerialPort comPort : cmPorts) {
                printToConsole(comPort.getDescriptivePortName()+ " Detected");
                combo.getItems().add(comPort.getDescriptivePortName());
            }
        });

    }
  


    /*
           PrintToConsole Method Allows Us To Print On Console , It Needs String "s" That
            Will Print On Console 
    */
    void printToConsole(String s){
        if(!"".equals(console.getText())){

            console.appendText("\n"+"-> "+s);
            
        }
        else{
            console.setText("-> "+s);
        }
    }
    


    /*
          Connect Method Is Used To Toggle Between Closing / Open SerialPort
    */
    private boolean connect(SerialPort sp) {

        if (sp.isOpen()) {
            //close before connection
            sp.closePort();

        }
        return sp.openPort();

    }
    
    
    /*
     Shut Down Method Allows Us To Close Connection Between Java And Arduino
      By Disabling SerialPort  
    */
    public void shutdown() {
        if (spConnected != null) {
            spConnected.closePort();
            spConnected = null;
            scheduledExecutorService.shutdownNow();
        }
        Platform.exit();
    }
  
}


    