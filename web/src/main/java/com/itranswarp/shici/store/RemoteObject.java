package com.itranswarp.shici.store;

import java.util.Objects;

public class RemoteObject {

	public String location;
	public String bucket;
	public String object;

	public RemoteObject(String location, String bucket, String object) {
		this.location = location;
		this.bucket = bucket;
		this.object = object;
	}

	@Override
	public int hashCode() {
		return Objects.hash(location, bucket, object);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof RemoteObject) {
			RemoteObject r = (RemoteObject) o;
			return Objects.equals(this.location, r.location) && Objects.equals(this.bucket, r.bucket)
					&& Objects.equals(this.object, r.object);
		}
		return false;
	}

	public String toString() {
		return "{RemoteObject: bucket=" + bucket + ", location=" + location + ", " + object + "}";
	}
}
