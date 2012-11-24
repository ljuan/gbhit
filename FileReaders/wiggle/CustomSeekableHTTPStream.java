package FileReaders.wiggle;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.samtools.util.HttpUtils;
/**
 * 
 * @author Chengwu Yan
 *
 */
public class CustomSeekableHTTPStream extends
		org.broad.tribble.util.SeekableStream {
	private long position = 0L;
	private long contentLength = -1L;
	private final URL url;

	public CustomSeekableHTTPStream(final URL url) {
		this.url = url;
		//get file length
		String contentLengthString = HttpUtils.getHeaderField(url,
				"Content-Length");
		if (contentLengthString != null)
			try {
				this.contentLength = Long.parseLong(contentLengthString);
			} catch (NumberFormatException ignored) {
				System.err.println("WARNING: Invalid content length ("
						+ contentLengthString + "  for: " + url);
				this.contentLength = -1L;
			}
	}

	public long length() {
		return this.contentLength;
	}

	public boolean eof() throws IOException {
		return this.position >= this.contentLength;
	}

	public void seek(long position) {
		this.position = position;
	}

	public int read(byte[] buffer, int offset, int len) throws IOException {
		if ((offset < 0) || (len < 0) || (offset + len > buffer.length)) {
			throw new IndexOutOfBoundsException("Offset=" + offset + ",len="
					+ len + ",buflen=" + buffer.length);
		}
		if (len == 0) {
			return 0;
		}
		HttpURLConnection connection = null;
		InputStream is = null;
		String byteRange = "";
		int n = 0;
		try {
			connection = (HttpURLConnection) this.url.openConnection();
			long endRange = this.position + len - 1L;
			if (this.contentLength > 0L) {
				endRange = Math.min(endRange, this.contentLength);
			}
			byteRange = "bytes=" + this.position + "-" + endRange;
			connection.setRequestProperty("Range", byteRange);
			is = connection.getInputStream();
			while (n < len) {
				int count = is.read(buffer, offset + n, len - n);
				if (count < 0) {
					if (n != 0)
						break;
					return -1;
				}
				n += count;
			}
			this.position += n;

			return n;
		} catch (IOException e) {
			if ((e.getMessage().contains("416"))
					|| ((e instanceof EOFException))) {
				if (n < 0) {
					return -1;
				}
				this.position += n;
				this.contentLength = this.position;
				return n;
			}
			throw e;
		} finally {
			if (is != null) {
				is.close();
			}
			if (connection != null)
				connection.disconnect();
		}
	}

	public void close() throws IOException {
	}

	public int read() throws IOException {
		byte[] tmp = new byte[1];
		read(tmp, 0, 1);
		return tmp[0] & 0xFF;
	}

	public String getSource() {
		return this.url.toString();
	}

	@Override
	public long position() throws IOException {
		return this.position;
	}
}
