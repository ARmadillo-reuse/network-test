package edu.mit.armadillo.networktest.download;

public abstract class DownloadProgress {

	public final long timestamp;

	public DownloadProgress(long time) {
		this.timestamp = time;
	}

	public enum Type {
		Initiate, Start, Progress, Finish, Error
	}

	public abstract Type getType();

	public static class InitiateMessage extends DownloadProgress {
		public InitiateMessage(long time) {
			super(time);
		}

		@Override
		public Type getType() {
			return Type.Initiate;
		}

		@Override
		public String toString() {
			return "Initiate: @ " + this.timestamp;
		}
	}

	public static class StartMessage extends DownloadProgress {

		private final long size;

		public StartMessage(long time, long size) {
			super(time);
			this.size = size;
		}

		@Override
		public Type getType() {
			return Type.Start;
		}

		public boolean isSizeAvailable() {
			return this.size >= 0;
		}

		public long getDownloadSize() {
			return this.size;
		}

		@Override
		public String toString() {
			return "Start: size: " + size + " @ " + this.timestamp;
		}
	}

	public static class ProgressMessage extends DownloadProgress {
		private final long size;

		public ProgressMessage(long time, long total) {
			super(time);
			this.size = total;
		}

		@Override
		public Type getType() {
			return Type.Progress;
		}

		public long getDownloadByteCount() {
			return this.size;
		}

		@Override
		public String toString() {
			return "Progress: downloaded: " + size + " @ " + this.timestamp;
		}
	}

	public static class FinishMessage extends DownloadProgress {
		private final long size;

		public FinishMessage(long time, long total) {
			super(time);
			this.size = total;
		}

		@Override
		public Type getType() {
			return Type.Finish;
		}

		public long getFinalSize() {
			return this.size;
		}

		@Override
		public String toString() {
			return "Finished: size: " + size + " @ " + this.timestamp;
		}
	}

	public static class ErrorMessage extends DownloadProgress {
		public final String message;

		public ErrorMessage(long time, String message) {
			super(time);
			this.message = message;
		}

		@Override
		public Type getType() {
			return Type.Error;
		}

		@Override
		public String toString() {
			return "Error: message: " + this.message + " @ " + this.timestamp;
		}

	}

}