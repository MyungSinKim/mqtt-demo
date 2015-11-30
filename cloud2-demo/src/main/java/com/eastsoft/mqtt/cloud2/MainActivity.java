package com.eastsoft.mqtt.cloud2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.model.MyMessage;

public class MainActivity extends Activity {

	private TextView resultTv;
	private Button light1Btn, light2Btn;
	private ImageView light1Iv,light2Iv;
	private ImageView tempIv;

//	private String host = "tcp://129.1.99.4:1883";
	private String host = "tcp://129.1.11.111:1883";
	private String userName = "eastsoft";
	private String passWord = "es";

	private Handler handler;

	private MqttClient client;

	private String myTopic = "sensor";
	private final String LIGHT_1_SUB = "eastsoft/things/BBBBBBBBBBBB/1664sub";
	private final String LIGHT_1_PUB = "eastsoft/things/BBBBBBBBBBBB/1664pub";
	private final String LIGHT_2_SUB = "eastsoft/things/CCCCCCCCCCCC/1664sub";
	private final String LIGHT_2_PUB = "eastsoft/things/CCCCCCCCCCCC/1664pub";
	private final String THERMOSTAT_SUB = "eastsoft/things/thermostat";

	private MqttConnectOptions options;

	private ScheduledExecutorService scheduler;
	//light pub state
	private MqttTopic light1PubTopic;
	private MqttTopic light2PubTopic;
	
	//switch pub control cmd
	private MqttTopic switch1PubTopic;
	private MqttTopic switch2PubTopic;

	private static Map<String, Integer> tempMap = new HashMap<String, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initView();
		initTempMap();
		initConnect();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case 1:
					System.out.println("-----------------------------");
					MyMessage payload = (MyMessage) msg.obj;
					Toast.makeText(MainActivity.this, payload.toString(),
							Toast.LENGTH_SHORT).show();
					if (LIGHT_1_SUB.equals(payload.getTopicName())) {
						MyMessage mmsg = new MyMessage();
						mmsg.setTopicName(LIGHT_1_PUB);
						if ("on".equals(payload.getMsg())) {
							light1Iv
									.setBackgroundResource(R.drawable.light_on);
							light1Iv.setTag("on");
							mmsg.setMsg("on");

						} else {
							light1Iv
									.setBackgroundResource(R.drawable.light_off);
							light1Iv.setTag("off");
							mmsg.setMsg("off");

						}
						pub(mmsg, light1PubTopic);
					} else if (LIGHT_2_SUB.equals(payload.getTopicName())) {
						MyMessage mmsg = new MyMessage();
						mmsg.setTopicName(LIGHT_2_PUB);
						if ("on".equals(payload.getMsg())) {
							light2Iv
									.setBackgroundResource(R.drawable.light_on);
							light2Iv.setTag("on");
							mmsg.setMsg("on");
						} else {
							light2Iv
									.setBackgroundResource(R.drawable.light_off);
							light2Iv.setTag("off");
							mmsg.setMsg("off");

						}
						pub(mmsg, light2PubTopic);
					} else if (THERMOSTAT_SUB.equals(payload.getTopicName())) {
						int temp = Integer.parseInt(payload.getMsg());
						tempIv.setBackgroundResource(tempMap.get(payload
								.getMsg()));
					}
					break;
				case 2:
					Toast.makeText(MainActivity.this, "连接成功",
							Toast.LENGTH_SHORT).show();
					try {
						light1PubTopic = client.getTopic(LIGHT_1_PUB);
						light2PubTopic = client.getTopic(LIGHT_2_PUB);
						switch1PubTopic = client.getTopic(LIGHT_1_SUB);
						switch2PubTopic = client.getTopic(LIGHT_2_SUB);
						client.subscribe(LIGHT_1_SUB, 1);
						client.subscribe(LIGHT_2_SUB, 1);
						client.subscribe(THERMOSTAT_SUB, 1);

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 3:
					Toast.makeText(MainActivity.this, "连接失败，系统正在重连",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			}
		};

		startReconnect();

	}

	private void initTempMap() {
		tempMap.put(16 + "", R.drawable.num_16);
		tempMap.put(17 + "", R.drawable.num_17);
		tempMap.put(18 + "", R.drawable.num_18);
		tempMap.put(19 + "", R.drawable.num_19);
		tempMap.put(20 + "", R.drawable.num_20);
		tempMap.put(21 + "", R.drawable.num_21);
		tempMap.put(22 + "", R.drawable.num_22);
		tempMap.put(23 + "", R.drawable.num_23);
		tempMap.put(24 + "", R.drawable.num_24);
		tempMap.put(25 + "", R.drawable.num_25);
		tempMap.put(26 + "", R.drawable.num_26);
		tempMap.put(27 + "", R.drawable.num_27);
		tempMap.put(28 + "", R.drawable.num_28);
		tempMap.put(29 + "", R.drawable.num_29);
		tempMap.put(30 + "", R.drawable.num_30);

	}

	private void initView() {
		tempIv = (ImageView) findViewById(R.id.temperture_iv);
		light1Iv=(ImageView)findViewById(R.id.light_1_iv);
		light2Iv=(ImageView)findViewById(R.id.light_2_iv);
		light1Btn = (Button) findViewById(R.id.imageButton1);
		light2Btn = (Button) findViewById(R.id.imageButton2);

		light1Btn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				try {
					MyMessage payload = new MyMessage();
					payload.setTopicName(LIGHT_1_SUB);
					if ("on".equals(light1Iv.getTag().toString())) {
						payload.setMsg("off");						
					} else {
						payload.setMsg("on");						

					}
					pub(payload, switch1PubTopic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		light2Btn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				try {
					MyMessage payload = new MyMessage();
					payload.setTopicName(LIGHT_2_SUB);
					if ("on".equals(light2Iv.getTag().toString())) {
						payload.setMsg("off");						

					} else {
						payload.setMsg("on");						

					}
					pub(payload, switch2PubTopic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void pub(MyMessage payload, MqttTopic myttTopic) {
		MqttMessage message = new MqttMessage();
		message.setQos(1);
		message.setRetained(true);
		System.out.println(message.isRetained() + "------ratained状态");
		// Gson gson = new Gson();
		// String payloadStr = gson.toJson(payload);
		message.setPayload(payload.getMsg().getBytes());
		try {
			MqttDeliveryToken token;
			token = myttTopic.publish(message);
			token.waitForCompletion();
			System.out.println(token.isComplete() + "========");

		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

	private void startReconnect() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {

			public void run() {
				if (!client.isConnected()) {
					connect();
				}
			}
		}, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
	}

	private void initConnect() {
		try {
			// host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
			client = new MqttClient(host, "test", new MemoryPersistence());
			// MQTT的连接设置
			options = new MqttConnectOptions();
			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
			options.setCleanSession(true);
			// //设置连接的用户名
			options.setUserName(userName);
			// //设置连接的密码
			options.setPassword(passWord.toCharArray());
			// 设置超时时间 单位为秒
			options.setConnectionTimeout(10);
			// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			options.setKeepAliveInterval(20);
			// 设置回调
			client.setCallback(new MqttCallback() {

				public void connectionLost(Throwable cause) {
					// 连接丢失后，一般在这里面进行重连
					System.out.println("connectionLost----------");
				}

				public void deliveryComplete(IMqttDeliveryToken token) {
					// publish后会执行到这里
					System.out.println("deliveryComplete---------"
							+ token.isComplete());
				}

				public void messageArrived(String topicName, MqttMessage message)
						throws Exception {
					// subscribe后得到的消息会执行到这里面
					System.out.println("messageArrived-----" + topicName
							+ "-----" + message.toString());
					Message msg = new Message();
					msg.what = 1;
					// msg.obj = topicName + "---" + message.toString();
					MyMessage mmsg = new MyMessage();
					mmsg.setMsg(message.toString());
					mmsg.setTopicName(topicName);
					msg.obj = mmsg;
					handler.sendMessage(msg);
				}

			});
			// connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void connect() {
		new Thread(new Runnable() {

			public void run() {
				try {
					client.connect(options);
					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.what = 3;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (client != null && keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				client.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			scheduler.shutdown();
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
