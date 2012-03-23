package org.jamp.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.jamp.JampMessage;
import org.jamp.Decoder;
import org.jamp.SkeletonServiceInvoker;
import org.jamp.example.model.EmployeeService;
import org.jamp.impl.JampMessageDecoderImpl;

public class JampFileProcessorMain {

    static SkeletonServiceInvoker serviceInvoker = org.jamp.Factory.factory()
            .createJampServerSkeletonFromClass(EmployeeService.class);
    static Decoder<JampMessage, CharSequence> messageDecoder = new JampMessageDecoderImpl();

    private static String readPayload(File file) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            if (reader != null)
                reader.close();
        }

        return builder.toString();
    }

    @SuppressWarnings("nls")
    public static void main(String[] args) throws Exception {

        File dir = new File("/Users/rick/test/file_invoker");

        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            System.out.println(file);
            if (!file.toString().endsWith(".jamp")) {
                continue;
            }
            String payload = readPayload(file);
            JampMessage message = messageDecoder.decode(payload);
            serviceInvoker.invokeMessage(message);

        }
    }

}
