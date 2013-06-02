package ma;

public class MessageInfo {
	int csid = 0;
	int request = 0;
	int reply = 0;
	int release = 0;
	int inquire = 0;
	int yield = 0;
	int failed = 0;

	public MessageInfo(int csid, int request, int reply, int release,
			int inquire, int yield, int failed) {
		this.csid = csid;
		this.request = request;
		this.reply = reply;
		this.release = release;
		this.inquire = inquire;
		this.yield = yield;
		this.failed = failed;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("csid: "+csid);
		buffer.append(", request: "+request);
		buffer.append(", reply: "+reply);
		buffer.append(", release: "+release);
		buffer.append(", inquire: "+inquire);
		buffer.append(", yield: "+yield);
		buffer.append(", failed: "+failed+"\n");
		return buffer.toString();
	}
}
