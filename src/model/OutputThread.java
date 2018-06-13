package model;

import com.example.mrc.attendencesystem.entity.TranObject;
import com.example.mrc.attendencesystem.entity.TranObjectType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 */
public class OutputThread extends Thread{
	@SuppressWarnings("unused")
	private OutputThreadMap map;
	private SocketThreadMap socketThreadMap;
	//private ObjectOutputStream oos;
	private OutputStreamWriter oStreamWriter;
	private DataOutputStream dataOutputStream;
	private TranObject object;
	private boolean isStart = true;// 循环标志位
	private Socket socket;

	public OutputThread(Socket socket, OutputThreadMap map) {
		this.socket = socket;
		this.map = map;
		try {			
			dataOutputStream = new DataOutputStream(socket.getOutputStream());// 在构造器里面实例化对象输出流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	// 调用写消息线程，设置了消息之后，唤醒run方法，可以节约资源
	public void setMessage(TranObject object) {
		this.object = object;
		synchronized (this) {
			notify();
		}
	}

	@Override
	public void run() {
		try {
			
			while (isStart) {
				// 没有消息写出的时候，线程等待
				synchronized (this) {
					wait();
				}
				if (object != null) {
					 Gson gson = new GsonBuilder()
			            		 .setPrettyPrinting()  //格式化输出（序列化）
			            		 .setDateFormat("yyyy-MM-dd HH:mm:ss") //日期格式化输出
			            		 .create();
					   oStreamWriter = new OutputStreamWriter(dataOutputStream, "UTF-8");
					   String outputString = gson.toJson(object);
					   //dataOutputStream.writeInt(outputString.length());
					   //dataOutputStream.write(outputString.getBytes());
					   //dataOutputStream.flush();
					   StringBuffer sBuilder = new StringBuffer();
	                    sBuilder.append(outputString);
					   oStreamWriter.write(sBuilder.toString());
					   oStreamWriter.flush();
					   if(object != null && object.getType()!= TranObjectType.HEART_TEST)
					   {
						   System.out.println(outputString);
					   }
					  
				}
			}
			if(oStreamWriter != null)
			{
				oStreamWriter.close();
			}
			if (dataOutputStream != null)// 循环结束后，关闭流，释放资源
				dataOutputStream.close();
			if (socket != null)
				socket.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
