package org.jamp.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jamp.amp.AmpFactory;
import org.jamp.amp.SkeletonServiceInvoker;
import org.jamp.amp.encoder.Decoder;
import org.jamp.amp.encoder.JampMessageDecoder;
import org.jamp.example.model.EmployeeService;
import org.jamp.amp.AmpMessage;


public class JampFileProcessorMain {
	
    static SkeletonServiceInvoker serviceInvoker = AmpFactory.factory().createJampServerSkeleton(EmployeeService.class);
    static Decoder <AmpMessage, String> messageDecoder = new JampMessageDecoder();



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
			AmpMessage message = messageDecoder.decodeObject(payload);
		    serviceInvoker.invokeMessage(message);

		}
	}
	
	
}
