package akkaTipsAndTricks;

import java.io.File;

import akka.NotUsed;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Framing;
import akka.util.ByteString;

public class FlatmapExample {

	static int maxLineSize = 1024;

	static final ByteString delim = ByteString.fromString("\r\n");

	static final Flow<String, String, NotUsed> pathsToContents = 
	  Flow.of(String.class)
          .flatMapConcat(path -> FileIO.fromFile(new File(path))
			                           .via(Framing.delimiter(delim, maxLineSize))
			                           .map(byteStr -> byteStr.utf8String()));

	
}




































