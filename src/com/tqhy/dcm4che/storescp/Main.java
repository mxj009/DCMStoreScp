package com.tqhy.dcm4che.storescp;

import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10.0d);
        root.setVgap(10.0d);
        root.setPadding(new Insets(25, 25, 25, 25));
        //root.setGridLinesVisible(true);

        Button bt_start = new Button("开启服务");
        bt_start.setOnAction(event -> {
            MyStoreScp myStoreScp = new MyStoreScp();
            new ConnectConfig("STORESCP:11112");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> task = executor.submit(myStoreScp);
        });

        scene = new Scene(root, 450, 300);
        primaryStage.setTitle("STORESCP");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
