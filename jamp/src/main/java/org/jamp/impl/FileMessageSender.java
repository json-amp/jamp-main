package org.jamp.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;

import org.jamp.JampMessage;
import org.jamp.JampMessageSender;

public class FileMessageSender implements JampMessageSender {
	File dir;
	int counter;
	
	@SuppressWarnings("nls")
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
	
	@SuppressWarnings("nls")
    @Override
    public JampMessage sendMessage(JampMessage message) throws Exception  {
		counter++;
		File outputFile = new File(dir, message.getToURL().getServiceName() + "." + message.getAction()  + "_" + System.currentTimeMillis() + "_" + counter + ".jamp");
		
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
