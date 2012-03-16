package org.jamp.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jamp.JampMessage;
import org.jamp.Decoder;
import org.jamp.SkeletonServiceInvoker;
import org.jamp.example.model.EmployeeService;
import org.jamp.impl.JampFactoryImpl;
import org.jamp.impl.JampMessageDecoder;


public class JampFileProcessorMain {
	
    static SkeletonServiceInvoker serviceInvoker = JampFactoryImpl.factory().createJampServerSkeleton(EmployeeService.class);
    static Decoder <JampMessage, String> messageDecoder = new JampMessageDecoder();



	private static String readPayload(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		
		try {
		reader = new BufferedReader(new FileReader(file));
		String line = null;
		
		while ((line=reader.readLine())!=null) {
			builder.append(line);
		}
		}finally{
			if (reader!=null)reader.close();
		}
		
		return builder.toString();
	}
	public static void main (String [] args) throws Exception {
		
		File dir = new File("/Users/rick/test/file_invoker");
		
		File[] listFiles = dir.listFiles();
		for (File file : listFiles) {
			System.out.println(file);
			if (!file.toString().endsWith(".jamp")){
				continue;
			}
			String payload = readPayload(file);
			JampMessage message = messageDecoder.decodeObject(payload);
		    serviceInvoker.invokeMessage(message);

		}
	}
	
	
}
