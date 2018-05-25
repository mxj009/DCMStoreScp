package com.tqhy.dcm4che;

import com.tqhy.dcm4che.msg.ConnConfigMsg;
import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageConfig;
import com.tqhy.dcm4che.storescp.tasks.MainTask;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
     * 设置本地链接提示
     */
    private Label lb_conn;

    /**
     * 设置本地连接输入框
     */
    private TextField tf_conn;

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
    public static String rootPath;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10.0d);
        root.setVgap(10.0d);
        root.setPadding(new Insets(25, 25, 25, 25));
        //root.setGridLinesVisible(true);

        lb_conn = new Label("设置连接:");
        root.add(lb_conn, 0, 0);

        tf_conn = new TextField();
        tf_conn.setTooltip(new Tooltip("格式:[<aet>[@<ip>]:]<port>"));
        root.add(tf_conn, 1, 0);

        bt_store = new Button("选择保存路径:");
        bt_store.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择保存路径");
            File file = directoryChooser.showDialog(primaryStage);
            tx_store.setText(file.getAbsolutePath());
            System.out.println("Main select storage path: " + file.getAbsolutePath());
        });
        root.add(bt_store, 0, 1);
        tx_store = new Text();
        root.add(tx_store, 1, 1);

        bt_start = new Button("开启服务");
        bt_start.setOnAction(event -> {
            ConnectConfig connConfig = new ConnectConfig();
            String aeAtHostPort = tf_conn.getText().trim();
            ConnConfigMsg configMsg = connConfig.init(aeAtHostPort);
            tx_result.setText(configMsg.getDesc());
            if (ConnConfigMsg.CONFIG_SUCCESS == configMsg.getStatus()) {
                buildServerSocket(connConfig);
            } else {
                return;
            }


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

    private void buildServerSocket(ConnectConfig connConfig) {

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(connConfig.getPort() + 1);
                Socket socket = null;
                ExecutorService pool = Executors.newCachedThreadPool();
                while (true) {
                    socket = serverSocket.accept();
                    System.out.println(getClass().getSimpleName() + " buildServerSocket() accept socket: " + socket);
                    StorageConfig sdConfig = new StorageConfig();
                    sdConfig.setDirectory(tx_store.getText().trim());
                    MainTask mainTask = new MainTask(socket, connConfig, sdConfig);
                    pool.submit(mainTask);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        String osName = System.getProperty("os.name");
        int begin = osName.toLowerCase().startsWith("win") ? 1 : 0;
        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //System.out.println("file location is: " + jarPath);
        int end = jarPath.lastIndexOf("/");
        rootPath = jarPath.substring(0, end);
        launch(args);
    }
}
