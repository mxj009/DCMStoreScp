package com.tqhy.dcm4che.storescp;

import com.tqhy.dcm4che.storescp.configs.ConnectConfig;
import com.tqhy.dcm4che.storescp.configs.StorageDirectoryConfig;
import com.tqhy.dcm4che.storescp.configs.TransferCapabilityConfig;
import com.tqhy.dcm4che.storescp.enums.msg.ConnConfigMsg;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;
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

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private Button bt_start;

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
            MyStoreScp myStoreScp = new MyStoreScp();
            ConnectConfig connConfig = new ConnectConfig();
            String aeAtHostPort = tf_conn.getText().trim();
            ConnConfigMsg configMsg = connConfig.init(aeAtHostPort);
            tx_result.setText(configMsg.getDesc());
            if (ConnConfigMsg.SUCCESS == configMsg) {
                myStoreScp.setConnectConfig(connConfig);
            }else {
                return;
            }

            TransferCapabilityConfig tcConfig = new TransferCapabilityConfig();
            myStoreScp.setTcConfig(tcConfig);
            StorageDirectoryConfig sdConfig = new StorageDirectoryConfig();
            sdConfig.setDirectory(tx_store.getText().trim());
            myStoreScp.setSdConfig(sdConfig);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> task = executor.submit(myStoreScp);
            try {
                String s = task.get();
                tx_result.setText(s);
                System.out.println(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        root.add(bt_start, 0, 8);

        tx_result = new Text();
        tx_result.setFill(Color.RED);
        root.add(tx_result, 1, 8);

        scene = new Scene(root, 450, 300);
        primaryStage.setTitle("STORESCP");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
