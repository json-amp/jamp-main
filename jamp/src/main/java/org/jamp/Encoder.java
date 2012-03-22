package org.jamp;

public interface Encoder <BUFFER, OBJ> {
	BUFFER encode(OBJ obj) throws Exception; 
}
