package org.jamp.amp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;

public class FileMessageSender implements AmpMessageSender {
	File dir;
	int counter;
	
	public FileMessageSender (final String aDir) {
		dir = new File(aDir);
		if (!dir.isDirectory()) {
			if (!dir.mkdirs()){
				throw new IllegalStateException("not able to create directory and it is not a directory " + dir);
			}
		}
		
		if (!dir.canWrite()) {
			throw new IllegalStateException("unable to write to dir " + dir);
		}
	}
	
	public AmpMessage sendMessage(AmpMessage message) throws Exception  {
		counter++;
		File outputFile = new File(dir, message.getToURL().getServiceName() + "." + message.getMethodName()  + "_" + System.currentTimeMillis() + "_" + counter + ".jamp");
		
		if (message.getPayload() instanceof String) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(outputFile);
				writer.write((String)message.getPayload());
			} finally {
				if (writer!=null) writer.close();
			}
		} else if (message.getPayload() instanceof byte[]) {
			FileOutputStream writer = null;
			try {
				writer = new FileOutputStream(outputFile);
				writer.write((byte[])message.getPayload());
			} finally {
				if (writer!=null) writer.close();
			}			
		} else {
			FileOutputStream writer = null;
			try {
				writer = new FileOutputStream(outputFile);
				ObjectOutputStream out = new ObjectOutputStream(writer);
				out.writeObject(message.getPayload());
			} finally {
				if (writer!=null) writer.close();
			}			
		}
		return null;
	}

}
