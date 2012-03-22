package org.jamp;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Factory {
    static JampFactory factory;
    static Exception stackTrace;
    static volatile int count;
    
    public static JampFactory factory() {
        if (factory == null) {
            factory = new JampFactoryBase();
        }
        return factory;
    }
    
    @SuppressWarnings("nls")
    public static void setFactoryOnce(JampFactory aFactory) {
        count++;
        if (count>1) {
            StringWriter writer = new StringWriter();
            writer.write("\n\n\n______________________PREVIOUS STACK TRACE THAT CALLED setFactoryOnce_______________________");
            stackTrace.printStackTrace(new PrintWriter(writer)); 
            writer.write("\n\n\n______________________END OF PREVIOUS STACK TRACE THAT CALLED setFactoryOnce_______________________");
            throw new IllegalStateException("You cannot initialize the factory more than once SEE the previous stack trace that called this method " + writer.toString());
        }
        factory = aFactory;
        stackTrace = new Exception();
        stackTrace.fillInStackTrace();
    }

}
