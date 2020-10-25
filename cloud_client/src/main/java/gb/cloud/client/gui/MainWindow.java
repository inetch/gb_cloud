package gb.cloud.client.gui;

import gb.cloud.client.ClientSettings;
import gb.cloud.client.network.Network;
import gb.cloud.client.network.Sender;
import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class MainWindow extends Application implements Initializable {
    @Override
    public void start(Stage primaryStage) throws Exception {
      /*  FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Box Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();*/

        primaryStage.setTitle("Hello world");

        /*Label label1 = new Label("Label1");
        Scene scene = new Scene(label1, 400, 200);
        primaryStage.setScene(scene);*/

        primaryStage.show();

        /*
         try (Socket socket = new Socket("localhost", 8189);
             ObjectEncoderOutputStream oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
             ObjectDecoderInputStream odis = new ObjectDecoderInputStream(socket.getInputStream(), 100 * 1024 * 1024);) {
            MyMessage textMessage = new MyMessage("Hello Server!!!");
            oeos.writeObject(textMessage);
            oeos.flush();
            MyMessage msgFromServer = (MyMessage) odis.readObject();
            System.out.println("Answer from server: " + msgFromServer.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        * */

        /*
        try(Socket socket = new Socket(ClientSettings.SERVER_ADDRESS, CommonSettings.SERVER_PORT);
            ObjectEncoderOutputStream messageEncoder = new ObjectEncoderOutputStream(socket.getOutputStream());
            ObjectDecoderInputStream messageDecoder = new ObjectDecoderInputStream(socket.getInputStream(), CommonSettings.MAX_NETWORK_OBJECT_SIZE);
        ){
            //CommandMessage handshake = new CommandMessage(Command.HANDSHAKE);

            messageEncoder.writeObject(new CommandMessage(Command.HANDSHAKE));
            messageEncoder.flush();

            ResponseMessage response = (ResponseMessage) messageDecoder.readObject();
            System.out.println("Server answer: " + response);

            messageEncoder.writeObject(new CommandMessage(Command.HANDSHAKE));
            messageEncoder.flush();

            response = (ResponseMessage) messageDecoder.readObject();
            System.out.println("Server answer: " + response);
        }catch (Exception e){
            e.printStackTrace();
        }
*/

        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();

        networkStarter.await();

        JSONObject header = new JSONObject();
        header.put(CommonSettings.J_COMMAND, CommonSettings.C_AUTH);
        header.put(CommonSettings.J_USERNAME, "user-figuser");
        header.put(CommonSettings.J_PASSWORD, "password-figasword");

        Sender.sendHeader(header, Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }else{
                System.out.println("Header sent!");
            }
            Network.getInstance().stop();
        });

        /*obj.put("name", "foo");
        obj.put("num", new Integer(100));
        obj.put("balance", new Double(1000.21));
        obj.put("is_vip", new Boolean(true));*/


//        ProtoFileSender.sendFile(Paths.get("demo.txt"), Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
////                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
////                Network.getInstance().stop();
//            }
//        });
////        Thread.sleep(2000);
//        ProtoFileSender.sendFile(Paths.get("demo1.txt"), Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
////                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан");
////                Network.getInstance().stop();
//            }
//        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
