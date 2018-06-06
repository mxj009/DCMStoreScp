package com.tqhy.dcm4che;

import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.tasks.MainTask;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件上传服务端界面入口
 */
public class Main extends Application {

    /**
     * 界面布局
     */
    private GridPane root;

    /**
     * 界面场景
     */
    private Scene scene;


    /**
     * 选择存储路径按钮
     */
    private Button bt_store;

    /**
     * 显示本地存储路径
     */
    private Text tx_store;

    /**
     * 显示状态信息
     */
    private Text tx_result;

    private Button bt_start;

    /**
     * jar包所在路径
     */
    public static String rootPath;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10.0d);
        root.setVgap(10.0d);
        root.setPadding(new Insets(25, 25, 25, 25));
        //root.setGridLinesVisible(true);

        bt_store = new Button("选择保存路径:");
        bt_store.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择保存路径");
            File file = directoryChooser.showDialog(primaryStage);
            tx_store.setText(file.getAbsolutePath());
            System.out.println("Main select storage path: " + file.getAbsolutePath());
        });
        root.add(bt_store, 0, 0);
        tx_store = new Text();
        root.add(tx_store, 0, 1);

        bt_start = new Button("开启服务");
        bt_start.setOnAction(event -> {
            buildServerSocket();
        });
        root.add(bt_start, 0, 8);

        tx_result = new Text();
        tx_result.setFill(Color.RED);
        root.add(tx_result, 1, 8);

        scene = new Scene(root, 500, 400);

        primaryStage.setTitle("STORESCP");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
    }

    private void buildServerSocket() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(11113);
                Socket socket = null;
                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    socket = serverSocket.accept();
                    System.out.println(getClass().getSimpleName() + " buildServerSocket() accept socket: " + socket);
                    StorageConfig sdConfig = new StorageConfig();
                    sdConfig.setDirectory(tx_store.getText().trim());
                    //Device device = initDevice(connectConfig);
                    MainTask mainTask = new MainTask(socket, sdConfig);
                    pool.submit(mainTask);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void main(String[] args) {
        setRootPath();
        launch(args);
    }

    private static void setRootPath() {
        String osName = System.getProperty("os.name");
        int begin = osName.toLowerCase().startsWith("win") ? 1 : 0;
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //System.out.println("file location is: " + jarPath);
        int end = jarPath.lastIndexOf("/");
        rootPath = jarPath.substring(begin, end);
    }
}
