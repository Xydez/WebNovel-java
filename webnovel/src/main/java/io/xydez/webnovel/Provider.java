package io.xydez.webnovel;

import java.io.IOException;
import java.util.ArrayList;

public interface Provider {
	ArrayList<Novel> search(String query) throws IOException;
}
