package FileReaders.wiggle;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.broad.tribble.util.SeekableStream;
/**
 * 
 * @author Chengwu Yan
 *
 */
public class CustomSeekableBufferedStream extends SeekableStream {
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	private final int bufferSize;
	private final SeekableStream wrappedStream;
	private BufferedInputStream bufferedStream;
	private long position;

	public CustomSeekableBufferedStream(SeekableStream httpStream,
			int bufferSize) {
		this.bufferSize = bufferSize;
		this.wrappedStream = httpStream;
		this.position = 0L;
		this.bufferedStream = new BufferedInputStream(this.wrappedStream,
				bufferSize);
	}

	public CustomSeekableBufferedStream(SeekableStream httpStream) {
		this(httpStream, CustomSeekableBufferedStream.DEFAULT_BUFFER_SIZE);
	}

	public long length() {
		return this.wrappedStream.length();
	}

	public void seek(long position) throws IOException {
		this.position = position;
		this.wrappedStream.seek(position);
		this.bufferedStream = new BufferedInputStream(this.wrappedStream,
				this.bufferSize);
	}

	public int read() throws IOException {
		int b = this.bufferedStream.read();
		this.position++;
		return b;
	}

	@Override
	public void readFully(byte[] buffer) throws IOException {
		this.read(buffer);
	}

	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, 0, buffer.length);
	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (offset < 0 || length < 0 || (offset + length) > buffer.length) {
			throw new IndexOutOfBoundsException();
		}
		if (length == 0) {
			return 0;
		}

		int j = 0;
		while (j < length) {
			int nBytesRead = this.bufferedStream.read(buffer, offset + j,
					length - j);
			if (nBytesRead > 0) {
				this.position += nBytesRead;
			} else if (nBytesRead < 0) {
				throw new EOFException();
			}
			j += nBytesRead;
		}

		return j;
	}

	public void close() throws IOException {
		this.wrappedStream.close();
	}

	public boolean eof() throws IOException {
		return this.position >= this.wrappedStream.length();
	}

	@Override
	public long position() throws IOException {
		return this.position;
	}
}
